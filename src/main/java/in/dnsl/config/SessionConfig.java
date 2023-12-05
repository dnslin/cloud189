package in.dnsl.config;

import in.dnsl.domain.dto.SessionDTO;
import in.dnsl.logic.CloudLogin;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SessionConfig {

    // 单例模式提供 SessionDto对象
    private static volatile SessionDTO sessionDTO;

    public static SessionDTO getSession(String user,String passwd){
        if (sessionDTO == null){
            synchronized (SessionConfig.class){
                if (sessionDTO == null){
                    log.info("sessionDTO is null 用户未登录");
                    sessionDTO = CloudLogin.login(user,passwd);
                }
            }
        }
        return sessionDTO;
    }
    public static SessionDTO getSession(){
        if (sessionDTO == null){
            synchronized (SessionConfig.class){
                if (sessionDTO == null){
                    log.info("sessionDTO is null 用户未登录");
                }
            }
        }
        return sessionDTO;
    }
}
