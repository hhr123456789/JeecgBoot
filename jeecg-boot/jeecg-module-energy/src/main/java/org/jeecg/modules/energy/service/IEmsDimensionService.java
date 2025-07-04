package org.jeecg.modules.energy.service;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import java.util.List;
/**
 * <p>
 * 部门表 服务实现类
 * <p>
 *
 * @Author:Steve
 * @Since：   2025-06-28
 */
public interface IEmsDimensionService extends IService<SysDepart> {
    /**
     * 根据关键字搜索相关的部门数据
     * @param keyWord
     * @param myDeptSearch
     * @param departIds 多个部门id
     * @return
     */
    List<SysDepartTreeModel> searchByDepartType(String keyWord, String myDeptSearch, String departIds);
}
