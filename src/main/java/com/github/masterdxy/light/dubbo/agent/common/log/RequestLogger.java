package com.github.masterdxy.light.dubbo.agent.common.log;

import com.alibaba.fastjson.JSON;
import com.github.masterdxy.light.dubbo.agent.common.config.ConfigCenter;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentRequestOuterClass;
import com.github.masterdxy.light.dubbo.agent.common.utils.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: dongxinyu
 * @date: 17/5/24 下午1:30
 */
public class RequestLogger {

    private static final Logger reqLogger = LoggerFactory.getLogger("request");

    private static final String HOST = NetUtil.getHostname();

    public static void logRequest(String remoteIp, AgentRequestOuterClass.AgentRequest request) {
        if (ConfigCenter.getBoolean("request_log", true)) {
            reqLogger.info("client : {} , requestId : {} , host : {} , service : {} , method : {} , data : {}", remoteIp, request.getRl(), HOST, request.getS(), request.getM(), request.getD());
        }
    }
}
