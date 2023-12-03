package in.dnsl.logic;


import com.fasterxml.jackson.databind.JsonNode;
import in.dnsl.domain.dto.AccessTokenDTO;
import in.dnsl.domain.dto.Params;
import in.dnsl.domain.dto.SessionDTO;
import in.dnsl.domain.xml.UserSession;
import in.dnsl.utils.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.kuku.utils.OkHttpUtils;
import me.kuku.utils.OkUtils;
import okhttp3.Headers;
import okhttp3.Response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static in.dnsl.constant.ApiConstant.API_URL;
import static in.dnsl.constant.ApiConstant.WEB_URL;
import static in.dnsl.utils.ApiUtils.PcClientInfoSuffixParam;
import static in.dnsl.utils.ApiUtils.uuidUpper;
import static in.dnsl.utils.StringGenerator.uuidDash;

@Slf4j
public class CloudLogin {

    static {
        OkHttpUtils.setOkhttpClient(HttpClientSingleton.getInstance());
    }


    public static void login(String username, String password) {
        String loginUrl = "https://open.e.189.cn/api/logbox/oauth2/loginSubmit.do";
        Params loginParams = getLoginParams();
        // 构建请求参数 rsa加密账号密码 注意Base64转hex方法
        Map<String, String> params = Map.ofEntries(
                Map.entry("appKey", "8025431004"),
                Map.entry("accountType", "01"),
                Map.entry("userName", "{RSA}" + RSAEncryption.rsaEncode(username)),
                Map.entry("password", "{RSA}" + RSAEncryption.rsaEncode(password)),
                Map.entry("validateCode", ""),
                Map.entry("captchaToken", loginParams.captchaToken),
                Map.entry("returnUrl", loginParams.returnUrl),
                Map.entry("mailSuffix", "@189.cn"),
                Map.entry("dynamicCheck", "FALSE"),
                Map.entry("clientType", "10020"),
                Map.entry("cb_SaveName", "1"),
                Map.entry("isOauth2", "false"),
                Map.entry("state", ""),
                Map.entry("paramId", loginParams.paramId));
        // 构建请求头
        Map<String, String> headers = Map.ofEntries(
                Map.entry("Content-Type", "application/x-www-form-urlencoded"),
                Map.entry("Referer", "https://open.e.189.cn/api/logbox/oauth2/unifyAccountLogin.do"),
                Map.entry("Cookie", "LT=" + loginParams.lt),
                Map.entry("X-Requested-With", "XMLHttpRequest"),
                Map.entry("REQID", loginParams.reqId),
                Map.entry("lt", loginParams.lt));
        JsonNode data = OkHttpUtils.postJson(loginUrl, params, Headers.of(headers));
        // 判断是否登录成功
        if ("登录成功".equals(data.get("msg").asText())) log.info("登录时间->{},登录成功", LocalDateTime.now());
        else log.error("登录失败: {}", data.get("msg").asText());
        // 获取Session
        String fullUrl = "%s/getSessionForPC.action?clientType=%s&version=%s&channelId=%s&redirectURL=%s";
        fullUrl = String.format(fullUrl, API_URL, "TELEMAC", "1.0.0", "web_cloud.189.cn", URLEncoder.encode(data.get("toUrl").asText(), StandardCharsets.UTF_8));
        JsonNode json = OkHttpUtils.getJson(fullUrl, Headers.of("Accept", "application/json;charset=UTF-8"));
        SessionDTO sessionDTO = JsonUtils.jsonToObject(json.toString(), SessionDTO.class);
        log.info("获取到的Session: {}", JsonUtils.objectToJson(sessionDTO));
        // 获取Ssk token
        String sskUrl = "%s/open/oauth2/getAccessTokenBySsKey.action?sessionKey=%s";
        sskUrl = String.format(sskUrl, API_URL, sessionDTO.getSessionKey());
        // md5 加密参数
        Map<String,String> signParams = Map.ofEntries(
                Map.entry("AppKey", "601102120"),
                Map.entry("sessionKey", sessionDTO.getSessionKey()),
                Map.entry("Timestamp", String.valueOf(System.currentTimeMillis())));
        // 构建请求头
        headers = Map.ofEntries(
                Map.entry("Accept", "application/json"),
                Map.entry("AppKey", "601102120"),
                Map.entry("Sign-Type", "1"),
                Map.entry("Timestamp", String.valueOf(System.currentTimeMillis())),
                Map.entry("Signature", SignatureUtils.signatureOfMd5(signParams)));
        JsonNode sskJson = OkHttpUtils.getJson(sskUrl, Headers.of(headers));
        log.info("获取到的Ssk: {}", sskJson);
        AccessTokenDTO accessTokenDTO = JsonUtils.jsonToObject(sskJson.toString(), AccessTokenDTO.class);
        sessionDTO.setAccessTokenDTO(accessTokenDTO);
        System.out.print(getSessionBySessionKey(sessionDTO.getSessionKey()));
    }

    // 通过Token刷新Session
    public static UserSession getSessionByAccessToken(String accessToken){
        // 生成uuid 和 clientSn
        String fullUrl = "%s/getSessionForPC.action?appId=%s&accessToken=%s&clientSn=%s&%s";
        fullUrl = String.format(fullUrl, API_URL, "8025431004", accessToken, uuidDash(), PcClientInfoSuffixParam());
        String xmlData = OkHttpUtils.getStr(fullUrl, Headers.of("X-Request-ID", uuidUpper()));
        // xml 转 对象
        UserSession userSession = XmlUtils.xmlToObject(xmlData, UserSession.class);
        log.info("通过Token获取到的Session: {}", userSession);
        return userSession;
    }

    // 通过SessionKey刷新Session
    public static String getSessionBySessionKey(String sessionKey){
        String fullUrl = "%s/ssoLogin.action?sessionKey=%s&redirectUrl=main.action%%23recycle";
        fullUrl = String.format(fullUrl, WEB_URL, sessionKey);
        Response response = OkHttpUtils.get(fullUrl, Headers.of("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,ja;q=0.7"));
        String cookie = OkUtils.cookie(response);
        log.info("通过SessionKey获取到的Cookie: {}", cookie);
        // 有两个 COOKIE_LOGIN_USER 我取的是最后面的 我也不知道为什么会返回两个
        return cookie.split("COOKIE_LOGIN_USER=")[2].split(";")[0];
    }

    @SneakyThrows
    private static Params getLoginParams() {
        String fullUrl = "%s/unifyLoginForPC.action?appId=%s&clientType=%s&returnURL=%s&timeStamp=%d";
        fullUrl = String.format(fullUrl, WEB_URL, "8025431004", "10020", "https://m.cloud.189.cn/zhuanti/2020/loginErrorPc/index.html", System.currentTimeMillis());
        log.info("请求API Params-->: {}", fullUrl);
        String content = OkHttpUtils.getStr(fullUrl, Headers.of("Content-Type", "application/x-www-form-urlencoded"));
        // 通过正则表达式获取参数
        return extractParams(content);
    }


    private static Params extractParams(String content) {
        Params params = new Params();
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile("captchaToken' value='(.+?)'");
        matcher = pattern.matcher(content);

        if (matcher.find()) params.captchaToken = matcher.group(1);
        pattern = Pattern.compile("lt = \"(.+?)\"");
        matcher = pattern.matcher(content);

        if (matcher.find()) params.lt = matcher.group(1);
        pattern = Pattern.compile("returnUrl = '(.+?)'");
        matcher = pattern.matcher(content);

        if (matcher.find()) params.returnUrl = matcher.group(1);
        pattern = Pattern.compile("paramId = \"(.+?)\"");
        matcher = pattern.matcher(content);

        if (matcher.find()) params.paramId = matcher.group(1);
        pattern = Pattern.compile("reqId = \"(.+?)\"");
        matcher = pattern.matcher(content);

        if (matcher.find()) params.reqId = matcher.group(1);
        pattern = Pattern.compile("j_rsaKey\" value=\"(.+?)\"");
        matcher = pattern.matcher(content);

        if (matcher.find()) params.jRsaKey = matcher.group(1);
        log.info("获取到的参数: {}", params);
        return params;
    }


}
