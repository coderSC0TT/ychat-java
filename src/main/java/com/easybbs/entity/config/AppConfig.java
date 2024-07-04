package com.easybbs.entity.config;

import com.easybbs.utils.StringTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @ClassName Appconfig
 * @Description 添加描述
 * @Author Suguo
 * @LastChangeDate 2024/7/4 下午3:21
 */

@Component("appConfig")
public class AppConfig {
    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    /**
     * websocket 端口
     */
    @Value("${ws.port:}")
    private Integer wsPort;
    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    @Value("${admin.emails:}")
    private String adminEmails;

    public String getProjectFolder() {
        if (!StringTools.isEmpty(projectFolder) && !projectFolder.endsWith("/")) {
            projectFolder = projectFolder + "/";
        }
        return projectFolder;
    }

    public String getAdminEmails() {
        return adminEmails;
    }

    public Integer getWsPort() {
        return wsPort;
    }
}

