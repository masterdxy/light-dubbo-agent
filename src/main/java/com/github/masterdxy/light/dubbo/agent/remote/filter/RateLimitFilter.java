package com.github.masterdxy.light.dubbo.agent.remote.filter;

import com.github.masterdxy.light.dubbo.agent.remote.invoke.InvocationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * The type Rate limit filter.
 *
 * @author: dongxinyu
 * @date: 17 /5/8 下午4:04
 */
@Component
@Order()
public class RateLimitFilter implements Filter{

	@Override
	public boolean filter(InvocationContext invocationContext) {
		//todo
		return true;
	}
}
