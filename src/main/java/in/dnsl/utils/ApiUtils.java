package in.dnsl.utils;

import static in.dnsl.utils.StringGenerator.*;

public class ApiUtils {

    public static String PcClientInfoSuffixParam(){
        return "clientType=TELEPC&version=6.2&channelId=web_cloud.189.cn&rand=" + rand();
    }

    // 获取大写的UUID
    public static String uuidUpper() {
        return uuidDash().toUpperCase();
    }
}
