package org.jeecg.modules.energy.service.impl;

import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.config.TenantContext;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.SymbolConstant;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.config.mybatis.MybatisPlusSaasConfig;
import org.jeecg.modules.energy.service.IEmsDimensionService;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.mapper.SysDepartMapper;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.jeecg.modules.system.util.FindsDepartsChildrenUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmsDimensionServiceImpl extends ServiceImpl<SysDepartMapper, SysDepart> implements IEmsDimensionService {
    /**
     * <p>
     * 根据关键字搜索相关的部门数据
     * </p>
     */
    @Override
    public List<SysDepartTreeModel> searchByDepartType(String keyWord, String myDeptSearch, String departIds) {
        //根据部门id获取所负责部门
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        String[] codeArr = this.getMyDeptParentOrgCode(departIds);
        //update-begin---author:wangshuai---date:2023-12-01---for:【QQYUN-7320】查询部门没数据，导致报错空指针---
        if(ArrayUtil.isEmpty(codeArr)){
            return null;
        }
        //update-end---author:wangshuai---date:2023-12-01---for:【QQYUN-7320】查询部门没数据，导致报错空指针---
        // for(int i=0;i<codeArr.length;i++){
        //query.or().likeRight(SysDepart::getOrgCode,codeArr[i]);
        //}
        //query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        query.like(SysDepart::getOrgCode,keyWord);

        //------------------------------------------------------------------------------------------------
        //是否开启系统管理模块的 SASS 控制
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------

        query.orderByAsc(SysDepart::getDepartOrder);
        //将父节点ParentId设为null
        List<SysDepart> listDepts = this.list(query);
        for(int i=0;i<codeArr.length;i++){
            for(SysDepart dept : listDepts){
                if(dept.getOrgCode().equals(codeArr[i])){
                    dept.setParentId(null);
                }
            }
        }
        // 调用wrapTreeDataToTreeList方法生成树状数据
        List<SysDepartTreeModel> listResult = FindsDepartsChildrenUtil.wrapTreeDataToTreeList(listDepts);
        return listResult;
    }

    /**
     * 根据用户所负责部门ids获取父级部门编码
     * @param departIds
     * @return
     */
    private String[] getMyDeptParentOrgCode(String departIds){
        //根据部门id查询所负责部门
        LambdaQueryWrapper<SysDepart> query = new LambdaQueryWrapper<SysDepart>();
        query.eq(SysDepart::getDelFlag, CommonConstant.DEL_FLAG_0.toString());
        if(oConvertUtils.isNotEmpty(departIds)){
            query.in(SysDepart::getId, Arrays.asList(departIds.split(",")));
        }

        //------------------------------------------------------------------------------------------------
        //是否开启系统管理模块的多租户数据隔离【SAAS多租户模式】
        if(MybatisPlusSaasConfig.OPEN_SYSTEM_TENANT_CONTROL){
            query.eq(SysDepart::getTenantId, oConvertUtils.getInt(TenantContext.getTenant(), 0));
        }
        //------------------------------------------------------------------------------------------------
        query.orderByAsc(SysDepart::getOrgCode);
        List<SysDepart> list = this.list(query);
        //查找根部门
        if(list == null || list.size()==0){
            return null;
        }
        String orgCode = this.getMyDeptParentNode(list);
        String[] codeArr = orgCode.split(",");
        return codeArr;
    }


    /**
     * 获取负责部门父节点
     * @param list
     * @return
     */
    private String getMyDeptParentNode(List<SysDepart> list){
        Map<String,String> map = new HashMap(5);
        //1.先将同一公司归类
        for(SysDepart dept : list){
            String code = dept.getOrgCode().substring(0,3);
            if(map.containsKey(code)){
                String mapCode = map.get(code)+","+dept.getOrgCode();
                map.put(code,mapCode);
            }else{
                map.put(code,dept.getOrgCode());
            }
        }
        StringBuffer parentOrgCode = new StringBuffer();
        //2.获取同一公司的根节点
        for(String str : map.values()){
            String[] arrStr = str.split(",");
            parentOrgCode.append(",").append(this.getMinLengthNode(arrStr));
        }
        return parentOrgCode.substring(1);
    }

    /**
     * 获取同一公司中部门编码长度最小的部门
     * @param str
     * @return
     */
    private String getMinLengthNode(String[] str){
        int min =str[0].length();
        StringBuilder orgCodeBuilder = new StringBuilder(str[0]);
        for(int i =1;i<str.length;i++){
            if(str[i].length()<=min){
                min = str[i].length();
                orgCodeBuilder.append(SymbolConstant.COMMA).append(str[i]);
            }
        }
        return orgCodeBuilder.toString();
    }

}
