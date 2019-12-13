package com.github.masterdxy.light.dubbo.agent.remote.filter;

import com.github.masterdxy.light.dubbo.agent.remote.invoke.InvocationContext;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午8:19
 */
public interface Filter {

	boolean filter(InvocationContext invocationContext);

}
