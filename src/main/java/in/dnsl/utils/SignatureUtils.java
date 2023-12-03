package in.dnsl.utils;

import lombok.SneakyThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SignatureUtils {

    public static String signatureOfMd5(Map<String, String> params) {
        List<String> keys = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            keys.add(entry.getKey() + "=" + entry.getValue());
        }

        Collections.sort(keys);
        String signStr = String.join("&", keys);

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(signStr.getBytes());
            byte[] digest = md.digest();
            return bytesToHex(digest).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    @SneakyThrows
    public static String signatureOfHmac(String secretKey, String sessionKey, String operate, String urlString, String dateOfGmt) {
        try {
            URI url = new URI(urlString);
            String requestUri = url.getPath();

            String plainStr = String.format("SessionKey=%s&Operate=%s&RequestURI=%s&Date=%s",
                    sessionKey, operate, requestUri, dateOfGmt);

            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(keySpec);
            byte[] result = mac.doFinal(plainStr.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(result).toUpperCase();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error while calculating HMAC", e);
        }
    }

}

