package cn.iocoder.yudao.framework.common.result;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;

/**
 * @program: intellect-callcenter
 * @description: 返回公共类
 * @author: huangyx
 * @create: 2024-01-23 13:56
 **/
public class ResultUtil {

    /**
     * @Description: 成功返回0000
     * @Param: [object, pager]
     * @return:
     * @Author: huangyx
     * @Date: 2024/1/23
     */
    public static CustomResult success() {
        CustomResult result = new CustomResult();
        result.setCode(ResultEnum.SUCCESS.getrespCode());
        result.setMessage(ResultEnum.SUCCESS.getrespDesc());
        return result;
    }

    /**
    * @Description: 成功带空数组
    * @Param: []
    * @return:
    * @Author: huangyx
    * @Date: 2024/1/26
    */
    public static CustomResult successForList() {
        CustomResult result = new CustomResult();
        result.setCode(ResultEnum.SUCCESS.getrespCode());
        result.setMessage(ResultEnum.SUCCESS.getrespDesc());
        result.setData(new ArrayList<>());
        return result;
    }

    /**
    * @Description: 成功带单对象
    * @Param: []
    * @return:
    * @Author: huangyx
    * @Date: 2024/1/26
    */
    public static CustomResult successForObject() {
        CustomResult result = new CustomResult();
        result.setCode(ResultEnum.SUCCESS.getrespCode());
        result.setMessage(ResultEnum.SUCCESS.getrespDesc());
        result.setData(new JSONObject());
        return result;
    }


    /**
     * @Description: 成功返回0000,不带分页
     * @Param: [object, pager]
     * @return:
     * @Author: huangyx
     * @Date: 2024/1/23
     */
    public static CustomResult success(Object object) {
        CustomResult result = new CustomResult();
        result.setCode(ResultEnum.SUCCESS.getrespCode());
        result.setMessage(ResultEnum.SUCCESS.getrespDesc());
        result.setData(object);
        return result;
    }

    /**
    * @Description: 成功返回0000，带分页，有判断
    * @Param: [object, pager]
    * @return:
    * @Author: huangyx
    * @Date: 2024/1/23
    */
    public static CustomResult success(Object object,Pager pager) {
        CustomResult result = new CustomResult();
        result.setCode(ResultEnum.SUCCESS.getrespCode());
        result.setMessage(ResultEnum.SUCCESS.getrespDesc());
        result.setData(object);
        if(pager != null){
            result.setPager(pager);
        }
        return result;
    }
    /**
    * @Description: 成功自定义编码
    * @Param: [code, msg, object, pager]
    * @return:
    * @Author: huangyx
    * @Date: 2024/1/23
    */
    public static CustomResult success(String code, String msg,Object object,Pager pager) {
        CustomResult result = new CustomResult();
        result.setCode(code);
        result.setMessage(msg);
        result.setData(object);
        if(pager != null){
            result.setPager(pager);
        }
        return result;
    }

    /**
     * @Description: 失败自定义错误编码，不带respdata节点
     * @Param: [code, msg]
     * @return:
     * @Author: huangyx
     * @Date: 2024/1/23
     */
    public static CustomResult error(String code, String msg) {
        CustomResult result = new CustomResult();
        result.setCode(code);
        result.setMessage(msg);
        return result;
    }

    /**
    * @Description: 失败自定义错误编码,带返回带respdata节点,是空的
    * @Param: [code, msg]
    * @return:
    * @Author: huangyx
    * @Date: 2024/1/23
    */
    public static CustomResult errorObject(String code, String msg) {
        CustomResult result = new CustomResult();
        result.setCode(code);
        result.setMessage(msg);
        JSONObject back = new JSONObject();
        result.setData(back);
        return result;
    }

    /**
     * @Description: 失败自定义错误编码,带返回信息带respdata节点
     * @Param: [code, msg]
     * @return:
     * @Author: huangyx
     * @Date: 2024/1/23
     */
    public static CustomResult errorList(String code, String msg) {
        CustomResult result = new CustomResult();
        result.setCode(code);
        result.setMessage(msg);
        JSONArray back = new JSONArray();
        result.setData(back);
        return result;
    }
}
