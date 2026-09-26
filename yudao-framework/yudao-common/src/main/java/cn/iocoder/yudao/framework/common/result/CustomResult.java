package cn.iocoder.yudao.framework.common.result;

/**
 * @program: intellect-callcenter
 * @description: 返回公共类
 * @author: huangyx
 * @create: 2024-01-23 13:52
 **/

public class CustomResult<T> {
    private String code;
    private String message;
    private T data;
    private Pager pager;

    public CustomResult() {
        super();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Pager getPager() {
        return pager;
    }

    public void setPager(Pager pager) {
        this.pager = pager;
    }
}
