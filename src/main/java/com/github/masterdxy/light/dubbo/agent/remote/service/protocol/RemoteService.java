package com.github.masterdxy.light.dubbo.agent.remote.service.protocol;

import java.util.Map;

/**
 * @author: dongxinyu
 * @date: 17/5/9 上午11:39
 */
public interface RemoteService {

	Object call(String method, Object data, Map<String, String> attachment);

}
