package org.jeecg.modules.energy.service;

import org.jeecg.modules.energy.vo.reasonable.ReasonableRequest;
import org.jeecg.modules.energy.vo.reasonable.ReasonableResponse;

/**
 * 合理用能 - 服务接口
 */
public interface IReasonableService {
    ReasonableResponse analyze(ReasonableRequest request);
}

