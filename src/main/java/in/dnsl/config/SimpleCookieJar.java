package in.dnsl.config;

import in.dnsl.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.*;
@Slf4j
public class SimpleCookieJar implements CookieJar {
    private static final Map<String, List<Cookie>> cookieStore = new HashMap<>();

    @Override
    public void saveFromResponse(HttpUrl url, @NotNull List<Cookie> cookies) {
        cookieStore.put(url.host(), new ArrayList<>(cookies));
    }

    @Override
    public @NotNull List<Cookie> loadForRequest(HttpUrl url) {
        List<Cookie> cookies = cookieStore.get(url.host());
        log.info("loadForRequest: url={},cookie={}", url, JsonUtils.objectToJson(cookies));
        return cookies != null ? cookies : new ArrayList<>();
    }

    public static void setGlobalCookie(@NotNull String url, @NotNull String cookie) {
        log.info("setGlobalCookie: url={}, cookie={}", url, cookie);
        HttpUrl httpUrl = HttpUrl.parse(url);
        Cookie cookies = new Cookie.Builder()
                .name("COOKIE_LOGIN_USER")
                .value(cookie)
                .domain(Objects.requireNonNull(httpUrl).host())
                .build();
        // 设置全局Cookie
        List<Cookie> cookiesForHost = cookieStore.getOrDefault(httpUrl.host(), new ArrayList<>());
        cookiesForHost.add(cookies);
        cookieStore.put(httpUrl.host(), cookiesForHost);
    }
}
