package com.github.masterdxy.light.dubbo.agent.remote.invoke;

import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RemoteService;

import java.util.Map;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午4:01
 */
public class InvocationContext {

	private String serviceKey;

	private String methodName;

	private Object paramData;

	private Map<String,String> attachment;

	private RemoteService remoteService;


	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Object getParamData() {
		return paramData;
	}

	public void setParamData(Object paramData) {
		this.paramData = paramData;
	}

	public Map<String, String> getAttachment() {
		return attachment;
	}

	public void setAttachment(Map<String, String> attachment) {
		this.attachment = attachment;
	}

	public RemoteService getRemoteService() {
		return remoteService;
	}

	public void setRemoteService(RemoteService remoteService) {
		this.remoteService = remoteService;
	}

	public String getInvokeKey(){
		return getServiceKey() + "." + getMethodName();
	}

	@Override
	public String toString() {
		return "InvocationContext{" +
				"serviceKey='" + serviceKey + '\'' +
				", methodName='" + methodName + '\'' +
				", paramData=" + paramData +
				", attachment=" + attachment +
				", remoteService=" + remoteService +
				'}';
	}
}
