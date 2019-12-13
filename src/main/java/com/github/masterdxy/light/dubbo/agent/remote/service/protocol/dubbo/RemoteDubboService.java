package com.github.masterdxy.light.dubbo.agent.remote.service.protocol.dubbo;

import com.alibaba.dubbo.rpc.service.GenericService;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RemoteService;

import java.util.Map;

/**
 * @author: dongxinyu
 * @date: 17/5/9 上午11:42
 */
public class RemoteDubboService implements RemoteService {

	private GenericService genericService;

	@Override
	public Object call(String method, Object data, Map<String, String> attachment) {

		return genericService.$invoke(
				method,
				new String[]{Object.class.getName(), Map.class.getName()},
				//pass trace data by attachment
				new Object[]{data, attachment}
		);
	}

	public RemoteDubboService(GenericService genericService) {
		this.genericService = genericService;
	}
}
