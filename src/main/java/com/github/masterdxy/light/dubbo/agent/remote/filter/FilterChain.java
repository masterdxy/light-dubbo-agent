package com.github.masterdxy.light.dubbo.agent.remote.filter;

import com.github.masterdxy.light.dubbo.agent.remote.invoke.InvocationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午4:05
 */
@Component
public class FilterChain {

	@Autowired
	private List<Filter> filters;

	public boolean doFilter(InvocationContext invocationContext){
		for (Filter filter : filters){
			try {
				boolean result = filter.filter(invocationContext);
				if(!result){
					return false;
				}
			}catch (Exception e){
				return false;
			}
		}
		return true;
	}

}
