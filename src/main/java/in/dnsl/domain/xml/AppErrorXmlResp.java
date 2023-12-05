package in.dnsl.domain.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("error")
public class AppErrorXmlResp {

    private String code;

    private String message;

}
