package org.jeecg.modules.energy.service.classification;

import org.jeecg.modules.energy.vo.classification.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 企业分类分区统计服务接口
 * @author jeecg
 */
public interface IEnergyClassificationService {
    
    /**
     * 获取部门树形结构
     * @return 部门树列表
     */
    List<OrgTreeVO> getOrgTree();
    
    /**
     * 获取能源类型列表
     * @return 能源类型列表
     */
    List<EnergyTypeVO> getEnergyTypes();
    
    /**
     * 查询分类分区统计汇总数据
     * @param param 查询参数
     * @return 统计汇总数据
     */
    ClassificationSummaryVO getSummaryData(ClassificationQueryParam param);
    
    /**
     * 查询趋势对比数据
     * @param param 查询参数
     * @return 趋势数据
     */
    TrendDataVO getTrendData(ClassificationQueryParam param);
    
    /**
     * 导出分类分区统计数据
     * @param param 查询参数
     * @param response HTTP响应
     */
    void exportData(ClassificationQueryParam param, HttpServletResponse response);
}