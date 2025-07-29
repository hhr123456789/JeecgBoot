package org.jeecg.modules.energy.vo.monitor;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @Description: 实时数据查询请求VO
 * @Author: jeecg-boot
 * @Date: 2025-07-16
 * @Version: V1.0
 */
@Data
@ApiModel(value = "实时数据查询请求VO", description = "实时数据查询请求VO")
public class RealTimeDataRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    /**仪表编号列表*/
    @ApiModelProperty(value = "仪表编号列表", required = true)
    @NotEmpty(message = "仪表编号列表不能为空")
    private List<String> moduleIds;

    /**参数编号列表*/
    @ApiModelProperty(value = "参数编号列表 (1=A相电流,2=B相电流,3=C相电流,4=A相电压,5=B相电压,6=C相电压,7=总有功功率,8=总无功功率,9=总视在功率,10=功率因数,11=频率,12=正向有功总电能)", required = true)
    @NotEmpty(message = "参数编号列表不能为空")
    private List<Integer> parameters;

    /**开始时间*/
    @ApiModelProperty(value = "开始时间 (格式: yyyy-MM-dd HH:mm:ss)", required = true, example = "2025-07-15 08:00:00")
    @NotNull(message = "开始时间不能为空")
    private String startTime;

    /**结束时间*/
    @ApiModelProperty(value = "结束时间 (格式: yyyy-MM-dd HH:mm:ss)", required = true, example = "2025-07-15 16:00:00")
    @NotNull(message = "结束时间不能为空")
    private String endTime;

    /**查询间隔*/
    @ApiModelProperty(value = "查询间隔 (1=15分钟,2=30分钟,3=60分钟,4=120分钟)", required = true)
    @NotNull(message = "查询间隔不能为空")
    private Integer interval;

    /**显示方式*/
    @ApiModelProperty(value = "显示方式 (1=统一显示,2=分开显示)", required = true)
    @NotNull(message = "显示方式不能为空")
    private Integer displayMode;

    /**文件名*/
    @ApiModelProperty(value = "导出文件名 (可选，默认为'实时数据导出')", required = false, example = "中电电气实时数据导出")
    private String fileName;
}
