package in.dnsl.domain.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Data;

@Data
@XStreamAlias("folderInfo")
public class AppGetFileInfoResult {

    // 文件ID
    private String id;

    // 父文件ID
    private String parentFolderId;

    // 文件名 or 文件夹名称
    private String name;

    private String createDate;

    private String lastOpTime;

    private String path;

    private String rev;

    private ParentFolderListNode parentFolderList;

    private String groupSpaceId;

}
