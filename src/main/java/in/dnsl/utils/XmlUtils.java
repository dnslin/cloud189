package in.dnsl.utils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class XmlUtils {
    private static final XStream xstream = new XStream();

    static {
        // 设置 XStream 安全性，允许任何类被序列化或反序列化
        xstream.addPermission(AnyTypePermission.ANY);
    }

    // 将XML转换为Java对象
    public static <T> T xmlToObject(String xml, Class<T> clazz) {
        xstream.processAnnotations(clazz);
        return clazz.cast(xstream.fromXML(xml));
    }

    // 将Java对象转换为XML字符串
    public static String objectToXml(Object obj) {
        xstream.processAnnotations(obj.getClass());
        return xstream.toXML(obj);
    }

}
