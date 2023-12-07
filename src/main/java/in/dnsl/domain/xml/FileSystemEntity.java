package in.dnsl.domain.xml;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FileSystemEntity {
    private String id;
    private String name;
    private boolean isFolder;
    private List<FileSystemEntity> children;

    // 构造函数
    public FileSystemEntity(String id, String name, boolean isFolder) {
        this.id = id;
        this.name = name;
        this.isFolder = isFolder;
        this.children = new ArrayList<>();
    }

    // 添加子实体（文件或文件夹）
    public void addChild(FileSystemEntity entity) {
        this.children.add(entity);
    }

}