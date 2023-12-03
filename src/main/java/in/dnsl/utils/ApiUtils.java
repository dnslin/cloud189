package in.dnsl.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static in.dnsl.utils.StringGenerator.rand;
import static in.dnsl.utils.StringGenerator.uuidDash;

public class ApiUtils {

    public static String PcClientInfoSuffixParam(){
        return "clientType=TELEPC&version=6.2&channelId=web_cloud.189.cn&rand=" + rand();
    }

    // 获取大写的UUID
    public static String uuidUpper() {
        return uuidDash().toUpperCase();
    }

    public static String dateOfGmtStr() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date currentDate = new Date();
        return dateFormat.format(currentDate);
    }

    public static void main(String[] args) {
        String s = dateOfGmtStr();
        System.out.println(s);
    }

    public static String SignatureOfHmac(String sessionSecret, String sessionKey, String get, String fullUrl, String s) {
        return null;
    }
}
