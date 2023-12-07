package in.dnsl.logic;

import in.dnsl.domain.req.AppFileListParam;
import in.dnsl.domain.req.AppGetFileInfoParam;
import in.dnsl.domain.result.AppFileEntity;
import in.dnsl.domain.xml.AppErrorXmlResp;
import in.dnsl.domain.xml.AppGetFileInfoResult;
import in.dnsl.domain.xml.FileSystemEntity;
import in.dnsl.domain.xml.ListFiles;
import in.dnsl.enums.OrderEnums;
import in.dnsl.utils.XmlUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.kuku.utils.OkHttpUtils;
import okhttp3.Headers;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static in.dnsl.constant.ApiConstant.API_URL;
import static in.dnsl.constant.ApiConstant.rootNode;
import static in.dnsl.logic.CloudLogin.getSession;
import static in.dnsl.utils.ApiUtils.*;
import static in.dnsl.utils.SignatureUtils.signatureOfHmac;

@Slf4j
public class FileDirectory {

    //根据文件ID或者文件绝对路径获取文件信息，支持文件和文件夹
    public static AppGetFileInfoResult appGetBasicFileInfo(@NotNull AppGetFileInfoParam build) {
        if (build.getFamilyId() > 0 && "-11".equals(build.getFileId())) build.setFileId("");
        if (build.getFilePath().isBlank() && build.getFileId().isBlank()) build.setFilePath("/");
        var session = getSession();
        String sessionKey, sessionSecret, fullUrlPattern;
        Object[] formatArgs;
        if (build.getFamilyId() >= 0) {
            // 个人云逻辑
            sessionKey = session.getSessionKey();
            sessionSecret = session.getSessionSecret();
            fullUrlPattern = "%s/getFolderInfo.action?folderId=%s&folderPath=%s&pathList=0&dt=3&%s";
            formatArgs = new Object[]{API_URL, build.getFileId(), urlEncode(build.getFilePath()), PcClientInfoSuffixParam()};
        } else {
            // 家庭云逻辑
            sessionKey = session.getFamilySessionKey();
            sessionSecret = session.getFamilySessionSecret();
            if (build.getFileId().isEmpty()) throw new RuntimeException("FileId为空");
            fullUrlPattern = "%s/family/file/getFolderInfo.action?familyId=%d&folderId=%s&folderPath=%s&pathList=0&%s";
            formatArgs = new Object[]{API_URL, build.getFamilyId(), build.getFileId(), urlEncode(build.getFilePath()), PcClientInfoSuffixParam()};
        }
        var xmlData = send(fullUrlPattern, formatArgs, sessionKey, sessionSecret);
        if (xmlData.contains("error")) {
            var appErrorXmlResp = XmlUtils.xmlToObject(xmlData, AppErrorXmlResp.class);
            throw new RuntimeException("请求失败:" + appErrorXmlResp.getCode());
        }
        return XmlUtils.xmlToObject(xmlData, AppGetFileInfoResult.class);
    }

    // 获取指定目录下的所有文件列表
    @SneakyThrows
    public static ListFiles appGetAllFileList(@NotNull AppFileListParam param) {
        // 参数校验
        if (param.getPageSize() <= 0) param.setPageSize(200);
        if (param.getFamilyId() > 0 && "-11".equals(param.getFileId())) param.setFileId("");
        // 获取初始文件列表
        var files = appFileList(param);
        if (files == null) throw new RuntimeException("文件列表为空");
        var fileList = files.getFileList();
        int totalFilesCount = fileList.getCount();
        // 检查是否需要分页
        if (totalFilesCount > param.getPageSize()) {
            int pageNum = (int) Math.ceil((double) totalFilesCount / param.getPageSize());
            for (int i = 2; i <= pageNum; i++) {
                param.setPageNum(i);
                var additionalFiles = appFileList(param).getFileList();
                if (additionalFiles != null) {
                    if (additionalFiles.getFile() != null) fileList.getFile().addAll(additionalFiles.getFile());
                    if (additionalFiles.getFolder() != null) fileList.getFolder().addAll(additionalFiles.getFolder());
                }
                TimeUnit.MILLISECONDS.sleep(100);
            }
        }
        return files;
    }


    //获取文件列表
    public static ListFiles appFileList(@NotNull AppFileListParam param) {
        Object[] formatArgs;
        var session = getSession();
        String sessionKey, sessionSecret, fullUrlPattern;
        if (param.getFamilyId() <= 0) {
            sessionKey = session.getSessionKey();
            sessionSecret = session.getSessionSecret();
            fullUrlPattern = "%s/listFiles.action?folderId=%s&recursive=0&fileType=0&iconOption=10&mediaAttr=0&orderBy=%s&descending=%s&pageNum=%s&pageSize=%s&%s";
            formatArgs = new Object[]{API_URL, param.getFileId(), OrderEnums.getByCode(param.getOrderBy()), false, param.getPageNum(), param.getPageSize(), PcClientInfoSuffixParam()};
        } else {
            // 家庭云
            if (rootNode.equals(param.getFileId())) param.setFileId("");
            sessionKey = session.getFamilySessionKey();
            sessionSecret = session.getFamilySessionSecret();
            fullUrlPattern = "%s/family/file/listFiles.action?folderId=%s&familyId=%s&fileType=0&iconOption=0&mediaAttr=0&orderBy=%s&descending=%s&pageNum=%d&pageSize=%d&%s";
            formatArgs = new Object[]{API_URL, param.getFileId(), param.getFamilyId(), OrderEnums.getByCode(param.getOrderBy()), false, param.getPageNum(), param.getPageSize(), PcClientInfoSuffixParam()};
        }
        var send = send(fullUrlPattern, formatArgs, sessionKey, sessionSecret);
        return XmlUtils.xmlToObject(send, ListFiles.class);
    }

    // 通过FileId获取文件的绝对路径
    @SneakyThrows
    public static String appFilePathById(Integer familyId, String fileId) {
        var fullPath = "";
        var param = AppGetFileInfoParam.builder()
                .familyId(familyId)
                .fileId(fileId).build();
        while (true) {
            var fi = appGetBasicFileInfo(param);
            if (fi == null) throw new RuntimeException("FileInfo is null");
            if (!fi.getPath().isEmpty()) return fi.getPath();
            if (fi.getId().startsWith("-") || fi.getParentFolderId().startsWith("-")) {
                fullPath = "/" + fullPath;
                break;
            }
            fullPath = fullPath.isEmpty() ? fi.getName() : fi.getName() + "/" + fullPath;

            param = AppGetFileInfoParam.builder()
                    .fileId(fi.getParentFolderId()).build();
            TimeUnit.MILLISECONDS.sleep(100);
        }
        return fullPath;
    }

    // 通过FileId获取文件详情
    public static AppFileEntity appFileInfoById(Integer familyId, String fileId) {
        var param = AppGetFileInfoParam.builder()
                .familyId(familyId)
                .fileId(fileId).build();
        var result = appGetBasicFileInfo(param);
        var build = AppFileListParam.builder()
                .fileId(result.getParentFolderId())
                .familyId(familyId).build();
        var files = appGetAllFileList(build);
        var file = files.getFileList().getFile();
        if (file == null) throw new RuntimeException("文件列表为空");
        var collect = convert(file, result.getPath(), result.getParentFolderId()).stream().filter(e -> e.getFileId().equals(fileId)).toList();
        if (collect.isEmpty()) throw new RuntimeException("文件不存在");
        return collect.getFirst();
    }



    // 递归获取文件夹下的所有文件 包括子文件夹
    public static FileSystemEntity appFileListByPath(Integer familyId, String path) {
        AppGetFileInfoParam fileInfoParam = AppGetFileInfoParam.builder()
                .filePath(path).familyId(familyId).build();
        AppGetFileInfoResult fileInfoResult = appGetBasicFileInfo(fileInfoParam);
        if (fileInfoResult == null) throw new RuntimeException("文件不存在");

        String folderId = fileInfoResult.getId();
        AppFileListParam fileListParam = AppFileListParam.builder()
                .fileId(folderId).familyId(familyId).build();
        ListFiles listFiles = appGetAllFileList(fileListParam);

        FileSystemEntity rootEntity = new FileSystemEntity(folderId, path, true);
        processFileList(familyId, rootEntity, listFiles.getFileList(), path);
        return rootEntity;
    }

    private static void processFileList(Integer familyId, FileSystemEntity parentEntity, ListFiles.FileList fileList, String parentPath) {
        if (fileList == null) return;

        // 处理子文件夹
        if (fileList.getFolder() != null) {
            for (ListFiles.Folder folder : fileList.getFolder()) {
                String subFolderPath = parentPath + "/" + folder.getName();
                FileSystemEntity subFolderEntity = appFileListByPath(familyId, subFolderPath);
                parentEntity.addChild(subFolderEntity);
            }
        }

        // 处理文件
        if (fileList.getFile() != null) {
            for (ListFiles.File file : fileList.getFile()) {
                FileSystemEntity fileEntity = new FileSystemEntity(file.getId(), file.getName(), false);
                parentEntity.addChild(fileEntity);
            }
        }
    }




    // 转换实体
    private static List<AppFileEntity> convert(List<ListFiles.File> file, String path, String parentId) {
        return file.stream().map(e -> AppFileEntity.builder()
                .fileId(e.getId())
                .fileName(e.getName())
                .fileSize(e.getSize())
                .fileMd5(e.getMd5())
                .startLabel(e.getStarLabel())
                .fileCata(e.getFileCata())
                .lastOpTime(e.getLastOpTime())
                .createTime(e.getCreateDate())
                .mediaType(e.getMediaType())
                .path(path)
                .parentId(parentId)
                .rev(e.getRev()).build()).collect(Collectors.toList());
    }


    private static String send(String fullUrlPattern, Object[] formatArgs, String sessionKey, String sessionSecret) {
        var fullUrl = String.format(fullUrlPattern, formatArgs);
        var headers = Map.ofEntries(Map.entry("Date", dateOfGmtStr()),
                Map.entry("SessionKey", sessionKey),
                Map.entry("Signature", signatureOfHmac(sessionSecret, sessionKey, "GET", fullUrl, dateOfGmtStr())),
                Map.entry("X-Request-ID", uuidUpper()));
        return OkHttpUtils.getStr(fullUrl, Headers.of(headers));
    }

}
