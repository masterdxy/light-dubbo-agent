package com.github.masterdxy.light.dubbo.agent.common.metrics;

import com.alibaba.dubbo.common.utils.NetUtils;
import com.github.masterdxy.light.dubbo.agent.hystrix.metrics.HystrixPrometheusMetricsPublisher;
import com.github.masterdxy.light.metric.client.MetricExporter;
import com.github.masterdxy.light.metric.client.internal.hotspot.ClassLoadingExports;
import com.github.masterdxy.light.metric.client.internal.hotspot.GarbageCollectorExports;
import com.github.masterdxy.light.metric.client.internal.hotspot.MemoryPoolsExports;
import com.github.masterdxy.light.metric.client.internal.hotspot.StandardExports;
import com.github.masterdxy.light.metric.client.internal.hotspot.ThreadExports;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author: dongxinyu
 * @date: 17/5/16 下午1:14
 */
@Component
public class Metrics {

	@Value("${metrics.port}")
	private int metricsPort = 9998;


	@PostConstruct
	public void doRegisterMetricsExporter() {
		new StandardExports().register();
		new MemoryPoolsExports().register();
		new GarbageCollectorExports().register();
		new ThreadExports().register();
		new ClassLoadingExports().register();
		HystrixPrometheusMetricsPublisher.register("Agt_" + NetUtils.getLocalHost().replaceAll("\\.", "_"));

		MetricExporter metricExporter = MetricExporter.getInstance();
        metricExporter.doExport(NetUtils.getLocalHost(), metricsPort);
	}

	public int getMetricsPort() {
		return metricsPort;
	}

	public void setMetricsPort(int metricsPort) {
		this.metricsPort = metricsPort;
	}
}
