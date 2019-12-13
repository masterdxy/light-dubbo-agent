package com.github.masterdxy.light.dubbo.agent.hystrix;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.HystrixThreadPoolKey;
import com.netflix.hystrix.HystrixThreadPoolProperties;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author: dongxinyu
 * @date: 17/5/12 下午2:36
 */
@Component
public class HystrixConfig {

	//集群和独立部署时不一样
	private static int default_concurrent_limit;
	private static int default_queue_reject_size;

	static {
		if (Boolean.getBoolean("cluster")) {
			System.out.println("Running in cluster mode.");
			default_concurrent_limit = 100;
			default_queue_reject_size = 3000;
		} else {
			System.out.println("Running in standalone mode.");
			default_concurrent_limit = 20;
			default_queue_reject_size = 500;
		}
		System.out.println("default_concurrent_limit = "+ default_concurrent_limit + ",default_queue_reject_size=" + default_queue_reject_size);
	}



	//不随集群部署而改变的变量
	private static int default_timeout = 700;

    public HystrixCommand.Setter buildHystrixInvocationSetter(String serviceKey, String method) {
		return HystrixCommand.Setter
				.withGroupKey(
						HystrixCommandGroupKey.Factory.asKey(serviceKey))
				.andCommandKey(
						HystrixCommandKey.Factory.asKey(serviceKey + "." + method))
				.andThreadPoolKey(
						HystrixThreadPoolKey.Factory.asKey(serviceKey))
				.andThreadPoolPropertiesDefaults(
						//接口级 隔离环境线程池大小(允许的最大并发调用)
						HystrixThreadPoolProperties.Setter().withCoreSize(getThreadPoolCoreSizeDefault())
								.withMaxQueueSize(getQueueMaxDefault())//最大队列10000
								//这个值必须 > 客户端并发数 否则小于部分的请求将无法进入队列
								.withQueueSizeRejectionThreshold(getQueueRejectDefault()))//默认超过500积压就拒绝

				.andCommandPropertiesDefaults(
						//方法级 超时
						HystrixCommandProperties.Setter()
								.withExecutionTimeoutInMilliseconds(getTimeoutDefault())
								//方法级 手动熔断开关
								.withCircuitBreakerForceOpen(false));
	}

	public int getThreadPoolCoreSizeDefault() {
		return ConfigurationManager.getConfigInstance().getInt("hystrix.threadpool.default.coreSize", default_concurrent_limit);
	}

	public int getThreadPoolCoreSizeService(String service) {
		if (StringUtils.isNotEmpty(service)) {
			return ConfigurationManager.getConfigInstance().getInt("hystrix.threadpool." + service + ".coreSize", default_concurrent_limit);
		}
		return default_concurrent_limit;
	}


	public int getTimeoutDefault() {
		return ConfigurationManager.getConfigInstance().getInt("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", default_timeout);
	}


	public int getTimeoutDefaultMethod(String service, String method) {
		if (StringUtils.isNotEmpty(service) && StringUtils.isNotEmpty(method)) {
			return ConfigurationManager.getConfigInstance().getInt("hystrix.command." + service + "." + method + ".execution.isolation.thread.timeoutInMilliseconds", default_timeout);
		}
		return default_timeout;
	}

	public int getQueueRejectDefault() {
		return ConfigurationManager.getConfigInstance().getInt("hystrix.threadpool.default.queueSizeRejectionThreshold", default_queue_reject_size);
	}

	public int getQueueRejectService(String service) {
		if (StringUtils.isNotEmpty(service)) {
			return ConfigurationManager.getConfigInstance().getInt("hystrix.threadpool." + service + ".queueSizeRejectionThreshold", default_queue_reject_size);
		}
		return default_queue_reject_size;
	}

	public int getQueueMaxDefault() {
        int default_max_queue_size = 10000;
        return ConfigurationManager.getConfigInstance().getInt("hystrix.threadpool.default.maxQueueSize", default_max_queue_size);
	}


}
