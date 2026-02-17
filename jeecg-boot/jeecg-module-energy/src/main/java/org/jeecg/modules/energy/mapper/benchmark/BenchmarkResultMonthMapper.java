package org.jeecg.modules.energy.mapper.benchmark;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.energy.entity.benchmark.BenchmarkResultMonth;

/**
 * @Description: 能效对标结果月表
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Mapper
public interface BenchmarkResultMonthMapper extends BaseMapper<BenchmarkResultMonth> {
}
