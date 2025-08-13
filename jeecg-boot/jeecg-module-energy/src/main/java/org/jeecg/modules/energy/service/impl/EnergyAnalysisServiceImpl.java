package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.energy.entity.TbEpEquEnergyDaycount;
import org.jeecg.modules.energy.entity.TbEpEquEnergyMonthcount;
import org.jeecg.modules.energy.entity.TbEpEquEnergyYearcount;
import org.jeecg.modules.energy.entity.TbModule;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyDaycountMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyMonthcountMapper;
import org.jeecg.modules.energy.mapper.TbEpEquEnergyYearcountMapper;
import org.jeecg.modules.energy.mapper.TbModuleMapper;
import org.jeecg.modules.energy.service.IEnergyAnalysisService;
import org.jeecg.modules.energy.vo.analysis.CompareDataRequest;
import org.jeecg.modules.energy.vo.analysis.CompareDataVO;
import org.jeecg.modules.energy.vo.analysis.ModuleVO;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.service.ISysDepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 能源分析对比 - 服务实现
 */
@Service
@Slf4j
public class EnergyAnalysisServiceImpl implements IEnergyAnalysisService {

    @Autowired
    private TbModuleMapper tbModuleMapper;
    @Autowired
    private TbEpEquEnergyDaycountMapper daycountMapper;
    @Autowired
    private TbEpEquEnergyMonthcountMapper monthcountMapper;
    @Autowired
    private TbEpEquEnergyYearcountMapper yearcountMapper;
    @Autowired
    private ISysDepartService sysDepartService;

    @Override
    public List<ModuleVO> getModulesByDimension(String orgCode, Integer energyType, Boolean includeChildren) {
        // orgCode -> sys_depart.id 列表
        List<String> departIds = resolveDepartIds(orgCode, includeChildren);
        if (departIds.isEmpty()) return Collections.emptyList();

        List<ModuleVO> list = tbModuleMapper.selectModulesByDimensionIds(departIds, energyType);
        // 单位映射
        for (ModuleVO vo : list) {
            vo.setEnergyTypeName(getEnergyTypeName(vo.getEnergyType()));
            vo.setUnit(getUnitByEnergyType(vo.getEnergyType()));
        }
        return list;
    }

    @Override
    public CompareDataVO getCompareData(CompareDataRequest request) {
        String moduleId = request.getModuleId();
        String timeType = request.getTimeType();
        String start = request.getStartTime();
        String end = request.getEndTime();
        String unit = getUnitByEnergyType(getEnergyTypeByModuleId(moduleId));

        List<String> categories = new ArrayList<>();
        List<Double> currentList = new ArrayList<>();
        List<Double> previousList = new ArrayList<>();

        // 计算同比区间
        if ("day".equalsIgnoreCase(timeType)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date s = sdf.parse(start);
                Date e = sdf.parse(end);
                Calendar cal = Calendar.getInstance();

                for (Date d = s; !d.after(e); d = nextDay(d)) {
                    String key = new SimpleDateFormat("MM-dd").format(d);
                    categories.add(key);
                    Double cur = sumDay(moduleId, d);
                    currentList.add(cur);

                    // 去年同一天
                    cal.setTime(d);
                    cal.add(Calendar.YEAR, -1);
                    Double pre = sumDay(moduleId, cal.getTime());
                    previousList.add(pre);
                }
            } catch (ParseException ex) {
                throw new RuntimeException("日期解析失败: " + ex.getMessage());
            }
        } else if ("month".equalsIgnoreCase(timeType)) {
            // start/end: yyyy-MM
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                Date s = sdf.parse(start);
                Date e = sdf.parse(end);

                for (Date d = s; !d.after(e); d = nextMonth(d)) {
                    String key = new SimpleDateFormat("yyyy-MM").format(d);
                    categories.add(key);
                    Double cur = sumMonth(moduleId, d);
                    currentList.add(cur);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    cal.add(Calendar.YEAR, -1);
                    Double pre = sumMonth(moduleId, cal.getTime());
                    previousList.add(pre);
                }
            } catch (ParseException ex) {
                throw new RuntimeException("月份解析失败: " + ex.getMessage());
            }
        } else if ("year".equalsIgnoreCase(timeType)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                Date s = sdf.parse(start);
                Date e = sdf.parse(end);

                for (Date d = s; !d.after(e); d = nextYear(d)) {
                    String key = new SimpleDateFormat("yyyy").format(d);
                    categories.add(key);
                    Double cur = sumYear(moduleId, d);
                    currentList.add(cur);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    cal.add(Calendar.YEAR, -1);
                    Double pre = sumYear(moduleId, cal.getTime());
                    previousList.add(pre);
                }
            } catch (ParseException ex) {
                throw new RuntimeException("年份解析失败: " + ex.getMessage());
            }
        } else {
            throw new IllegalArgumentException("不支持的时间类型: " + timeType);
        }

        // 汇总
        double totalCur = currentList.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        double totalPre = previousList.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        // 节能率 = (基准-对比)/基准
        Double savingRate = totalCur == 0 ? null : ((totalCur - totalPre) / totalCur) * 100.0;

        // 组装返回
        CompareDataVO vo = new CompareDataVO();
        CompareDataVO.SummaryData summary = new CompareDataVO.SummaryData();
        summary.setTotalConsumption(round(totalCur));
        summary.setPreviousConsumption(round(totalPre));
        summary.setGrowthRate(savingRate == null ? null : round(savingRate));
        summary.setUnit(unit);
        vo.setSummary(summary);

        CompareDataVO.ChartData chart = new CompareDataVO.ChartData();
        chart.setCategories(categories);
        List<CompareDataVO.SeriesData> series = new ArrayList<>();
        CompareDataVO.SeriesData s1 = new CompareDataVO.SeriesData();
        s1.setName("基准线");
        s1.setUnit(unit);
        s1.setData(currentList.stream().map(this::round).collect(Collectors.toList()));
        CompareDataVO.SeriesData s2 = new CompareDataVO.SeriesData();
        s2.setName("对比线");
        s2.setUnit(unit);
        s2.setData(previousList.stream().map(this::round).collect(Collectors.toList()));
        series.add(s1); series.add(s2);
        chart.setSeries(series);
        vo.setChartData(chart);

        List<CompareDataVO.TableData> table = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            CompareDataVO.TableData row = new CompareDataVO.TableData();
            row.setDate(categories.get(i));
            Double cur = safeGet(currentList, i);
            Double pre = safeGet(previousList, i);
            row.setCurrentConsumption(round(cur));
            row.setPreviousConsumption(round(pre));
            Double diff = (cur == null || pre == null) ? null : cur - pre;
            row.setDifference(round(diff));
            Double rate = (cur == null || pre == null || cur == 0) ? null : (cur - pre) / cur * 100.0; // 节能率=节能量/基准
            row.setGrowthRate(rate == null ? null : round(rate));
            table.add(row);
        }
        vo.setTableData(table);

        CompareDataVO.ModuleInfo mi = new CompareDataVO.ModuleInfo();
        mi.setModuleId(moduleId);
        TbModule m = tbModuleMapper.selectByModuleId(moduleId).stream().findFirst().orElse(null);
        if (m != null) {
            mi.setModuleName(m.getModuleName());
            mi.setEnergyType(m.getEnergyType());
            mi.setUnit(getUnitByEnergyType(m.getEnergyType()));
            // 维度名称
            if (StringUtils.hasText(m.getSysOrgCode())) {
                SysDepart d = sysDepartService.getById(m.getSysOrgCode());
                if (d != null) mi.setDimensionName(d.getDepartName());
            }
        }
        vo.setModuleInfo(mi);
        return vo;
    }

    @Override
    public void exportCompareData(CompareDataRequest request, HttpServletResponse response) {
        // 使用简单POI导出，三张Sheet：概览、趋势、明细
        CompareDataVO data = getCompareData(request);
        String unit = data.getSummary().getUnit();
        String file = String.format("能源对比_%s_%s_%s至%s", Optional.ofNullable(data.getModuleInfo().getModuleName()).orElse(request.getModuleId()),
                request.getTimeType(), request.getStartTime(), request.getEndTime());
        try {
            String filename = URLEncoder.encode(file, "UTF-8");
            org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();

            // 概览
            org.apache.poi.ss.usermodel.Sheet s1 = wb.createSheet("概览");
            int r = 0;
            s1.createRow(r++).createCell(0).setCellValue("仪表");
            s1.getRow(0).createCell(1).setCellValue(Optional.ofNullable(data.getModuleInfo().getModuleName()).orElse("-"));
            s1.createRow(r++).createCell(0).setCellValue("时间范围");
            s1.getRow(1).createCell(1).setCellValue(request.getStartTime()+" 至 "+request.getEndTime());
            s1.createRow(r++).createCell(0).setCellValue("统计粒度");
            s1.getRow(2).createCell(1).setCellValue(request.getTimeType());
            s1.createRow(r++).createCell(0).setCellValue("基准用量("+unit+")");
            s1.getRow(3).createCell(1).setCellValue(nvl(data.getSummary().getTotalConsumption()));
            s1.createRow(r++).createCell(0).setCellValue("对比用量("+unit+")");
            s1.getRow(4).createCell(1).setCellValue(nvl(data.getSummary().getPreviousConsumption()));
            s1.createRow(r++).createCell(0).setCellValue("节能量("+unit+")");
            s1.getRow(5).createCell(1).setCellValue(nvl(data.getSummary().getTotalConsumption()-data.getSummary().getPreviousConsumption()));
            s1.createRow(r++).createCell(0).setCellValue("节能率(%)");
            s1.getRow(6).createCell(1).setCellValue(data.getSummary().getGrowthRate()==null?"--":String.valueOf(data.getSummary().getGrowthRate()));

            // 趋势
            org.apache.poi.ss.usermodel.Sheet s2 = wb.createSheet("趋势对比");
            org.apache.poi.ss.usermodel.Row h2 = s2.createRow(0);
            h2.createCell(0).setCellValue("时间");
            h2.createCell(1).setCellValue("基准用量("+unit+")");
            h2.createCell(2).setCellValue("对比用量("+unit+")");
            for (int i=0;i<data.getChartData().getCategories().size();i++){
                org.apache.poi.ss.usermodel.Row row = s2.createRow(i+1);
                row.createCell(0).setCellValue(data.getChartData().getCategories().get(i));
                row.createCell(1).setCellValue(nvl(safeGet(data.getChartData().getSeries().get(0).getData(), i)));
                row.createCell(2).setCellValue(nvl(safeGet(data.getChartData().getSeries().get(1).getData(), i)));
            }

            // 明细
            org.apache.poi.ss.usermodel.Sheet s3 = wb.createSheet("对比明细");
            org.apache.poi.ss.usermodel.Row h3 = s3.createRow(0);
            h3.createCell(0).setCellValue("时间");
            h3.createCell(1).setCellValue("基准用量("+unit+")");
            h3.createCell(2).setCellValue("对比用量("+unit+")");
            h3.createCell(3).setCellValue("节能量("+unit+")");
            h3.createCell(4).setCellValue("节能率(%)");
            double sumCur=0,sumPre=0; int idx=1;
            for (CompareDataVO.TableData td : data.getTableData()){
                org.apache.poi.ss.usermodel.Row row = s3.createRow(idx++);
                row.createCell(0).setCellValue(td.getDate());
                row.createCell(1).setCellValue(nvl(td.getCurrentConsumption()));
                row.createCell(2).setCellValue(nvl(td.getPreviousConsumption()));
                row.createCell(3).setCellValue(nvl(td.getDifference()));
                row.createCell(4).setCellValue(td.getGrowthRate()==null?"--":String.valueOf(td.getGrowthRate()));
                sumCur += nvl(td.getCurrentConsumption());
                sumPre += nvl(td.getPreviousConsumption());
            }
            org.apache.poi.ss.usermodel.Row total = s3.createRow(idx);
            total.createCell(0).setCellValue("合计");
            total.createCell(1).setCellValue(sumCur);
            total.createCell(2).setCellValue(sumPre);
            total.createCell(3).setCellValue(sumCur - sumPre);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition","attachment; filename="+filename+".xlsx");
            wb.write(response.getOutputStream());
            wb.close();
        } catch (Exception e) {
            log.error("导出失败", e);
            throw new RuntimeException("导出失败:"+e.getMessage());
        }
    }

    // ============== 内部方法 ==============
    private List<String> resolveDepartIds(String orgCode, boolean includeChildren){
        List<String> ids = new ArrayList<>();
        try{
            QueryWrapper<SysDepart> w = new QueryWrapper<>();
            w.eq("org_code", orgCode);
            SysDepart cur = sysDepartService.getOne(w);
            if (cur==null) return ids;
            ids.add(cur.getId());
            if (includeChildren){
                QueryWrapper<SysDepart> cw = new QueryWrapper<>();
                cw.eq("parent_id", cur.getId());
                List<SysDepart> children = sysDepartService.list(cw);
                ids.addAll(children.stream().map(SysDepart::getId).collect(Collectors.toList()));
            }
        }catch (Exception e){
            log.error("解析维度失败", e);
        }
        return ids;
    }

    private Integer getEnergyTypeByModuleId(String moduleId){
        List<TbModule> list = tbModuleMapper.selectByModuleId(moduleId);
        return list.isEmpty()?1:list.get(0).getEnergyType();
    }

    private String getUnitByEnergyType(Integer type){
        if (type==null) return "m³";
        switch (type){
            case 1: return "kWh"; // 电
            case 2: return "m³";  // 水
            case 3: return "m³";  // 气
            default: return "m³";
        }
    }

    private String getEnergyTypeName(Integer type){
        if (type==null) return "其他";
        switch (type){
            case 1: return "电";
            case 2: return "水";
            case 3: return "气";
            default: return "其他";
        }
    }

    private Date nextDay(Date d){ Calendar c=Calendar.getInstance(); c.setTime(d); c.add(Calendar.DAY_OF_MONTH,1); return c.getTime(); }
    private Date nextMonth(Date d){ Calendar c=Calendar.getInstance(); c.setTime(d); c.add(Calendar.MONTH,1); return c.getTime(); }
    private Date nextYear(Date d){ Calendar c=Calendar.getInstance(); c.setTime(d); c.add(Calendar.YEAR,1); return c.getTime(); }

    private Double sumDay(String moduleId, Date date){
        // 直接按日期=当天汇总 energy_count
        QueryWrapper<TbEpEquEnergyDaycount> w = new QueryWrapper<>();
        w.eq("module_id", moduleId).apply("DATE(dt) = DATE({0})", date);
        List<TbEpEquEnergyDaycount> list = daycountMapper.selectList(w);
        return list.stream().map(x -> x.getEnergyCount() == null ? 0.0 : x.getEnergyCount().doubleValue()).mapToDouble(Double::doubleValue).sum();
    }

    private Double sumMonth(String moduleId, Date date){
        QueryWrapper<TbEpEquEnergyMonthcount> w = new QueryWrapper<>();
        w.eq("module_id", moduleId)
         .apply("YEAR(dt)=YEAR({0})", date)
         .apply("MONTH(dt)=MONTH({0})", date);
        List<TbEpEquEnergyMonthcount> list = monthcountMapper.selectList(w);
        return list.stream().map(x -> x.getEnergyCount() == null ? 0.0 : x.getEnergyCount().doubleValue()).mapToDouble(Double::doubleValue).sum();
    }

    private Double sumYear(String moduleId, Date date){
        QueryWrapper<TbEpEquEnergyYearcount> w = new QueryWrapper<>();
        w.eq("module_id", moduleId)
         .apply("YEAR(dt)=YEAR({0})", date);
        List<TbEpEquEnergyYearcount> list = yearcountMapper.selectList(w);
        return list.stream().map(x -> x.getEnergyCount() == null ? 0.0 : x.getEnergyCount().doubleValue()).mapToDouble(Double::doubleValue).sum();
    }

    private Double round(Double d){ if (d==null) return null; return new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue(); }
    private double nvl(Double d){ return d==null?0.0:d; }
    private Double safeGet(List<Double> list, int i){ return (list!=null && i<list.size())? list.get(i): null; }
}

