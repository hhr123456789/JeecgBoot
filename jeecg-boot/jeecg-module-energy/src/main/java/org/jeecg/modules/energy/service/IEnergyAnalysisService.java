package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.analysis.CompareDataRequest;
import org.jeecg.modules.energy.vo.analysis.CompareDataVO;
import org.jeecg.modules.energy.vo.analysis.ModuleVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 能源分析对比 - 服务接口
 */
public interface IEnergyAnalysisService {

    /**
     * 根据维度获取仪表列表（右侧仪表下拉）
     */
    List<ModuleVO> getModulesByDimension(String orgCode, Integer energyType, Boolean includeChildren);

    /**
     * 查询对比数据
     */
    CompareDataVO getCompareData(CompareDataRequest request);

    /**
     * 导出Excel
     */
    void exportCompareData(CompareDataRequest request, HttpServletResponse response);
}

