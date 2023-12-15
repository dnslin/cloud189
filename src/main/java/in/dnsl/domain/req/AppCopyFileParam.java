package in.dnsl.domain.req;

import lombok.Data;

@Data
public class AppCopyFileParam {
    private String fileId;
    private String destFileName;
    private String destFolderId;
}
