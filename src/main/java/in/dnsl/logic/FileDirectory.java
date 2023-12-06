package in.dnsl.logic;

import in.dnsl.domain.req.AppFileListParam;
import in.dnsl.domain.req.AppGetFileInfoParam;
import in.dnsl.domain.result.AppFileEntity;
import in.dnsl.domain.result.AppFileListResult;
import in.dnsl.domain.xml.AppErrorXmlResp;
import in.dnsl.domain.xml.AppGetFileInfoResult;
import in.dnsl.domain.xml.ListFiles;
import in.dnsl.enums.OrderEnums;
import in.dnsl.utils.JsonUtils;
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
        var xmlData = send(fullUrlPattern,formatArgs,sessionKey, sessionSecret);
        if (xmlData.contains("error")) {
            var appErrorXmlResp = XmlUtils.xmlToObject(xmlData, AppErrorXmlResp.class);
            throw new RuntimeException("请求失败:"+appErrorXmlResp.getCode());
        }
        return XmlUtils.xmlToObject(xmlData, AppGetFileInfoResult.class);
    }

    // 获取指定目录下的所有文件列表
    @SneakyThrows
    public static void appGetAllFileList(@NotNull AppFileListParam param){
        if (param.getPageSize() <= 0) param.setPageSize(200);
        // 如果 家庭id 大于 0 并且 文件id为 -11 则把文件ID设置为 空
        if (param.getFamilyId() > 0 && param.getFileId().equals("-11")) param.setFileId("");
        var files = appFileList(param);
        var build = AppFileListResult.builder()
                .lastRev(files.getLastRev())
                .count(files.getFileList().getFile().size())
                .fileList(convert(files.getFileList().getFile())).build();
        if (build.getCount() > param.getPageSize()){
            // 如果文件数量大于每页数量，则递归获取
            var pageNum = param.getPageNum();
            var pageSize = param.getPageSize();
            var totalPage = (int) Math.ceil((double) build.getCount() / pageSize);
            for (int i = 1; i < totalPage; i++) {
                param.setPageNum(++pageNum);
                var fileList = appFileList(param);
                build.getFileList().addAll(convert(fileList.getFileList().getFile()));
                TimeUnit.MILLISECONDS.sleep(100);
            }
        }
        // 替换
        build.getFileList().forEach(e-> e.setParentId(param.getFileId()));
        // construct path
        log.info("{}", JsonUtils.objectToJson(build));
    }

    // 转换实体
    private static List<AppFileEntity> convert( List<ListFiles.File> file){
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
                .rev(e.getRev()).build()).collect(Collectors.toList());
    }

    //获取文件列表
    public static ListFiles appFileList(@NotNull AppFileListParam param){
        Object[] formatArgs;
        var session = getSession();
        String sessionKey, sessionSecret, fullUrlPattern;
        if (param.getFamilyId() <= 0){
            sessionKey = session.getSessionKey();
            sessionSecret = session.getSessionSecret();
            fullUrlPattern = "%s/listFiles.action?folderId=%s&recursive=0&fileType=0&iconOption=10&mediaAttr=0&orderBy=%s&descending=%s&pageNum=%s&pageSize=%s&%s";
            formatArgs = new Object[]{API_URL, param.getFileId(), OrderEnums.getByCode(param.getOrderBy()), false, param.getPageNum(), param.getPageSize(), PcClientInfoSuffixParam()};
        }else {
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
    public static String appFilePathById(Integer familyId, String fileId){
        var fullPath = "";
        var param = AppGetFileInfoParam.builder()
                .familyId(familyId)
                .fileId(fileId).build();
        while (true) {
            var fi = appGetBasicFileInfo(param);
            if (fi == null) throw new RuntimeException("FileInfo is null");
            if (!fi.getPath().isEmpty()) {
                return fi.getPath();
            }
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
    public static void appFileInfoById(Integer familyId, String fileId){
        var param = AppGetFileInfoParam.builder()
                .familyId(familyId)
                .fileId(fileId).build();
        var result = appGetBasicFileInfo(param);

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
