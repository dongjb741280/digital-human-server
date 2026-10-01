package cn.iocoder.yudao.module.digital.service;

import cn.iocoder.yudao.module.digital.framework.file.config.DigitalAbilityConfig;
import cn.iocoder.yudao.module.digital.util.AbilityShareClient;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: ai_digital
 * @description: 调用python
 * @author: huangyx
 * @create: 2024-07-21 11:37
 **/
@Service("callPythonService")
@Slf4j
public class CallPythonService {
    @Resource
    private DigitalAbilityConfig digitalAbilityConfig;

    @Async("callPythonInterFaceExecutor")
    public void callPythonChangeImega(JSONObject request) {
        String url = digitalAbilityConfig.getChangeViedoAndImageUrl().get("changeimageUrl");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPost(url, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("callPythonChangeImega:{}",e);
        }
    }

    @Async("callPythonInterFaceExecutor")
    public void callPythonChangeImegaAndViedo(JSONObject request) {
        String url = digitalAbilityConfig.getChangeViedoAndImageUrl().get("changeAllUrl");
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            AbilityShareClient.doPost(url, headers, JSONObject.toJSONString(request));
        } catch (Exception e) {
            log.error("callPythonChangeImegaAndViedo:",e);
        }
    }

    public JSONObject callToPPtPythonGet(JSONObject request,String interName) {
        JSONObject result = null;
        String url = digitalAbilityConfig.getAbigetpptUrl()+interName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            String s = AbilityShareClient.doGet(url, headers);
            result = JSONObject.parseObject(s);
        } catch (Exception e) {
            log.error("callToPPtPythonGet:{}",e);

        }
        return result;
    }

    public JSONObject callToPPtPythonPost(JSONObject request,String interName) {
        JSONObject result = null;
        String url = digitalAbilityConfig.getAbigetpptUrl()+interName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            String s = AbilityShareClient.doPostPPt(url, headers,JSONObject.toJSONString(request));
            result = JSONObject.parseObject(s);
        } catch (Exception e) {
            log.error("callToPPtPythonPost:{}",e);

        }
        return result;
    }

    /**
     * 调用 ppt 相关 python 接口（长超时，ppt-master 生成是分钟级）
     */
    public JSONObject callToPPtPythonPostLong(JSONObject request,String interName) {
        JSONObject result = null;
        String url = digitalAbilityConfig.getAbigetpptUrl()+interName;
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            String s = AbilityShareClient.doPostPPtLong(url, headers,JSONObject.toJSONString(request));
            result = JSONObject.parseObject(s);
        } catch (Exception e) {
            log.error("callToPPtPythonPostLong:{}",e);

        }
        return result;
    }

    /**
     * 异步处理 ppt 转图片
     * @param request
     * @return
     */
    @Async("callPythonInterFaceExecutor")
    public JSONObject pptToImagesPythonPost(JSONObject request) {
        JSONObject result = null;
        String url = digitalAbilityConfig.getPpttoimage();
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json;charset=UTF-8");  //传参格式
        headers.put("Accept", "application/json");
        try {
            String s = AbilityShareClient.doPostPPt(url, headers,JSONObject.toJSONString(request));
            result = JSONObject.parseObject(s);
        } catch (Exception e) {
            log.error("pptToImagesPythonPost:{}",e);
        }
        return result;
    }
}
