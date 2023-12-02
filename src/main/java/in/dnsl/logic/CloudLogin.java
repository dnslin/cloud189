package in.dnsl.logic;


import in.dnsl.constant.ApiConstant;
import in.dnsl.utils.HttpClientSingleton;
import lombok.extern.slf4j.Slf4j;
import me.kuku.utils.OkHttpUtils;
import okhttp3.Headers;

@Slf4j
public class CloudLogin {

    static {
        OkHttpUtils.setOkhttpClient(HttpClientSingleton.getInstance());
    }
    public static void main(String[] args) {
        getLoginParams();
    }

    public static void login(String username, String password) {

    }

    private static void getLoginParams() {
        String data = OkHttpUtils.getStr(ApiConstant.WEB_URL + "/udb/udb_login.jsp?pageId=1&redirectURL=/main.action",
                Headers.of("Content-Type", "application/x-www-form-urlencoded"));
        System.out.println(data);
    }
}
