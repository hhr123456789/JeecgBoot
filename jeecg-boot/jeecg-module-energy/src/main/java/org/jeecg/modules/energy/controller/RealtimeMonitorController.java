package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.net.URLEncoder;
import org.jeecg.modules.energy.service.IRealtimeMonitorService;
import org.jeecg.modules.energy.vo.realtime.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @Description: 实时数据监控Controller
 * @Author: jeecg-boot
 * @Date: 2025-07-25
 * @Version: V1.0
 */
@Api(tags = "实时数据监控")
@RestController
@RequestMapping("/energy/realtime")
@Slf4j
public class RealtimeMonitorController {

    @Autowired
    private IRealtimeMonitorService realtimeMonitorService;

    /**
     * 根据维度获取仪表列表
     *
     * @param dimensionCode   维度编码
     * @param energyType      能源类型(1:电力,2:天然气,3:压缩空气,4:企业用水)
     * @param includeChildren 是否包含子维度
     * @return 仪表列表
     */
    @ApiOperation(value = "根据维度获取仪表列表", notes = "根据维度编码获取该维度下的所有启用仪表")
    @GetMapping("/getModulesByDimension")
    public Result<List<ModuleInfoVO>> getModulesByDimension(
            @ApiParam(value = "维度编码", required = true, example = "A02A02A01") @RequestParam String dimensionCode,
            @ApiParam(value = "能源类型(1:电力,2:天然气,3:压缩空气,4:企业用水)", required = true, example = "1") @RequestParam Integer energyType,
            @ApiParam(value = "是否包含子维度", example = "true") @RequestParam(defaultValue = "true") Boolean includeChildren) {

        log.info("根据维度获取仪表列表，维度编码：{}，能源类型：{}，包含子维度：{}", dimensionCode, energyType, includeChildren);

        try {
            List<ModuleInfoVO> result = realtimeMonitorService.getModulesByDimension(dimensionCode, energyType,
                    includeChildren);
            log.info("查询到仪表数量：{}", result.size());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("根据维度获取仪表列表失败", e);
            return Result.error("查询仪表列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取参数配置
     *
     * @param energyType 能源类型(1:电力,2:天然气,3:压缩空气,4:企业用水)
     * @return 参数配置列表
     */
    @ApiOperation(value = "获取参数配置", notes = "根据能源类型获取可选的参数配置列表")
    @GetMapping("/getParameterConfig")
    public Result<List<ParameterConfigVO>> getParameterConfig(
            @ApiParam(value = "能源类型(1:电力,2:天然气,3:压缩空气,4:企业用水)", required = true, example = "1") @RequestParam Integer energyType) {

        log.info("获取参数配置，能源类型：{}", energyType);

        try {
            List<ParameterConfigVO> result = realtimeMonitorService.getParameterConfig(energyType);
            log.info("查询到参数配置数量：{}", result.size());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取参数配置失败", e);
            return Result.error("获取参数配置失败: " + e.getMessage());
        }
    }

    /**
     * 查询时序数据（核心接口）
     *
     * @param query 查询参数
     * @return 时序数据结果
     */
    @ApiOperation(value = "查询时序数据", notes = "根据时间粒度查询多仪表、多参数的时序数据，用于图表和表格展示")
    @PostMapping("/getTimeSeriesData")
    public Result<TimeSeriesResultVO> getTimeSeriesData(
            @ApiParam(value = "时序数据查询参数", required = true) @Valid @RequestBody TimeSeriesQueryVO query) {

        log.info("查询时序数据，参数：{}", query);

        try {
            TimeSeriesResultVO result = realtimeMonitorService.getTimeSeriesData(query);
            log.info("时序数据查询完成，数据点数：{}",
                    result.getChartData() != null ? result.getChartData().getTimeLabels().size() : 0);
            return Result.OK(result);
        } catch (IllegalArgumentException e) {
            log.warn("时序数据查询参数错误：{}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("查询时序数据失败", e);
            return Result.error("查询时序数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时状态
     *
     * @param request 请求参数
     * @return 实时状态列表
     */
    @ApiOperation(value = "获取实时状态", notes = "获取选中仪表的当前实时状态和最新数值")
    @PostMapping("/getCurrentStatus")
    public Result<List<ModuleStatusVO>> getCurrentStatus(
            @ApiParam(value = "实时状态查询参数", required = true) @Valid @RequestBody CurrentStatusRequestVO request) {

        log.info("获取实时状态，仪表ID：{}，参数：{}", request.getModuleIds(), request.getParameters());

        try {
            List<ModuleStatusVO> result = realtimeMonitorService.getCurrentStatus(request.getModuleIds(),
                    request.getParameters());
            log.info("查询到实时状态数量：{}", result.size());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取实时状态失败", e);
            return Result.error("获取实时状态失败: " + e.getMessage());
        }
    }

    /**
     * 查询负荷时序数据（核心接口）
     *
     * @param query 负荷查询参数
     * @return 负荷时序数据结果
     */
    @ApiOperation(value = "查询负荷时序数据", notes = "根据时间粒度查询多仪表的负荷时序数据，用于负荷曲线和负荷率曲线展示")
    @PostMapping("/getLoadTimeSeriesData")
    public Result<LoadTimeSeriesResultVO> getLoadTimeSeriesData(
            @ApiParam(value = "负荷时序数据查询参数", required = true) @Valid @RequestBody LoadTimeSeriesQueryVO query) {

        log.info("查询负荷时序数据，参数：{}", query);

        try {
            LoadTimeSeriesResultVO result = realtimeMonitorService.getLoadTimeSeriesData(query);
            log.info("负荷时序数据查询完成，仪表数量：{}", query.getModuleIds().size());
            return Result.OK(result);
        } catch (IllegalArgumentException e) {
            log.warn("负荷时序数据查询参数错误：{}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("查询负荷时序数据失败", e);
            return Result.error("查询负荷时序数据失败: " + e.getMessage());
        }
    }

    /**
     * 获取实时负荷状态
     *
     * @param request 负荷状态查询参数
     * @return 实时负荷状态列表
     */
    @ApiOperation(value = "获取实时负荷状态", notes = "获取选中仪表的当前实时负荷状态和最新功率数值")
    @PostMapping("/getCurrentLoadStatus")
    public Result<List<ModuleLoadStatusVO>> getCurrentLoadStatus(
            @ApiParam(value = "实时负荷状态查询参数", required = true) @Valid @RequestBody LoadStatusRequestVO request) {

        log.info("获取实时负荷状态，仪表ID：{}", request.getModuleIds());

        try {
            List<ModuleLoadStatusVO> result = realtimeMonitorService.getCurrentLoadStatus(request.getModuleIds());
            log.info("查询到实时负荷状态数量：{}", result.size());
            return Result.OK(result);
        } catch (Exception e) {
            log.error("获取实时负荷状态失败", e);
            return Result.error("获取实时负荷状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取负荷数据表格（统计数据）
     *
     * @param query 负荷表格查询参数
     * @return 负荷统计表格数据
     */
    @ApiOperation(value = "获取负荷数据表格", notes = "获取各仪表在指定时间范围内的详细功率和负荷率统计数据")
    @PostMapping("/getLoadTableData")
    public Result<LoadTableResultVO> getLoadTableData(
            @ApiParam(value = "负荷表格查询参数", required = true) @Valid @RequestBody LoadTableQueryVO query) {

        log.info("获取负荷数据表格，参数：{}", query);

        try {
            LoadTableResultVO result = realtimeMonitorService.getLoadTableData(query);
            log.info("负荷数据表格查询完成，数据条数：{}", result.getTableData().size());
            return Result.OK(result);
        } catch (IllegalArgumentException e) {
            log.warn("负荷数据表格查询参数错误：{}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("获取负荷数据表格失败", e);
            return Result.error("获取负荷数据表格失败: " + e.getMessage());
        }
    }
    
    /**
     * 导出负荷数据
     *
     * @param exportVO 导出参数
     * @return Excel文件字节流
     */
    @ApiOperation(value = "导出负荷数据", notes = "导出负荷数据为Excel文件")
    @PostMapping(value = "/exportLoadData", produces = "application/vnd.ms-excel")
    public void exportLoadData(
            @ApiParam(value = "负荷数据导出参数", required = true) @Valid @RequestBody LoadDataExportVO exportVO,
            HttpServletResponse response) {

        log.info("导出负荷数据，参数：{}", exportVO);

        try {
            byte[] excelBytes = realtimeMonitorService.exportLoadData(exportVO);
            log.info("负荷数据导出完成，文件大小：{} 字节", excelBytes.length);
            
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("UTF-8");
            String fileName = URLEncoder.encode(exportVO.getFileName(), "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            
            // 写入响应流
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(excelBytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            log.error("导出负荷数据失败", e);
            throw new RuntimeException("导出负荷数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 实时状态查询请求VO
     */
    public static class CurrentStatusRequestVO {
        @ApiParam(value = "仪表ID列表", required = true)
        private List<String> moduleIds;

        @ApiParam(value = "参数编码列表", required = true)
        private List<Integer> parameters;

        public List<String> getModuleIds() {
            return moduleIds;
        }

        public void setModuleIds(List<String> moduleIds) {
            this.moduleIds = moduleIds;
        }

        public List<Integer> getParameters() {
            return parameters;
        }

        public void setParameters(List<Integer> parameters) {
            this.parameters = parameters;
        }
    }

    /**
     * 负荷状态查询请求VO
     */
    public static class LoadStatusRequestVO {
        @ApiParam(value = "仪表ID列表", required = true)
        private List<String> moduleIds;

        public List<String> getModuleIds() {
            return moduleIds;
        }

        public void setModuleIds(List<String> moduleIds) {
            this.moduleIds = moduleIds;
        }
    }
}
