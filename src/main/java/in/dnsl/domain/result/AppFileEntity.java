package in.dnsl.domain.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppFileEntity {

    private String fileId;

    private String parentId;

    private String fileMd5;

    private String fileName;

    private long fileSize;

    private String lastOpTime;

    private String createTime;

    private String path;

    private int mediaType;

    private boolean isFolder;

    private int subFileCount;

    private int startLabel;

    private int favoriteLabel;

    private int orientation;

    private String rev;

    private int fileCata;
}