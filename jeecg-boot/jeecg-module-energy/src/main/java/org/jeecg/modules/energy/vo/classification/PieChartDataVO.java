package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 饼图数据VO
 * @author jeecg
 */
@Data
@ApiModel(value = "饼图数据")
public class PieChartDataVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "系列数据")
    private List<SeriesDataVO> series;
    
    @ApiModel(value = "系列数据项")
    public static class SeriesDataVO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "系列名称")
        private String name;
        
        @ApiModelProperty(value = "图表类型")
        private String type;
        
        @ApiModelProperty(value = "数据")
        private List<DataItemVO> data;
        
        // Explicit setter methods for Lombok compatibility
        public void setName(String name) {
            this.name = name;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public void setData(List<DataItemVO> data) {
            this.data = data;
        }
        
        public String getName() {
            return name;
        }
        
        public String getType() {
            return type;
        }
        
        public List<DataItemVO> getData() {
            return data;
        }
    }
    
    @ApiModel(value = "数据项")
    public static class DataItemVO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "数值")
        private Double value;
        
        @ApiModelProperty(value = "名称")
        private String name;
        
        @ApiModelProperty(value = "百分比")
        private Double percentage;
        
        // Explicit setter methods for Lombok compatibility
        public void setValue(Double value) {
            this.value = value;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
        
        public Double getValue() {
            return value;
        }
        
        public String getName() {
            return name;
        }
        
        public Double getPercentage() {
            return percentage;
        }
    }
}