package in.dnsl.utils;

import java.util.Random;

import static java.util.UUID.*;

public class StringGenerator {
    public static String rand() {
        Random random = new Random();
        return random.nextInt(100000) + // rand.Int63n(1e5) in Go
                "_" +
                random.nextLong(100000000L);
    }
    // 生成UUID
    public static String uuid() {
        return randomUUID().toString().replace("-", "");
    }

    // 生成UUID带 -
    public static String uuidDash() {
        return randomUUID().toString();
    }
    public static void main(String[] args) {
        String randomString = rand();
        System.out.println(randomString);
    }
}
