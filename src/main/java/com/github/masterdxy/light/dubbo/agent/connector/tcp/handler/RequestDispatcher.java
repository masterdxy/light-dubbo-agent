package com.github.masterdxy.light.dubbo.agent.connector.tcp.handler;

import com.google.common.base.Preconditions;
import com.github.masterdxy.light.dubbo.agent.common.log.RequestLogger;
import com.github.masterdxy.light.dubbo.agent.common.model.AgentRequestOuterClass;
import com.github.masterdxy.light.dubbo.agent.remote.invoke.Invoker;
import org.jboss.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午3:43
 */
public class RequestDispatcher extends SimpleChannelUpstreamHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);

    private Invoker invoker;

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        Object msg = e.getMessage();
        if (msg != null && msg instanceof AgentRequestOuterClass.AgentRequest) {
            AgentRequestOuterClass.AgentRequest request = (AgentRequestOuterClass.AgentRequest) msg;
            Preconditions.checkNotNull(request, "rpc agent request is null");
            Preconditions.checkNotNull(request.getS(), "request service is null,please set S");
            Preconditions.checkNotNull(request.getM(), "request method is null,please set M");
            RequestLogger.logRequest(e.getRemoteAddress().toString(), request);
            invoker.invoke(request, ctx);
        } else {
            logger.warn("message received but msg is null , remote : {}", ctx.getChannel().getRemoteAddress());
            close(ctx);
        }
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelDisconnected(ctx, e);
    }

    @Override
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        super.channelClosed(ctx, e);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        logger.warn("exception caught msg : {} , remote : {}", e.getCause() == null ? "error msg is null" : e.getCause().getMessage(), ctx.getChannel() != null ? ctx.getChannel().getRemoteAddress() : "unknown");
        close(ctx);
    }

    private void close(ChannelHandlerContext ctx) {
        Channel channel = ctx.getChannel();
        if (channel != null) {
            channel.close();
        }
    }

    public RequestDispatcher(Invoker invoker) {
        this.invoker = invoker;
    }
}
