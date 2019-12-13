package com.github.masterdxy.light.dubbo.agent.common.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * The type Config center.
 *
 * @author: dongxinyu
 * @date: 17 /5/12 下午2:51
 */
@Component public class ConfigCenter {

    private static Logger logger = LoggerFactory.getLogger(ConfigCenter.class);
    private static final ConcurrentHashMap<String, String> configurations = new ConcurrentHashMap<>();

    @PostConstruct public void init() {
        //for debug
        startDebugPrinter();
        //Load config from nacos/apollo
    }

    private static void startDebugPrinter() {
        Executors.newSingleThreadExecutor().submit(() -> {
            while (true) {
                logger.info("config : {}", JSON.toJSONString(configurations, SerializerFeature.PrettyFormat));
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignored) {
                }
            }
        });
    }

    //---- GETS

    public static String getString(String path, String def) {

        return configurations.getOrDefault(path, def);
    }

    public static boolean getBoolean(String path, boolean def) {
        return Boolean.parseBoolean(configurations.getOrDefault(path, String.valueOf(def)));
    }

    public static long getLong(String path, long def) {
        return Long.parseLong(configurations.getOrDefault(path, String.valueOf(def)));
    }

    public static int getInt(String path, int def) {
        return Integer.parseInt(configurations.getOrDefault(path, String.valueOf(def)));
    }

}
