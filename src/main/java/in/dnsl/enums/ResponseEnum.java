package in.dnsl.enums;

import lombok.Setter;

public enum ResponseEnum {

    SUCCESS("200","success"),
    SYSTEM_ERROR("500","发生未知异常。。。"),
    SK_BUSY("4001","网络故障"),
    ID_NOTFOUND("4002","状态错误"),
    LIST_ERROR("4003","数据格式异常"),
    COOKIE_NOT_FOUND("4004","Cookie获取失败"),
    TOKEN_INVALID("4005","Token失效"),
    PARAMETERS_ARE_MISSING("4006","参数缺失"),
    PARAMETER_ERROR("4008","参数错误");


    private String code;
    private String message;

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    ResponseEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }
    ResponseEnum() {
    }
}