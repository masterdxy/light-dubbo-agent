package com.github.masterdxy.light.dubbo.agent.remote.filter;

import com.alibaba.dubbo.common.utils.NetUtils;
import com.uber.jaeger.Configuration;
import com.uber.jaeger.SpanContext;
import com.uber.jaeger.context.TracingUtils;
import com.uber.jaeger.samplers.ConstSampler;
import com.github.masterdxy.light.dubbo.agent.common.config.ConfigCenter;
import com.github.masterdxy.light.dubbo.agent.remote.invoke.InvocationContext;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;

/**
 * @author: dongxinyu
 * @date: 17/5/18 下午6:41
 */
@Component
public class TraceFilter implements Filter {

	private Tracer tracer;

	private Configuration tracerConfiguration;

	private Logger logger = LoggerFactory.getLogger(TraceFilter.class);

	private static String serviceName = "DubboAgent";

	private String globe_trace_key = "globe_agent_trace_enable";
	private double globe_trace_probability = 0.001;

	private String attachment_key = "trace";

	private String host = System.getProperty("hostname", NetUtils.getLocalHost());

	/**
	 * PHP -->  Agent  --> Provider
	 * start    child      child
	 *
	 * @param invocationContext
	 *
	 * @return
	 */
	@Override
	public boolean filter(InvocationContext invocationContext) {
		boolean traceEnable = ConfigCenter.getBoolean(globe_trace_key, true);
		if (traceEnable) {
			Map<String, String> attachment = invocationContext.getAttachment();
			if (attachment == null || attachment.isEmpty()) {
				logger.debug("trace is enable but no attachment is set. invocationContext : {}", invocationContext);
				return true;
			} else {
				//PHP trace data
				Object traceData = attachment.get(attachment_key);
				if (traceData != null) {
					SpanContext spanContext = SpanContext.contextFromString((String) traceData);
					Span agentSpan = tracer.buildSpan(invocationContext.getInvokeKey()).asChildOf(spanContext).start();
					Tags.SPAN_KIND.set(agentSpan, "client");
					Tags.PEER_HOSTNAME.set(agentSpan, host);
					Tags.PEER_PORT.set(agentSpan, 9999);
					Tags.PEER_SERVICE.set(agentSpan, invocationContext.getServiceKey());
					//attachment.put(attachment_key, ((SpanContext) agentSpan.context()).contextAsString());
					TracingUtils.getTraceContext().push(agentSpan);
					return true;
				}
			}
		}
		return true;
	}


	@PostConstruct
	public void initTracer() {
		if (tracer == null) {
			tracerConfiguration = new Configuration(
					serviceName,
					//todo change to remote control sampler
					new Configuration.SamplerConfiguration(ConstSampler.TYPE, globe_trace_probability),
					new Configuration.ReporterConfiguration());
			tracer = tracerConfiguration.getTracer();
		}
	}

	@PreDestroy
	public void closeTracer() {
		if (tracer != null && tracerConfiguration != null) {
			tracerConfiguration.closeTracer();
		}
	}

}
