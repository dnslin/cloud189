package in.dnsl.domain.xml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileSystemEntity {
    private String id;
    private String parentId;
    private String name;
    private long size;
    private String md5;
    private int starLabel;
    private int fileCata;
    private String lastOpTime;
    private String createDate;
    private int mediaType;
    private String rev;
    private boolean isFolder;
    private int subFileCount; // 仅对文件夹有效
    @Builder.Default
    private List<FileSystemEntity> children = new ArrayList<>();


    // 添加子实体（文件或文件夹）
    public void addChild(FileSystemEntity entity) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(entity);
    }

}