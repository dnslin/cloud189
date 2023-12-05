package in.dnsl.domain.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XStreamAlias("userSession")
public class UserSession {

    private String loginName;

    private String sessionKey;
    
    private String sessionSecret;
    
    private int keepAlive;
    
    private int getFileDiffSpan;
    
    private int getUserInfoSpan;
    
    private String familySessionKey;
    
    private String familySessionSecret;


}