package cn.iocoder.yudao.framework.common.result;

/**
 * @program: intellect-callcenter
 * @description: 返回公共类
 * @author: huangyx
 * @create: 2024-01-23 13:57
 **/
public enum ResultEnum {
    //定义返回值内容
    SUCCESS("0000","处理成功"),
    ERROR("9999","处理失败"),
    UNKNOWN_ERROR("9998","未知错误")
    ;
    private String respCode;
    private String respDesc;

    ResultEnum(String respCode, String respDesc) {
        this.respCode = respCode;
        this.respDesc = respDesc;
    }

    public String getrespCode() {
        return respCode;
    }

    public String getrespDesc() {
        return respDesc;
    }
}
