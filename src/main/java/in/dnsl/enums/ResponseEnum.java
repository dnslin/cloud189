package in.dnsl.enums;

public enum ResponseEnum {

    SUCCESS("200","success"),
    SYSTEM_ERROR("500","发生未知异常。。。");

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