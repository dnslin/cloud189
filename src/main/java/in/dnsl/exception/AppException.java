package in.dnsl.exception;

import in.dnsl.enums.ResponseEnum;
import lombok.Data;

@Data
@SuppressWarnings("Lombok")
public class AppException extends RuntimeException{

    private String code;
    private String message;

    public AppException(ResponseEnum responseEnum){
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }

    public AppException(String message){
        this.code = "500";
        this.message = message;
    }
}