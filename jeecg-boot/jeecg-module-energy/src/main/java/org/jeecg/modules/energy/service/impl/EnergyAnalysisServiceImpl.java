package org.jeecg.modules.energy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;

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
        String bStart = request.getBaselineStartTime();
        String bEnd = request.getBaselineEndTime();
        String cStart = request.getCompareStartTime();
        String cEnd = request.getCompareEndTime();
        String unit = getUnitByEnergyType(getEnergyTypeByModuleId(moduleId));

        List<String> categories = new ArrayList<>(); // 基准期用于图表的横轴（天粒度为MM-dd）
        List<Double> currentList = new ArrayList<>();
        List<Double> previousList = new ArrayList<>();
        // 表格用的完整日期（天粒度为yyyy-MM-dd）
        List<String> baselineFullDates = new ArrayList<>();
        List<String> compareFullDates = new ArrayList<>();

        // 按基准期与对比期分别计算
        if ("day".equalsIgnoreCase(timeType)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date bs = sdf.parse(bStart);
                Date be = sdf.parse(bEnd);
                Date cs = sdf.parse(cStart);
                Date ce = sdf.parse(cEnd);

                // 构建横轴：以基准期为准
                for (Date d = bs; !d.after(be); d = nextDay(d)) {
                    categories.add(new SimpleDateFormat("MM-dd").format(d));
                    baselineFullDates.add(new SimpleDateFormat("yyyy-MM-dd").format(d));
                }
                // 基准期曲线
                for (Date d = bs; !d.after(be); d = nextDay(d)) {
                    currentList.add(sumDay(moduleId, d));
                }
                // 对比期曲线
                for (Date d = cs; !d.after(ce); d = nextDay(d)) {
                    previousList.add(sumDay(moduleId, d));
                    compareFullDates.add(new SimpleDateFormat("yyyy-MM-dd").format(d));
                }
            } catch (ParseException ex) {
                throw new RuntimeException("日期解析失败: " + ex.getMessage());
            }
        } else if ("month".equalsIgnoreCase(timeType)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
                Date bs = sdf.parse(bStart);
                Date be = sdf.parse(bEnd);
                Date cs = sdf.parse(cStart);
                Date ce = sdf.parse(cEnd);

                for (Date d = bs; !d.after(be); d = nextMonth(d)) {
                    String m = new SimpleDateFormat("yyyy-MM").format(d);
                    categories.add(m);
                    baselineFullDates.add(m);
                }
                for (Date d = bs; !d.after(be); d = nextMonth(d)) {
                    currentList.add(sumMonth(moduleId, d));
                }
                for (Date d = cs; !d.after(ce); d = nextMonth(d)) {
                    previousList.add(sumMonth(moduleId, d));
                    compareFullDates.add(new SimpleDateFormat("yyyy-MM").format(d));
                }
            } catch (ParseException ex) {
                throw new RuntimeException("月份解析失败: " + ex.getMessage());
            }
        } else if ("year".equalsIgnoreCase(timeType)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
                Date bs = sdf.parse(bStart);
                Date be = sdf.parse(bEnd);
                Date cs = sdf.parse(cStart);
                Date ce = sdf.parse(cEnd);

                for (Date d = bs; !d.after(be); d = nextYear(d)) {
                    String y = new SimpleDateFormat("yyyy").format(d);
                    categories.add(y);
                    baselineFullDates.add(y);
                }
                for (Date d = bs; !d.after(be); d = nextYear(d)) {
                    currentList.add(sumYear(moduleId, d));
                }
                for (Date d = cs; !d.after(ce); d = nextYear(d)) {
                    previousList.add(sumYear(moduleId, d));
                    compareFullDates.add(new SimpleDateFormat("yyyy").format(d));
                }
            } catch (ParseException ex) {
                throw new RuntimeException("年份解析失败: " + ex.getMessage());
            }
        } else {
            throw new IllegalArgumentException("不支持的时间类型: " + timeType);
        }

        // 汇总
        double baselineTotal = currentList.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        double compareTotal = previousList.stream().filter(Objects::nonNull).mapToDouble(Double::doubleValue).sum();
        double savingTotal = baselineTotal - compareTotal;

        // 组装返回
        CompareDataVO vo = new CompareDataVO();
        CompareDataVO.SummaryData summary = new CompareDataVO.SummaryData();
        summary.setBaselineTotal(round(baselineTotal));
        summary.setCompareTotal(round(compareTotal));
        summary.setSavingTotal(round(savingTotal));
        summary.setUnit(unit);
        vo.setSummary(summary);

        CompareDataVO.ChartData chart = new CompareDataVO.ChartData();
        chart.setBaselineDates(categories);
        // 对比期日期：按对比期起止生成
        List<String> compareDates = new ArrayList<>();
        if ("day".equalsIgnoreCase(timeType)) {
            try { SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); for (Date d = sdf.parse(cStart); !d.after(sdf.parse(cEnd)); d = nextDay(d)) { compareDates.add(new SimpleDateFormat("MM-dd").format(d)); } } catch (ParseException ignored) {}
        } else if ("month".equalsIgnoreCase(timeType)) {
            try { SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM"); for (Date d = sdf.parse(cStart); !d.after(sdf.parse(cEnd)); d = nextMonth(d)) { compareDates.add(new SimpleDateFormat("yyyy-MM").format(d)); } } catch (ParseException ignored) {}
        } else {
            try { SimpleDateFormat sdf = new SimpleDateFormat("yyyy"); for (Date d = sdf.parse(cStart); !d.after(sdf.parse(cEnd)); d = nextYear(d)) { compareDates.add(new SimpleDateFormat("yyyy").format(d)); } } catch (ParseException ignored) {}
        }
        chart.setCompareDates(compareDates);

        List<CompareDataVO.SeriesData> series = new ArrayList<>();
        CompareDataVO.SeriesData s1 = new CompareDataVO.SeriesData();
        s1.setName("基准期");
        s1.setType("line");
        s1.setUnit(unit);
        s1.setData(currentList.stream().map(this::round).collect(Collectors.toList()));
        CompareDataVO.SeriesData s2 = new CompareDataVO.SeriesData();
        s2.setName("对比期");
        s2.setType("line");
        s2.setUnit(unit);
        s2.setData(previousList.stream().map(this::round).collect(Collectors.toList()));
        CompareDataVO.SeriesData s3 = new CompareDataVO.SeriesData();
        s3.setName("节能情况");
        s3.setType("bar");
        s3.setUnit(unit);
        List<Double> savingSeries = new ArrayList<>();
        for (int i=0; i<Math.max(currentList.size(), previousList.size()); i++){
            Double cur = safeGet(currentList, i); Double pre = safeGet(previousList, i);
            Double delta = (cur==null?0.0:cur) - (pre==null?0.0:pre);
            savingSeries.add(round(delta));
        }
        s3.setData(savingSeries);
        series.add(s1); series.add(s2); series.add(s3);
        chart.setSeries(series);
        vo.setChartData(chart);

        // 表格：基准时间 | 基准能耗 | 对比时间 | 对比能耗 | 节能情况
        List<CompareDataVO.TableData> table = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            CompareDataVO.TableData row = new CompareDataVO.TableData();
            // 使用完整日期用于表格展示（yyyy-MM-dd / yyyy-MM / yyyy）
            String bDate = i < baselineFullDates.size() ? baselineFullDates.get(i) : null;
            row.setBaselineDate(bDate);
            Double cur = safeGet(currentList, i);
            row.setBaselineValue(round(cur));
            String cDate = i < compareFullDates.size() ? compareFullDates.get(i) : null;
            row.setCompareDate(cDate);
            Double pre = safeGet(previousList, i);
            row.setCompareValue(round(pre));
            Double delta = (cur==null?0.0:cur) - (pre==null?0.0:pre);
            String savingText = (delta>=0?"节约 ":"超出 ") + (round(Math.abs(delta))) + " " + unit;
            row.setSavingText(savingText);
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
        String file = String.format("能源对比_%s_%s_基准%s~%s_对比%s~%s",
                Optional.ofNullable(data.getModuleInfo().getModuleName()).orElse(request.getModuleId()),
                request.getTimeType(),
                request.getBaselineStartTime(), request.getBaselineEndTime(),
                request.getCompareStartTime(), request.getCompareEndTime());
        try {
            String filename = URLEncoder.encode(file, "UTF-8");
            org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook();

            // 概览
            org.apache.poi.ss.usermodel.Sheet s1 = wb.createSheet("概览");
            int r = 0;
            s1.createRow(r++).createCell(0).setCellValue("仪表");
            s1.getRow(0).createCell(1).setCellValue(Optional.ofNullable(data.getModuleInfo().getModuleName()).orElse("-"));
            s1.createRow(r++).createCell(0).setCellValue("时间范围");
            s1.getRow(1).createCell(1).setCellValue("基准:"+request.getBaselineStartTime()+"~"+request.getBaselineEndTime()+
                    " 对比:"+request.getCompareStartTime()+"~"+request.getCompareEndTime());
            s1.createRow(r++).createCell(0).setCellValue("统计粒度");
            s1.getRow(2).createCell(1).setCellValue(request.getTimeType());
            s1.createRow(r++).createCell(0).setCellValue("基准用量("+unit+")");
            s1.getRow(3).createCell(1).setCellValue(nvl(data.getSummary().getBaselineTotal()));
            s1.createRow(r++).createCell(0).setCellValue("对比用量("+unit+")");
            s1.getRow(4).createCell(1).setCellValue(nvl(data.getSummary().getCompareTotal()));
            s1.createRow(r++).createCell(0).setCellValue("节能量("+unit+")");
            s1.getRow(5).createCell(1).setCellValue(nvl(data.getSummary().getSavingTotal()));


            // 趋势
            org.apache.poi.ss.usermodel.Sheet s2 = wb.createSheet("趋势对比");
            org.apache.poi.ss.usermodel.Row h2 = s2.createRow(0);
            h2.createCell(0).setCellValue("基准时间");
            h2.createCell(1).setCellValue("基准用量("+unit+")");
            h2.createCell(2).setCellValue("对比时间");
            h2.createCell(3).setCellValue("对比用量("+unit+")");
            h2.createCell(4).setCellValue("节能情况("+unit+")");
            int rows2 = Math.max(data.getChartData().getSeries().get(0).getData().size(), data.getChartData().getSeries().get(1).getData().size());
            for (int i=0;i<rows2;i++){
                org.apache.poi.ss.usermodel.Row row = s2.createRow(i+1);
                String baseDate = i < data.getChartData().getBaselineDates().size() ? data.getChartData().getBaselineDates().get(i) : "";
                String compDate = i < data.getChartData().getCompareDates().size() ? data.getChartData().getCompareDates().get(i) : "";
                Double baseVal = nvl(safeGet(data.getChartData().getSeries().get(0).getData(), i));
                Double compVal = nvl(safeGet(data.getChartData().getSeries().get(1).getData(), i));
                row.createCell(0).setCellValue(baseDate);
                row.createCell(1).setCellValue(baseVal);
                row.createCell(2).setCellValue(compDate);
                row.createCell(3).setCellValue(compVal);
                row.createCell(4).setCellValue(baseVal - compVal);
            }

            // 明细
            org.apache.poi.ss.usermodel.Sheet s3 = wb.createSheet("对比明细");
            org.apache.poi.ss.usermodel.Row h3 = s3.createRow(0);
            h3.createCell(0).setCellValue("基准时间");
            h3.createCell(1).setCellValue("基准用量("+unit+")");
            h3.createCell(2).setCellValue("对比时间");
            h3.createCell(3).setCellValue("对比用量("+unit+")");
            h3.createCell(4).setCellValue("节能情况("+unit+")");
            double sumBase=0,sumComp=0; int idx=1;
            for (CompareDataVO.TableData td : data.getTableData()){
                org.apache.poi.ss.usermodel.Row row = s3.createRow(idx++);
                row.createCell(0).setCellValue(td.getBaselineDate());
                row.createCell(1).setCellValue(nvl(td.getBaselineValue()));
                row.createCell(2).setCellValue(td.getCompareDate()==null?"--":td.getCompareDate());
                row.createCell(3).setCellValue(nvl(td.getCompareValue()));
                double delta = nvl(td.getBaselineValue()) - nvl(td.getCompareValue());
                row.createCell(4).setCellValue(delta);
                sumBase += nvl(td.getBaselineValue());
                sumComp += nvl(td.getCompareValue());
            }
            org.apache.poi.ss.usermodel.Row total = s3.createRow(idx);
            total.createCell(0).setCellValue("合计");
            total.createCell(1).setCellValue(sumBase);
            total.createCell(3).setCellValue(sumComp);
            total.createCell(4).setCellValue(sumBase - sumComp);

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

