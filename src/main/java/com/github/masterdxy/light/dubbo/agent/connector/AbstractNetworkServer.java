package com.github.masterdxy.light.dubbo.agent.connector;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AbstractIdleService;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * @author: dongxinyu
 * @date: 17/5/10 上午11:25
 */
public abstract class AbstractNetworkServer extends AbstractIdleService {

	protected String serviceName;
	protected int bossThreadPoolSize;
	protected int workerThreadPoolSize;
	protected int execThreadPoolSize;
	protected long execThreadKeepAliveSecs;
	protected Map<String, Object> channelConfigs;
	protected OneToOneDecoder decoder;
	protected OneToOneEncoder encoder;
	protected RejectedExecutionHandler rejectedExecutionHandler;
	protected ChannelGroup channelGroup;
	protected Function<ChannelPipeline, ChannelPipeline> pipelineModifier;

	protected ServerBootstrap bootstrap;
	protected ExecutionHandler executionHandler;
	protected InetSocketAddress bindAddress;

	protected Map<String,ChannelHandler> channelHandlerMap;

	public AbstractNetworkServer() {
	}
}
