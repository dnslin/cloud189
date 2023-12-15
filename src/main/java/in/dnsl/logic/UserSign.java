package in.dnsl.logic;

import in.dnsl.domain.dto.SessionDTO;
import in.dnsl.domain.result.UserSignResult;
import in.dnsl.utils.SignatureUtils;
import in.dnsl.utils.XmlUtils;
import lombok.extern.slf4j.Slf4j;
import me.kuku.utils.OkHttpUtils;
import okhttp3.Headers;

import java.util.HashMap;
import java.util.Map;

import static in.dnsl.constant.ApiConstant.API_URL;
import static in.dnsl.utils.ApiUtils.dateOfGmtStr;
import static in.dnsl.utils.ApiUtils.uuidUpper;
import static in.dnsl.utils.StringGenerator.uuidDash;

@Slf4j
public class UserSign {

    public static void appUserSign(SessionDTO dto){
        var fullUrl = "%s/mkt/userSign.action?clientType=TELEIPHONE&version=8.9.4&model=iPhone&osFamily=iOS&osVersion=13.7&clientSn=%s";
        fullUrl = String.format(fullUrl, API_URL, uuidDash());
        var headers = Map.ofEntries(
                Map.entry("Date", dateOfGmtStr()),
                Map.entry("SessionKey",dto.getSessionKey()),
                Map.entry("Signature", SignatureUtils.signatureOfHmac(dto.getSessionSecret(),dto.getSessionKey(),"POST",fullUrl,dateOfGmtStr())),
                Map.entry("X-Request-ID", uuidUpper()),
                Map.entry("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Mobile/15E148 Ecloud/8.9.4 (iPhone; " + uuidDash() + "; appStore) iOS/13.7")
        );
        var xmlData = OkHttpUtils.postStr(fullUrl, new HashMap<>(),Headers.of(headers));
        var userSignResult = XmlUtils.xmlToObject(xmlData, UserSignResult.class);
        log.info("{}", userSignResult);
    }
}
