package in.dnsl.domain.req;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppGetFileInfoParam {

    // 家庭云ID
    @Builder.Default
    private int familyId = 0;

    // FileId 文件ID，支持文件和文件夹
    @Builder.Default
    private String fileId = "";

    // FilePath 文件绝对路径，支持文件和文件夹
    @Builder.Default
    private String filePath = "";
}
