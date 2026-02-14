package org.jeecg.modules.energy.vo.teamenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: 班组信息VO
 * @Author: jeecg-boot
 * @Date: 2026-01-24
 * @Version: V1.0
 */
@Data
@ApiModel(value="TeamInfoVO", description="班组信息VO")
public class TeamInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "班组编码")
    private String code;

    @ApiModelProperty(value = "班组名称")
    private String name;

    @ApiModelProperty(value = "班次类型")
    private String shiftType;

    @ApiModelProperty(value = "所属组织编码")
    private String orgCode;

    @ApiModelProperty(value = "所属组织名称")
    private String orgName;

    @ApiModelProperty(value = "状态")
    private Integer status;
}
