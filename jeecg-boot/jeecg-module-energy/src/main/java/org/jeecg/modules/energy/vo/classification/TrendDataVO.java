package org.jeecg.modules.energy.vo.classification;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 趋势数据VO
 * @author jeecg
 */
@Data
@ApiModel(value = "趋势数据")
public class TrendDataVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @ApiModelProperty(value = "X轴数据")
    private XAxisDataVO xAxis;
    
    @ApiModelProperty(value = "系列数据")
    private List<SeriesDataVO> series;
    
    @ApiModel(value = "X轴数据")
    public static class XAxisDataVO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "类型")
        private String type;
        
        @ApiModelProperty(value = "数据")
        private List<String> data;
        
        // Explicit setter methods for Lombok compatibility
        public void setType(String type) {
            this.type = type;
        }
        
        public void setData(List<String> data) {
            this.data = data;
        }
        
        public String getType() {
            return type;
        }
        
        public List<String> getData() {
            return data;
        }
    }
    
    @ApiModel(value = "系列数据项")
    public static class SeriesDataVO implements Serializable {
        private static final long serialVersionUID = 1L;
        
        @ApiModelProperty(value = "名称")
        private String name;
        
        @ApiModelProperty(value = "类型")
        private String type;
        
        @ApiModelProperty(value = "数据")
        private List<Number> data;
        
        // Explicit setter methods for Lombok compatibility
        public void setName(String name) {
            this.name = name;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public void setData(List<Number> data) {
            this.data = data;
        }
        
        public String getName() {
            return name;
        }
        
        public String getType() {
            return type;
        }
        
        public List<Number> getData() {
            return data;
        }
    }
}