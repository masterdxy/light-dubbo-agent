package com.github.masterdxy.light.dubbo.agent.connector.tcp.codec;

import com.github.masterdxy.light.dubbo.agent.common.Constants;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午7:29
 */
public class LineDelimiterEncoder extends OneToOneEncoder {

    private static final Logger logger = LoggerFactory.getLogger(LineDelimiterEncoder.class);

    @Override
    protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
        if (msg != null) {
            ChannelBuffer msgBuffer = (ChannelBuffer) msg;
            ChannelBuffer after = ChannelBuffers.wrappedBuffer(msgBuffer, Constants.DELIMITER);
            return after;
        } else {
            logger.warn(" LineDelimiterEncoder encode error : msg is null.");
            return null;
        }
    }

}
