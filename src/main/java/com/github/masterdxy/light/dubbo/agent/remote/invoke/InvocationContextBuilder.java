package com.github.masterdxy.light.dubbo.agent.remote.invoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentRequestOuterClass;
import com.github.masterdxy.light.dubbo.agent.remote.service.RemoteServiceMgr;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: dongxinyu
 * @date: 17/5/9 上午9:38
 */
@Component
public class InvocationContextBuilder {

	@Autowired
	private RemoteServiceMgr remoteServiceMgr;

	public InvocationContext getInvocationContext(AgentRequestOuterClass.AgentRequest request) {
		InvocationContext invocationContext = new InvocationContext();
		String serviceKey = request.getS();
		String methodName = request.getM();
		invocationContext.setRemoteService(remoteServiceMgr.getServiceWrap(request));
		invocationContext.setServiceKey(serviceKey);
		invocationContext.setMethodName(methodName);
		invocationContext.setParamData(request.getD());//convert to byte[]
		String attJson = request.getA();
		Map<String,String> attachment = StringUtils.isEmpty(attJson) ? new HashMap<>() : JSON.parseObject(attJson,new TypeReference<Map<String,String>>(){});
		invocationContext.setAttachment(attachment);
		return invocationContext;
	}

}
