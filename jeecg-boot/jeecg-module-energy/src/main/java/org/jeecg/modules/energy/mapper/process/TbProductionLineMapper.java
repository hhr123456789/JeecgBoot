package org.jeecg.modules.energy.mapper.process;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.energy.entity.process.TbProductionLine;

import java.util.List;

/**
 * @Description: 生产线/工序配置表Mapper
 * @Author: jeecg-boot
 * @Date: 2026-02-17
 * @Version: V1.0
 */
@Mapper
public interface TbProductionLineMapper extends BaseMapper<TbProductionLine> {

    /**
     * 查询所有启用的生产线/工序
     */
    List<TbProductionLine> selectAllEnabled();

    /**
     * 根据父ID查询子节点
     */
    List<TbProductionLine> selectByParentId(@Param("parentId") String parentId);

    /**
     * 根据编码查询
     */
    TbProductionLine selectByLineCode(@Param("lineCode") String lineCode);
}
