package in.dnsl.domain.dto;

import lombok.Data;

@Data
public class ParamsDTO {

    public String captchaToken;

    public String lt;

    public String returnUrl;

    public String paramId;

    public String reqId;

    public String jRsaKey;
}
