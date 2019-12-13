package com.github.masterdxy.light.dubbo.agent.bootstrap.configuration;

import com.github.masterdxy.light.dubbo.agent.common.serialization.Serializer;
import com.github.masterdxy.light.dubbo.agent.common.serialization.json.JSONSerializer;
import com.github.masterdxy.light.dubbo.agent.common.serialization.json.JacksonSerializer;
import com.github.masterdxy.light.dubbo.agent.connector.AbstractNetworkServer;
import com.github.masterdxy.light.dubbo.agent.connector.tcp.LengthBasePBServer;
import com.github.masterdxy.light.dubbo.agent.connector.tcp.codec.RPCAgentDecoder;
import com.github.masterdxy.light.dubbo.agent.connector.tcp.codec.RPCAgentEncoder;
import com.github.masterdxy.light.dubbo.agent.connector.tcp.handler.RequestDispatcher;
import com.github.masterdxy.light.dubbo.agent.remote.invoke.Invoker;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author tomoyo
 */
@Configuration
@ConfigurationProperties(locations = "classpath:/connector/connector-${spring.profiles.active}.properties", prefix = "connector")
public class ConnectorConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ConnectorConfiguration.class);

    @Value("${serializer}")
    private String serializer;
    /**
     * http or tcp
     */
    private String type;
    private String name;
    private int boss;
    private int worker;
    private int port;
    private Map<String, String> option;
    private String host;

    @Autowired
    private Invoker invoker;


    @Bean
    public AbstractNetworkServer abstractNetworkServer() {
        logger.info("connector start params : [ type :{}, name :{}, boss :{}, worker :{}, options : {} ]", type, name, boss, worker, option);
        Map<String, ChannelHandler> channelHandlerMap = new LinkedHashMap<>();
        channelHandlerMap.put("dispatcher", getProtoBufDispatcher());
        Map<String, Object> config = new HashMap<>();
        config.put("keepAlive", Boolean.parseBoolean(option.getOrDefault("connector.option.keepAlive", "true")));
        config.put("tcpNoDelay", Boolean.parseBoolean(option.getOrDefault("connector.option.tcpNoDelay", "true")));

        return LengthBasePBServer.builder(name)
                .setHost(host)
                .setPort(port)
                .setChannelConfig(config)
                .setEncoder(getEncoder())
                .setDecoder(getDecoder())
                .setHandlers(channelHandlerMap)
                .setBossThreadPoolSize(boss)
                .setWorkerThreadPoolSize(worker).build();
    }

    @Bean
    public OneToOneEncoder getEncoder() {
        return new RPCAgentEncoder(getSerializer());
    }

    @Bean
    public OneToOneDecoder getDecoder() {
        return new RPCAgentDecoder(getSerializer());
    }

    @Bean
    public Serializer getSerializer() {
        if(Serializer.FASTJSON.equalsIgnoreCase(serializer)){
            return new JSONSerializer();
        }else{
            return new JacksonSerializer();
        }
    }

    @Bean
    public RequestDispatcher getProtoBufDispatcher(){
        return new RequestDispatcher(invoker);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBoss() {
        return boss;
    }

    public void setBoss(int boss) {
        this.boss = boss;
    }

    public int getWorker() {
        return worker;
    }

    public void setWorker(int worker) {
        this.worker = worker;
    }

    public Map<String, String> getOption() {
        return option;
    }

    public void setOption(Map<String, String> option) {
        this.option = option;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
