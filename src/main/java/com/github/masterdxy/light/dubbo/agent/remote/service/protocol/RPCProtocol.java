package com.github.masterdxy.light.dubbo.agent.remote.service.protocol;

/**
 * @author: dongxinyu
 * @date: 17/5/9 上午9:47
 */
public interface RPCProtocol {

	RemoteService refer(RPCUrl url);

	void destroy();
}
