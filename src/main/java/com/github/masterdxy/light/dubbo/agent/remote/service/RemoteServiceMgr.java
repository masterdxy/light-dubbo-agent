package com.github.masterdxy.light.dubbo.agent.remote.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentRequestOuterClass;
import com.github.masterdxy.light.dubbo.agent.common.utils.HttpUtil;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RPCProtocol;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RPCUrl;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RemoteService;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.dubbo.DubboConstant;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午4:27
 */
public class RemoteServiceMgr {

    private RPCProtocol rpcProtocol;

    private static Map<String, RemoteService> remoteServiceWrapMap = new ConcurrentHashMap<>();

    private ReentrantLock lock = new ReentrantLock();

    private static final Logger logger = LoggerFactory.getLogger(RemoteServiceMgr.class);

    public RemoteService getServiceWrap(AgentRequestOuterClass.AgentRequest request) {

        RemoteService remoteServiceWrap = remoteServiceWrapMap.get(mixServiceKey(request.getS(), request.getV()));
        if (remoteServiceWrap == null) {
            lock.lock();
            try {
                remoteServiceWrap = remoteServiceWrapMap.get(mixServiceKey(request.getS(), request.getV()));
                if (remoteServiceWrap == null) {
                    RPCUrl url = new RPCUrl();
                    //service name
                    url.setServiceKey(request.getS());
                    //service version
                    url.setVersion(request.getV());
                    remoteServiceWrap = referRemoteService(url);
                    Preconditions.checkNotNull(remoteServiceWrap, "cannot refer remote service ,request : {}", request);
                    remoteServiceWrapMap.put(mixServiceKey(request.getS(), request.getV()), remoteServiceWrap);
                }
            } finally {
                //no provider exception 防止死锁
                lock.unlock();
            }
        }

        return remoteServiceWrap;
    }

    private RemoteService referRemoteService(RPCUrl url) {
        try {
            return rpcProtocol.refer(url);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return null;
    }

    public void destroy() {
        rpcProtocol.destroy();
    }

    public RemoteServiceMgr(RPCProtocol rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }


    private String mixServiceKey(String serviceName, String version) {
        if (StringUtils.isEmpty(version) || DubboConstant.EMPTY_VERSION.equals(version)) {
            //default version is empty
            return serviceName;
        }
        return serviceName + "_" + version;
    }

    /**
     * 预初始化客户端连接
     */
    public void preInit(String zkproxyUrl, String secureCode) {
        Map<String, Object> params = new HashMap<>();
        params.put("version", "0");
        Map<String, String> header = new HashMap<>();
        header.put("secure_code", secureCode);
        logger.info("pull service provider url from zkproxy start. url : {}", zkproxyUrl);
        String serviceMapping = HttpUtil.doGet(zkproxyUrl, params, header);
        logger.info("pull service provider url from zkproxy end.");
        if (StringUtils.isNotEmpty(serviceMapping)) {
            List<String> available = getAvailableService(serviceMapping);
            available.forEach(s -> {
                RPCUrl rpcUrl = new RPCUrl();
                rpcUrl.setServiceKey(s);
                rpcUrl.setVersion(null);
                RemoteService remoteService = referRemoteService(rpcUrl);
                if (remoteService != null) {
                    remoteServiceWrapMap.put(s, remoteService);
                }
            });
        }
    }

    private List<String> getAvailableService(String serviceMapping) {
        List<String> available = new ArrayList<>();

        JSONObject object = JSON.parseObject(serviceMapping);
        object.keySet().forEach(serviceName -> {
            if (!"timestamp".equals(serviceName)) {
                JSONObject serviceProtocolConfig = (JSONObject) object.get(serviceName);
                if (serviceProtocolConfig != null) {
                    JSONArray serviceIpArray = (JSONArray) serviceProtocolConfig.get(DubboConstant.DUBBO);
                    if (serviceIpArray != null && serviceIpArray.size() != 0) {
                        available.add(serviceName);
                    }
                }
            }
        });
        logger.info("success refer {} remote service", available.size());
        return available;
    }


}
