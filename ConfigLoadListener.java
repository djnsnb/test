package com.nrzx.htkf.hottrackserver.listener;

import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ConfigLoadListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoadListener.class);
    private static final String log4jName = "log4j2.xml";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String rootDirectory = System.getProperty("dir");
        if (rootDirectory == null) {
            logger.error("error: you need specify project directory -Ddir=/path/to");
            return;
        }
        String configPath = rootDirectory + File.separator + "config" + File.separator;
        File log4jFile = new File(configPath+log4jName);
        try {
            if (log4jFile.exists()) {
                ConfigurationSource source = new ConfigurationSource(new FileInputStream(log4jFile), log4jFile);
                Configurator.reconfigure(log4jFile.toURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        logger.info("config path is:"+configPath);
        File rootConfigFile = new File(configPath);
        if (rootConfigFile.isDirectory() && rootConfigFile.exists()) {
            File[] properties = rootConfigFile.listFiles();
            if (properties != null) {
                try {

                    for (File file : properties) {
                        if (file.getName().endsWith(".properties")) {
                            Resource resource = new FileSystemResource(file.getAbsoluteFile());
                            logger.info("load config file" + resource.getFilename());
                            event.getEnvironment().getPropertySources().addLast(new ResourcePropertySource(
                                    Objects.requireNonNull(resource.getFilename()), new EncodedResource(resource, StandardCharsets.UTF_8)));
                        }
                    }

                } catch (IOException e) {
                    System.err.println("fail to find config file");
                    e.printStackTrace();
                }
            }
        }


    }
}