package in.dnsl.utils;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncryption {

    private static  String RsaPublicKey = """
            -----BEGIN PUBLIC KEY-----
            MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDY7mpaUysvgQkbp0iIn2ezoUyh
            i1zPFn0HCXloLFWT7uoNkqtrphpQ/63LEcPz1VYzmDuDIf3iGxQKzeoHTiVMSmW6
            FlhDeqVOG094hFJvZeK4OzA6HVwzwnEW5vIZ7d+u61RV1bsFxmB68+8JXs3ycGcE
            4anY+YzZJcyOcEGKVQIDAQAB
            -----END PUBLIC KEY-----""";
    private static final String[] BI_RM = "0123456789abcdefghijklmnopqrstuvwxyz".split("");
    private static final String B64MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";


    @SneakyThrows
    public static String rsaEncode(String data) {
        PublicKey publicKey = loadPublicKey();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return b64toHex(Base64.getEncoder().encodeToString(encryptedBytes));
    }

    private static PublicKey loadPublicKey() throws Exception {
        RsaPublicKey = RsaPublicKey.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
        byte[] keyBytes = Base64.getDecoder().decode(RsaPublicKey);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    private static String int2char(int a) {
        return BI_RM[a];
    }

    public static String b64toHex(String a) {
        StringBuilder d = new StringBuilder();
        int e = 0;
        int c = 0;
        for (int i = 0; i < a.length(); i++) {
            char currentChar = a.charAt(i);
            if (currentChar != '=') {
                int v = B64MAP.indexOf(currentChar);
                switch (e) {
                    case 0:
                        e = 1;
                        d.append(int2char(v >> 2));
                        c = v & 3;
                        break;
                    case 1:
                        e = 2;
                        d.append(int2char(c << 2 | v >> 4));
                        c = v & 15;
                        break;
                    case 2:
                        e = 3;
                        d.append(int2char(c));
                        d.append(int2char(v >> 2));
                        c = v & 3;
                        break;
                    default:
                        e = 0;
                        d.append(int2char(c << 2 | v >> 4));
                        d.append(int2char(v & 15));
                        break;
                }
            }
        }

        if (e == 1) {
            d.append(int2char(c << 2));
        }
        return d.toString();
    }
}
