package in.dnsl.domain.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("folder")
public class AppGetFolderInfoNode {

    private String fid;

    private String fname;
}
