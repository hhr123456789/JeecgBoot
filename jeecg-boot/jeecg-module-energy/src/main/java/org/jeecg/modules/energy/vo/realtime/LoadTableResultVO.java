package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Description: 负荷表格结果VO
 * @Author: jeecg-boot
 * @Date: 2025-07-26
 * @Version: V1.0
 */
@Data
@ApiModel(value = "LoadTableResultVO", description = "负荷表格结果")
public class LoadTableResultVO {
    
    @ApiModelProperty(value = "表格数据")
    private List<LoadStatisticsRowVO> tableData;
    
    @ApiModelProperty(value = "分页信息")
    private PaginationVO pagination;
    
    @ApiModelProperty(value = "汇总信息")
    private LoadTableSummaryVO summary;
    
    /**
     * 负荷统计行数据VO
     */
    @Data
    @ApiModel(value = "LoadStatisticsRowVO", description = "负荷统计行数据")
    public static class LoadStatisticsRowVO {
        
        @ApiModelProperty(value = "序号")
        private Integer 序号;
        
        @ApiModelProperty(value = "设备名称")
        private String 设备名称;
        
        @ApiModelProperty(value = "最大功率(kW)")
        private Double 最大功率;
        
        @ApiModelProperty(value = "最大功率率(%)")
        private Double 最大功率率;
        
        @ApiModelProperty(value = "最大功率发生时间")
        private String 最大功率发生时间;
        
        @ApiModelProperty(value = "最小功率(kW)")
        private Double 最小功率;
        
        @ApiModelProperty(value = "最小功率率(%)")
        private Double 最小功率率;
        
        @ApiModelProperty(value = "最小功率发生时间")
        private String 最小功率发生时间;
        
        @ApiModelProperty(value = "平均功率(kW)")
        private Double 平均功率;
        
        @ApiModelProperty(value = "平均功率率(%)")
        private Double 平均功率率;
    }
    
    /**
     * 分页信息VO
     */
    @Data
    @ApiModel(value = "PaginationVO", description = "分页信息")
    public static class PaginationVO {
        
        @ApiModelProperty(value = "总记录数")
        private Integer total;
        
        @ApiModelProperty(value = "当前页码")
        private Integer pageNum;
        
        @ApiModelProperty(value = "每页条数")
        private Integer pageSize;
        
        @ApiModelProperty(value = "总页数")
        private Integer pages;
    }
    
    /**
     * 负荷表格汇总信息VO
     */
    @Data
    @ApiModel(value = "LoadTableSummaryVO", description = "负荷表格汇总信息")
    public static class LoadTableSummaryVO {
        
        @ApiModelProperty(value = "总仪表数")
        private Integer totalModules;
        
        @ApiModelProperty(value = "时间范围")
        private String timeRange;
        
        @ApiModelProperty(value = "数据类型")
        private String dataType;
    }
}
