package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 部门树VO
 * @author jeecg
 */
@Data
@ApiModel(value = "部门树")
public class OrgTreeVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "ID")
    private String id;
    
    @ApiModelProperty(value = "部门编码")
    private String orgCode;
    
    @ApiModelProperty(value = "部门名称")
    private String orgName;
    
    @ApiModelProperty(value = "父级ID")
    private String parentId;
    
    @ApiModelProperty(value = "子节点")
    private List<OrgTreeVO> children;
}