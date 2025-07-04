package org.jeecg.modules.energy.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.energy.service.IEmsDimensionService;
import org.jeecg.modules.system.model.SysDepartTreeModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/energy/emsDimension")
@Slf4j
public class EmsDimensionController {
    @Autowired
    private IEmsDimensionService EmsDimensionService;
    /**
     * 查询数据 查出我的維度,并以树结构数据格式响应给前端
     *
     * @return
     */
    @RequestMapping(value = "/queryMyDimensionTreeList", method = RequestMethod.GET)
    public Result<List<SysDepartTreeModel>> queryMyDimensionTreeList(@RequestParam(name = "keyWord", required = false) Integer typenow) {
        Result<List<SysDepartTreeModel>> result = new Result<>();
        LoginUser user = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        String keywordnow="";

        if(typenow==1){
            keywordnow="A02A02";
        }else if(typenow==2){
            keywordnow="A02A03";
        }else if(typenow==3){
            keywordnow="A02A04";
        }else if(typenow==4){
            keywordnow="A02A05";
        }else if(typenow==5){
            keywordnow="A02A06";
        }else{
            keywordnow="A02A02";
        }
        try {
            if(oConvertUtils.isNotEmpty(user.getUserIdentity()) && user.getUserIdentity().equals( CommonConstant.USER_IDENTITY_2 )){
                //update-begin--Author:liusq  Date:20210624  for:部门查询ids为空后的前端显示问题 issues/I3UD06
                String departIds = user.getDepartIds();
                if(StringUtils.isNotBlank(departIds)){
                    List<SysDepartTreeModel> list = EmsDimensionService.searchByDepartType(keywordnow,"",departIds);
                    result.setResult(list);
                }
                //update-end--Author:liusq  Date:20210624  for:部门查询ids为空后的前端显示问题 issues/I3UD06
                result.setMessage(CommonConstant.USER_IDENTITY_2.toString());
                result.setSuccess(true);
            }else{
                result.setMessage(CommonConstant.USER_IDENTITY_1.toString());
                result.setSuccess(true);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return result;
    }
}
