package in.dnsl.config;

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
        var cookies = cookieStore.get(url.host());
        return cookies != null ? cookies : new ArrayList<>();
    }

    public static void setGlobalCookie(@NotNull String url, @NotNull String cookie) {
        var httpUrl = HttpUrl.parse(url);
        var cookies = new Cookie.Builder()
                .name("COOKIE_LOGIN_USER")
                .value(cookie)
                .domain(Objects.requireNonNull(httpUrl).host())
                .build();
        // 设置全局Cookie
        var cookiesForHost = cookieStore.getOrDefault(httpUrl.host(), new ArrayList<>());
        cookiesForHost.add(cookies);
        cookieStore.put(httpUrl.host(), cookiesForHost);
    }
}
