package in.dnsl.domain.result;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@XStreamAlias("userSignResult")
public class UserSignResult {
    private int result;
    private String resultTip;
    private int activityFlag;
    private String prizeListUrl;
    private String buttonTip;
    private String buttonUrl;
    private String activityTip;
}
