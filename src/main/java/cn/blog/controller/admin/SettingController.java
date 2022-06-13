package cn.blog.controller.admin;

import cn.blog.constant.LogActions;
import cn.blog.constant.WebConst;
import cn.blog.controller.BaseController;
import cn.blog.model.OptionsDomain;
import cn.blog.service.log.LogService;
import cn.blog.service.option.OptionService;
import cn.blog.utils.APIResponse;
import cn.blog.utils.GsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by winterchen on 2022/4/30.
 */
@Api("系统设置")
@Controller
@RequestMapping("/admin/setting")
public class SettingController extends BaseController {

    @Autowired
    private OptionService optionService;

    @Autowired
    private LogService logService;


    @ApiOperation("进入设置页")
    @GetMapping(value = "")
    public String setting(HttpServletRequest request){
        List<OptionsDomain> optionsList = optionService.getOptions();
        Map<String, String> options = new HashMap<>();
        optionsList.forEach((option) -> {
            options.put(option.getName(), option.getValue());
        });
        request.setAttribute("options", options);
        return "admin/setting";
    }


    @ApiOperation("保存系统设置")
    @PostMapping(value = "")
    @ResponseBody
    public APIResponse saveSetting(HttpServletRequest request) {
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            Map<String, String> querys = new HashMap<>();
            parameterMap.forEach((key, value) -> {
                querys.put(key, join(value));
            });
            optionService.saveOptions(querys);

            //刷新设置
            List<OptionsDomain> options = optionService.getOptions();
            if(! CollectionUtils.isEmpty(options)){
                //当存在某个OptionsDomain的value值为空时，会报空指针异常
                //WebConst.initConfig = options.stream().collect(Collectors.toMap(OptionsDomain::getName, OptionsDomain::getValue));
                WebConst.initConfig = options.stream().collect(HashMap::new,(hashMap, optionsDomain) -> hashMap.put(optionsDomain.getName(),optionsDomain.getValue()),HashMap::putAll);
            }
            logService.addLog(LogActions.SYS_SETTING.getAction(), GsonUtils.toJsonString(querys), request.getRemoteAddr(), this.getUid(request));
            return APIResponse.success();
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "保存设置失败";
            return APIResponse.fail(e.getMessage());
        }
    }







}
