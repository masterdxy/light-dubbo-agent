package com.github.masterdxy.light.dubbo.agent.common.log;

import com.alibaba.fastjson.JSON;
import com.github.masterdxy.light.dubbo.agent.common.Constants;
import com.github.masterdxy.light.dubbo.agent.common.config.ConfigCenter;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentRequestOuterClass;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentResponseOuterClass;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: dongxinyu
 * @date: 17/5/31 上午10:24
 */
public class ResponseLogger {

    private static final Logger respLogger = LoggerFactory.getLogger("response");

    private static final String HOST = System.getProperty("LOCAL_IP", "unknown");


    public static void logResponse(Channel channel, long start, AgentRequestOuterClass.AgentRequest request, AgentResponseOuterClass.AgentResponse response) {
        if (ConfigCenter.getBoolean("response_log", false)
                || response.getS() != Constants.SUCCESS) {
            respLogger.info("client : {} , requestId : {} , host : {} , " +
                            "service : {} , method : {} , data : {} ," +
                            " cost : {} , response data : {}", channel.getRemoteAddress().toString(), request.getRl(), HOST,
                    request.getS(), request.getM(), request.getD(),
                    System.currentTimeMillis() - start, JSON.toJSONString(response.getD()));
        }
    }
}
