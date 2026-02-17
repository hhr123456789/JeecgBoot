package org.jeecg.modules.energy.vo.processenergy;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 生产线树节点VO
 */
@Data
@ApiModel(value = "生产线树节点", description = "生产线树节点")
public class ProductionLineTreeVO {

    @ApiModelProperty(value = "节点标题")
    private String title;

    @ApiModelProperty(value = "节点key")
    private String key;

    @ApiModelProperty(value = "子节点")
    private List<ProductionLineTreeVO> children;
}
