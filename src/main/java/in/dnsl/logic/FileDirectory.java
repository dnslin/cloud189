package in.dnsl.logic;

import in.dnsl.domain.req.AppGetFileInfoParam;
import in.dnsl.domain.xml.AppErrorXmlResp;
import in.dnsl.domain.xml.AppGetFileInfoResult;
import in.dnsl.utils.JsonUtils;
import in.dnsl.utils.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import me.kuku.utils.OkHttpUtils;
import okhttp3.Headers;

import java.util.Map;

import static in.dnsl.config.SessionConfig.getSession;
import static in.dnsl.constant.ApiConstant.API_URL;
import static in.dnsl.utils.ApiUtils.*;
import static in.dnsl.utils.SignatureUtils.signatureOfHmac;

@Slf4j
public class FileDirectory {

    //根据文件ID或者文件绝对路径获取文件信息，支持文件和文件夹
    public static void appGetBasicFileInfo(AppGetFileInfoParam build) {
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
            if (build.getFileId().isBlank()) throw new RuntimeException("FileId为空");
            fullUrlPattern = "%s/family/file/getFolderInfo.action?familyId=%d&folderId=%s&folderPath=%s&pathList=0&%s";
            formatArgs = new Object[]{API_URL, build.getFamilyId(), build.getFileId(), urlEncode(build.getFilePath()), PcClientInfoSuffixParam()};
        }
        var fullUrl = String.format(fullUrlPattern, formatArgs);
        // 构建请求头 签名参数
        Map<String,String> headers = Map.ofEntries(
                Map.entry("Date", dateOfGmtStr()),
                        Map.entry("SessionKey", sessionKey),
                        Map.entry("Signature", signatureOfHmac(sessionSecret, sessionKey, "GET", fullUrl, dateOfGmtStr())),
                        Map.entry("X-Request-ID", uuidUpper())
                );
        var xmlData = OkHttpUtils.getStr(fullUrl,Headers.of(headers));
        if (xmlData.contains("error")) {
            AppErrorXmlResp appErrorXmlResp = XmlUtils.xmlToObject(xmlData, AppErrorXmlResp.class);
            throw new RuntimeException("请求失败:"+appErrorXmlResp.getCode());
        }
        AppGetFileInfoResult appGetFileInfoResult = XmlUtils.xmlToObject(xmlData, AppGetFileInfoResult.class);
        log.info("{}", JsonUtils.objectToJson(appGetFileInfoResult));
    }


}
