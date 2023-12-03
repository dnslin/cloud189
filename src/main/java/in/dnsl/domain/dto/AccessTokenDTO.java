package in.dnsl.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AccessTokenDTO {

    @JsonProperty("expiresIn")
    private Long expiresIn;

    @JsonProperty("accessToken")
    private String accessToken;
}
