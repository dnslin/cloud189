package in.dnsl.logic;

import in.dnsl.domain.req.AppCopyFileParam;
import in.dnsl.domain.result.AppFileEntity;
import in.dnsl.exception.AppException;
import in.dnsl.utils.JsonUtils;
import in.dnsl.utils.SignatureUtils;
import in.dnsl.utils.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import me.kuku.utils.OkHttpUtils;
import okhttp3.Headers;

import java.util.HashMap;
import java.util.Map;

import static in.dnsl.constant.ApiConstant.API_URL;
import static in.dnsl.logic.CloudLogin.getSession;
import static in.dnsl.utils.ApiUtils.*;

@Slf4j
public class FileCopy {

    public static void appCopyFile(AppCopyFileParam param) {
        var session = getSession();
        var fullUrl = "%s/copyFile.action?fileId=%s&destFileName=%s&destParentFolderId=%s&%s";
        fullUrl = String.format(fullUrl, API_URL, param.getFileId(), urlEncode(param.getDestFileName()), param.getDestFolderId(), PcClientInfoSuffixParam());
        Map<String,String> headers = Map.ofEntries(
                Map.entry("Date", dateOfGmtStr()),
                Map.entry("SessionKey", session.getSessionKey()),
                Map.entry("Signature", SignatureUtils.signatureOfHmac(session.getSessionSecret(), session.getSessionKey(), "POST", fullUrl, dateOfGmtStr())),
                Map.entry("X-Request-ID", uuidUpper())
        );
        var xmlData = OkHttpUtils.postStr(fullUrl, new HashMap<>(), Headers.of(headers));
        if (xmlData.contains("FileAlreadyExists")) throw new AppException("文件已存在");
        var appFileEntity = XmlUtils.xmlToObject(xmlData, AppFileEntity.class);
        log.info("{}", xmlData);
        log.info("{}", JsonUtils.objectToJson(appFileEntity));
    }
}
