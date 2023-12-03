package in.dnsl.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SessionDTO {

    @JsonProperty("res_code")
    private Integer resCode;

    @JsonProperty("res_message")
    private String resMessage;

    @JsonProperty("accessToken")
    private String accessToken;

    @JsonProperty("familySessionKey")
    private String familySessionKey;

    @JsonProperty("familySessionSecret")
    private String familySessionSecret;

    @JsonProperty("getFileDiffSpan")
    private Integer getFileDiffSpan;

    @JsonProperty("getUserInfoSpan")
    private Integer getUserInfoSpan;

    @JsonProperty("isSaveName")
    private String isSaveName;

    @JsonProperty("keepAlive")
    private Integer keepAlive;

    @JsonProperty("loginName")
    private String loginName;

    @JsonProperty("refreshToken")
    private String refreshToken;

    @JsonProperty("sessionKey")
    private String sessionKey;

    @JsonProperty("sessionSecret")
    private String sessionSecret;

    private AccessTokenDTO accessTokenDTO;

}
