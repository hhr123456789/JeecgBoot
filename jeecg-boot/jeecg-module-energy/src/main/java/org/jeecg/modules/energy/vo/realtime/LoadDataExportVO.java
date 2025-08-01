package org.jeecg.modules.energy.vo.realtime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * @Description: 负荷数据导出参数VO
 * @Author: jeecg-boot
 * @Date: 2025-07-31
 * @Version: V1.0
 */
@Data
@ApiModel(value = "负荷数据导出参数", description = "负荷数据导出参数")
public class LoadDataExportVO {

    @ApiModelProperty(value = "文件名称", required = true)
    @NotBlank(message = "文件名称不能为空")
    private String fileName;

    @ApiModelProperty(value = "仪表ID列表", required = true)
    @NotEmpty(message = "仪表ID列表不能为空")
    private List<String> moduleIds;

    @ApiModelProperty(value = "查询日期", required = true, example = "2025-07-31")
    @NotBlank(message = "查询日期不能为空")
    @Pattern(regexp = "^\\d{4}(-\\d{1,2}){0,2}$", message = "日期格式不正确，支持格式：yyyy、yyyy-MM、yyyy-MM-dd")
    private String queryDate;

    @ApiModelProperty(value = "时间粒度", required = true, example = "day", allowableValues = "day,month,year")
    @NotBlank(message = "时间粒度不能为空")
    @Pattern(regexp = "^(day|month|year)$", message = "时间粒度只能是day、month或year")
    private String timeGranularity;
}