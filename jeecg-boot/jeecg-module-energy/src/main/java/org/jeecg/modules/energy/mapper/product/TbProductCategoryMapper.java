package org.jeecg.modules.energy.mapper.product;

import org.jeecg.modules.energy.entity.product.TbProductCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @Description: 产品分类Mapper接口
 * @Author: jeecg-boot
 * @Date: 2026-02-16
 * @Version: V1.0
 */
public interface TbProductCategoryMapper extends BaseMapper<TbProductCategory> {

    /**
     * 查询所有产品分类树
     */
    @Select("SELECT " +
            "id, " +
            "parent_id as parentId, " +
            "category_code as `key`, " +
            "category_name as title, " +
            "is_leaf as isLeaf " +
            "FROM tb_product_category " +
            "WHERE status = 1 " +
            "ORDER BY sort_order ASC")
    List<Map<String, Object>> getCategoryTree();
}
