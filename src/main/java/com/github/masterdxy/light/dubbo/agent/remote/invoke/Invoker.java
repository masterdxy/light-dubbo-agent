package com.github.masterdxy.light.dubbo.agent.remote.invoke;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Preconditions;
import com.netflix.hystrix.HystrixCommand;
import com.uber.jaeger.context.TracingUtils;
import com.github.masterdxy.light.dubbo.agent.common.Constants;
import com.github.masterdxy.light.dubbo.agent.common.log.ResponseLogger;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentRequestOuterClass;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentResponseOuterClass;
import com.github.masterdxy.light.dubbo.agent.hystrix.HystrixConfig;
import com.github.masterdxy.light.dubbo.agent.remote.filter.FilterChain;
import io.opentracing.Span;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import rx.Observable;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午4:01
 */
@Component
public class Invoker {

	@Autowired
	private HystrixConfig hystrixConfig;
	@Autowired
	private FilterChain filterChain;
	@Autowired
	private InvocationContextBuilder invocationContextBuilder;

	private static final Logger logger = LoggerFactory.getLogger(Invoker.class);
	private static final Logger fallback = LoggerFactory.getLogger("fallback");
	private static final Logger reject = LoggerFactory.getLogger("reject");

	/**
	 * invoke rpc request warped as hystrix invocation command
	 *
	 * @param request
	 * @param ctx
	 *
	 * @throws InvokeException
	 */
	public void invoke(AgentRequestOuterClass.AgentRequest request, ChannelHandlerContext ctx) throws InvokeException {

		//invoke remote service method in hystrix thread pool
		long startTime = System.currentTimeMillis();
		Observable<AgentResponseOuterClass.AgentResponse> objectObservable = new HystrixInvocation(request).toObservable();
		//after subscribe current invocation can be invoke in hystrix thread pool
		//async call channel write to write and flush the rpc response
		objectObservable.subscribe(o -> {
			//pipeline task async do this
			Channel channel = ctx.getChannel();
            ResponseLogger.logResponse(channel,startTime, request, o);
			channel.write(o);
		});
	}

	/**
	 * threads default has 1 min idle time
	 */
	private class HystrixInvocation extends HystrixCommand<AgentResponseOuterClass.AgentResponse> {

		private long startTime = System.currentTimeMillis();
		private AgentRequestOuterClass.AgentRequest request;
		private InvocationContext invocationContext;

		public HystrixInvocation(AgentRequestOuterClass.AgentRequest request) {
			//build hystrix invocation cmd
			super(hystrixConfig.buildHystrixInvocationSetter(request.getS(), request.getM()));
			this.request = request;
		}

		@Override
		protected AgentResponseOuterClass.AgentResponse run() throws Exception {
			//build invocation context in non I/O thread, to prevent deadlock proof worker exception
			//may block current thread, because dubbo client call await* to connect service provider
			invocationContext = invocationContextBuilder.getInvocationContext(request);
			Preconditions.checkArgument(invocationContext != null, "invocationContext is null");
			Preconditions.checkArgument(invocationContext.getRemoteService() != null, "invocationContext remote service is null");
			//invoke filters check
			boolean filtered = filterChain.doFilter(invocationContext);
			Preconditions.checkState(filtered, "filter invoke failed.");
			//invoke remote service
			Object data = invocationContext.getRemoteService().call(
					invocationContext.getMethodName(),
					invocationContext.getParamData(),
					invocationContext.getAttachment());

			AgentResponseOuterClass.AgentResponse.Builder responseBuilder = AgentResponseOuterClass.AgentResponse.newBuilder();
			responseBuilder.setRl(request.getRl());
			responseBuilder.setD(data == null ? "" : JSON.toJSONString(data));
			responseBuilder.setS(Constants.SUCCESS);

			//response.setAttachment(invocationContext.getAttachment());

			if (!TracingUtils.getTraceContext().isEmpty()) {
				TracingUtils.getTraceContext().pop().finish();
			}
			return responseBuilder.build();
		}


		@Override
		protected AgentResponseOuterClass.AgentResponse getFallback() {
			//fallback response
			AgentResponseOuterClass.AgentResponse.Builder responseBuilder = AgentResponseOuterClass.AgentResponse.newBuilder();
			responseBuilder.setRl(request.getRl());

			//exception check
			Throwable throwable = getFailedExecutionException();
			logger.warn("execution fallback cost : {} , requestId : {} , service : {}", getExecutionTimeInMilliseconds(), request.getRl(), request.getS(), request.getM());

			if (throwable != null) {
				if (throwable instanceof RpcException) {
					fallback.warn("rpc_exception : {}, request : {}", throwable.getMessage(), request);
					responseBuilder.setS(Constants.REMOTE_SERVICE_EXCEPTION);
				} else if (throwable instanceof IllegalStateException) {
					fallback.warn("illegal_state_exception : {}, request : {}", throwable.getMessage(), request);
					responseBuilder.setS(Constants.AGENT_FILTER_EXCEPTION);
				} else if (throwable instanceof IllegalArgumentException) {
					fallback.warn("illegal_argument_exception :{}, request : {}", throwable.getMessage(), request);
					responseBuilder.setS(Constants.INVOKE_EXCEPTION);
				} else {
					fallback.error("unknown_exception ,request : {}", request, throwable);
					responseBuilder.setS(Constants.UNKNOWN_EXCEPTION);
				}
				return responseBuilder.build();
			} else {
				//CircuitBreakerOpen or ShortCircuited or ResponseTimedOut or ResponseRejected check
				if (isResponseTimedOut()) {
					fallback.warn("Fallback because ResponseTimedOut ,cost : {} , default limit :{} ,method limit : {} ,request : {}", (System.currentTimeMillis() - startTime), hystrixConfig.getTimeoutDefault(), hystrixConfig.getTimeoutDefaultMethod(request.getS(), request.getM()), request);
					responseBuilder.setS(Constants.RESPONSE_TIMEOUT);
				} else if (isResponseRejected()) {
					fallback.warn("Fallback because ThreadPool reject , default limit : {} , service limit : {} , queue reject size default: {}, queue reject service size : {} ,request : {}", hystrixConfig.getThreadPoolCoreSizeDefault(), hystrixConfig.getThreadPoolCoreSizeService(request.getS()),hystrixConfig.getQueueRejectDefault(),hystrixConfig.getQueueRejectService(request.getS()), request);
					responseBuilder.setS(Constants.RESPONSE_REJECTED);
				} else if (isResponseShortCircuited()) {
					fallback.warn("Fallback because ShortCircuited ,request : {}", request);
					responseBuilder.setS(Constants.SHORT_CIRCUITED);
				} else if (isCircuitBreakerOpen()) {
					fallback.warn("Fallback because CircuitBreakerOpen ,request : {}", request);
					responseBuilder.setS(Constants.CIRCUITED_BREAKER_OPEN);
				} else {
					fallback.warn("Fallback unknown , isExecutionComplete :{} , request : {} ,return 20", isExecutionComplete(), request);
					responseBuilder.setS(Constants.UNKNOWN_EXCEPTION);
				}
			}
			if (!TracingUtils.getTraceContext().isEmpty()) {
				Span span = TracingUtils.getTraceContext().pop();
				span.setTag("error", responseBuilder.getS());
				span.finish();
			}

			//TODO each service or method has own fallback data
			return responseBuilder.build();
		}


	}

}
