package in.dnsl.utils;

import in.dnsl.config.SimpleCookieJar;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.net.Proxy;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;


import java.util.Objects;
@Slf4j
public class HttpClientSingleton {
    private static volatile OkHttpClient instance;
    private static Proxy proxy;

    public static OkHttpClient getInstance() {
        if (instance == null) {
            synchronized (HttpClientSingleton.class) {
                if (instance == null) {
                    try {
                        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        trustManagerFactory.init((KeyStore) null);

                        SSLContext sslContext = SSLContext.getInstance("TLS");
                        sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

                        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                                .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) Objects.requireNonNull(trustManagerFactory.getTrustManagers()[0]))
                                .hostnameVerifier((hostname, session) -> true)
                                .followRedirects(true)
                                .followSslRedirects(false)
                                .readTimeout(60L, TimeUnit.SECONDS)
                                .connectTimeout(60L, TimeUnit.SECONDS)
                                .cookieJar(new SimpleCookieJar());

                        if (proxy != null) builder.proxy(proxy);
                        instance = builder.build();
                    } catch (Exception e) {
                        log.error("Error initializing HttpClientSingleton", e);
                    }
                }
            }
        }
        return instance;
    }
}
