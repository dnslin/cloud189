package in.dnsl.domain.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@XStreamAlias("listFiles")
@Getter
@Setter
public class ListFiles {
    private String lastRev;
    private FileList fileList;

    @Getter
    @Setter
    class FileList {
        private int count;

        @XStreamImplicit(itemFieldName = "folder")
        private List<Folder> folder;

        @XStreamImplicit(itemFieldName = "file")
        private List<File> file;
    }

    @XStreamAlias("folder")
    @Getter
    @Setter
    static class Folder {
        private String id;
        private String parentId;
        private String name;
        private String createDate;
        private int starLabel;
        private String lastOpTime;
        private String rev;
        private int fileCount;
        private int fileCata;
    }

    @XStreamAlias("file")
    @Getter
    @Setter
    static class File {
        private String id;
        private String name;
        private long size;
        private String md5;
        private int starLabel;
        private int fileCata;
        private String lastOpTime;
        private String createDate;
        private int mediaType;
        private String rev;
    }
}
