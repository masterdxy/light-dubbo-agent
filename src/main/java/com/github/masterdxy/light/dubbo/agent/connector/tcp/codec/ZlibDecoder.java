package com.github.masterdxy.light.dubbo.agent.connector.tcp.codec;

import com.github.masterdxy.light.dubbo.agent.common.utils.ZLibUtil;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: dongxinyu
 * @date: 17/6/6 上午2:01
 */
public class ZlibDecoder extends OneToOneDecoder {

	private static Logger logger = LoggerFactory.getLogger(ZlibDecoder.class);

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		try {
			return ChannelBuffers.copiedBuffer(ZLibUtil.decompress(((ChannelBuffer) msg).array()));
		} catch (Exception e) {
			logger.warn("zlib decode error return null. : {},{}", msg, e.getMessage());
			return null;
		}
	}
}
