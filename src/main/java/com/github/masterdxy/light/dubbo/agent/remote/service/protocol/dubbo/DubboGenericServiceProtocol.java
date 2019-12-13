package com.github.masterdxy.light.dubbo.agent.remote.service.protocol.dubbo;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.github.masterdxy.light.dubbo.agent.common.config.ConfigCenter;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RPCProtocol;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RPCUrl;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RemoteService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: dongxinyu
 * @date: 17/5/9 上午9:50
 */
public class DubboGenericServiceProtocol implements RPCProtocol {

    private String zookeeper;
    private String appname;

    private Map<RPCUrl, ReferenceConfig<GenericService>> urlReferenceConfigConcurrentHashMap = new ConcurrentHashMap<>();
    private static Logger logger = LoggerFactory.getLogger(DubboGenericServiceProtocol.class);

    @Override
    public RemoteService refer(RPCUrl url) {
        GenericService genericService = getGenericService(url);
        return new RemoteDubboService(genericService);
    }

    private GenericService getGenericService(RPCUrl url) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig();
        referenceConfig.setInterface(url.getServiceKey());
        referenceConfig.setProtocol(DubboConstant.DUBBO);
        referenceConfig.setGeneric(true);
        //version
        if (StringUtils.isNotEmpty(url.getVersion()) && !DubboConstant.EMPTY_VERSION.equals(url.getVersion())) {
            referenceConfig.setVersion(url.getVersion());
        }
        //连接数 一条连接最大7M带宽 需要自行换算QPS
        int connections = ConfigCenter.getInt("connections_" + mixServiceKey(url.getServiceKey(), url.getVersion()), 0);
        logger.info("service : {} , version : {} connections : {}", url.getServiceKey(), url.getVersion(), connections);

        referenceConfig.setConnections(connections);
        referenceConfig.setApplication(getApplicationConfig());
        referenceConfig.setRegistry(getRegistryConfig());

        Map<String, String> attr = Maps.newHashMap();
//		线程派发策略
//		attr.put("dispatcher", "direct");

        referenceConfig.setParameters(attr);

        GenericService service = referenceConfig.get();
        Preconditions.checkNotNull(service, "refer service but get null. url : " + JSON.toJSONString(url));
        urlReferenceConfigConcurrentHashMap.put(url, referenceConfig);
        return service;
    }

    private RegistryConfig getRegistryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        String registryProtocol = DubboConstant.REGISTRY;
        registryConfig.setProtocol(registryProtocol);
        registryConfig.setAddress(zookeeper);
        return registryConfig;
    }

    private ApplicationConfig getApplicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(appname);
        return applicationConfig;
    }

    private String mixServiceKey(String serviceName, String version) {
        if(StringUtils.isEmpty(version) || DubboConstant.EMPTY_VERSION.equals(version) ){
            return serviceName;
        }
        return serviceName + "_" + (StringUtils.isEmpty(version) ? DubboConstant.EMPTY_VERSION : version);
    }

    public DubboGenericServiceProtocol(String zookeeper, String appname) {
        this.zookeeper = zookeeper;
        this.appname = appname;
    }

    @Override
    public void destroy() {
        urlReferenceConfigConcurrentHashMap.forEach((url, referenceConfig) -> {
            try {
                referenceConfig.destroy();
            } catch (Exception ignore) {
            }
        });
    }

}
