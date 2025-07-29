package org.jeecg.modules.energy.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.energy.service.IEnergyMonitorService;
import org.jeecg.modules.energy.vo.monitor.ModuleVO;
import org.jeecg.modules.energy.vo.monitor.RealTimeDataRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @Description: 能源监控控制器
 * @Author: jeecg-boot
 * @Date: 2023-08-17
 * @Version: V1.0
 */
@RestController
@RequestMapping("/energy/monitor")
@Api(tags = "能源实时监控")
@Slf4j
public class EnergyMonitorController {

    @Autowired
    private IEnergyMonitorService energyMonitorService;
    
    /**
     * 获取实时监控数据
     *
     * @param orgCode 部门编码
     * @param nowtype 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)
     * @return 实时监控数据
     */
    @ApiOperation(value = "获取实时监控数据", notes = "获取右侧实时监控数据")
    @GetMapping("/getRealTimeData")
    public Result<List<Map<String, Object>>> getRealTimeData(
            @ApiParam(value = "部门编码", required = true) @RequestParam String orgCode,
            @ApiParam(value = "维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)", required = true) @RequestParam Integer nowtype) {
        log.info("获取实时监控数据，部门编码：{}，能源类型：{}", orgCode, nowtype);
        List<Map<String, Object>> result = energyMonitorService.getRealTimeData(orgCode, nowtype);
        log.info("查询结果条数：{}", result.size());
        return Result.OK(result);
    }

    /**
     * 根据维度获取仪表列表
     *
     * @param orgCodes 维度编码列表（支持多选）
     * @param nowtype 维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)
     * @return 仪表列表
     */
    @ApiOperation(value = "根据维度获取仪表列表", notes = "根据维度编码获取该维度下的所有启用仪表，支持多选维度")
    @GetMapping("/getModulesByOrgCode")
    public Result<List<ModuleVO>> getModulesByOrgCode(
            @ApiParam(value = "维度编码列表，多个用逗号分隔", required = true) @RequestParam String orgCodes,
            @ApiParam(value = "维度类型(1:按部门用电,2:按线路用电,3:天然气,4:压缩空气,5:企业用水)", required = true) @RequestParam Integer nowtype) {
        log.info("根据维度获取仪表列表，维度编码：{}，维度类型：{}", orgCodes, nowtype);
        List<ModuleVO> result = energyMonitorService.getModulesByOrgCode(orgCodes, nowtype);
        log.info("查询到仪表数量：{}", result.size());
        return Result.OK(result);
    }

    /**
     * 查询实时数据
     *
     * @param request 查询请求参数
     * @return 实时数据
     */
    @ApiOperation(value = "查询实时数据", notes = "查询指定仪表、参数、时间范围的实时数据")
    @PostMapping("/getRealTimeMonitorData")
    public Result<Object> getRealTimeMonitorData(@Valid @RequestBody RealTimeDataRequest request) {
        log.info("🔍 查询实时数据开始，请求参数：{}", request);
        try {
            Object result = energyMonitorService.getRealTimeMonitorData(request);
            log.info("✅ 查询实时数据成功，返回结果类型：{}", result != null ? result.getClass().getSimpleName() : "null");
            Result<Object> response = Result.OK(result);
            log.info("🔍 Controller返回的Result格式：success={}, code={}, message={}",
                response.isSuccess(), response.getCode(), response.getMessage());
            return response;
        } catch (Exception e) {
            log.error("❌ 查询实时数据失败", e);
            return Result.error("查询实时数据失败: " + e.getMessage());
        }
    }

    /**
     * 导出实时数据到Excel
     *
     * @param request 查询请求参数
     * @param response HTTP响应
     */
    @ApiOperation(value = "导出实时数据到Excel", notes = "导出指定仪表、参数、时间范围的实时数据为Excel文件")
    @PostMapping("/exportRealTimeData")
    public void exportRealTimeData(@Valid @RequestBody RealTimeDataRequest request, HttpServletResponse response) {
        log.info("📊 导出实时数据开始，请求参数：{}", request);
        try {
            energyMonitorService.exportRealTimeData(request, response);
            log.info("✅ 导出实时数据成功");
        } catch (Exception e) {
            log.error("❌ 导出实时数据失败", e);
            throw new RuntimeException("导出实时数据失败: " + e.getMessage());
        }
    }

    /**
     * 调试接口：检查仪表ID和InfluxDB数据
     *
     * @param moduleIds 仪表ID列表（逗号分隔）
     * @return 调试信息
     */
    @ApiOperation(value = "调试接口：检查仪表ID和数据", notes = "用于调试导出问题，检查仪表ID是否正确以及InfluxDB中是否有数据")
    @GetMapping("/debugModuleData")
    public Result<Map<String, Object>> debugModuleData(@RequestParam String moduleIds) {
        log.info("🔍 调试接口被调用，仪表ID：{}", moduleIds);
        try {
            Map<String, Object> debugInfo = energyMonitorService.debugModuleData(moduleIds);
            return Result.OK(debugInfo);
        } catch (Exception e) {
            log.error("❌ 调试接口执行失败", e);
            return Result.error("调试失败: " + e.getMessage());
        }
    }

}