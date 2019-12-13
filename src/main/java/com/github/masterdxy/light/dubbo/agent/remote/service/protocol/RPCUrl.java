package com.github.masterdxy.light.dubbo.agent.remote.service.protocol;

import java.util.Map;

/**
 * @author: dongxinyu
 * @date: 17/5/9 上午9:48
 */
public class RPCUrl {

	private String serviceKey;
	private String version;


	private Map<String,String> params;

	public String getServiceKey() {
		return serviceKey;
	}

	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		RPCUrl rpcUrl = (RPCUrl) o;

		if (serviceKey != null ? !serviceKey.equals(rpcUrl.serviceKey) : rpcUrl.serviceKey != null) return false;
		if (version != null ? !version.equals(rpcUrl.version) : rpcUrl.version != null) return false;
		return params != null ? params.equals(rpcUrl.params) : rpcUrl.params == null;
	}

	@Override
	public int hashCode() {
		int result = serviceKey != null ? serviceKey.hashCode() : 0;
		result = 31 * result + (version != null ? version.hashCode() : 0);
		result = 31 * result + (params != null ? params.hashCode() : 0);
		return result;
	}
}
