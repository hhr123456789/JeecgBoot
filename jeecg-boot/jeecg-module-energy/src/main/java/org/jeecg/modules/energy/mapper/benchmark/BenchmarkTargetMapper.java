package org.jeecg.modules.energy.mapper.benchmark;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.energy.entity.benchmark.BenchmarkTarget;

/**
 * @Description: 能效对标对象表
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Mapper
public interface BenchmarkTargetMapper extends BaseMapper<BenchmarkTarget> {
}
