package com.github.masterdxy.light.dubbo.agent.connector.tcp.codec;

import com.alibaba.fastjson.JSON;
import com.github.masterdxy.light.dubbo.agent.common.model.RPCAgentResponse;
import com.github.masterdxy.light.dubbo.agent.common.serialization.Serializer;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午7:29
 */
public class RPCAgentEncoder extends OneToOneEncoder {

	private Serializer<String> serializer;
	private static final Logger logger = LoggerFactory.getLogger(RPCAgentEncoder.class);

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (msg != null) {
			String jsonStr = serializer.serialize(msg);
			if (StringUtils.isNotEmpty(jsonStr)) {
//				logger.debug("RPCAgentEncoder after encode msg : {}",jsonStr);
				return ChannelBuffers.wrappedBuffer(jsonStr.getBytes(Charset.defaultCharset()));
			}
			logger.warn("RPCAgentEncoder encode error : jsonStr is empty.");
		}
		logger.warn("encode error : msg is null.");
		return ChannelBuffers.wrappedBuffer(JSON.toJSONString(new RPCAgentResponse()).getBytes(Charset.defaultCharset()));
	}

	public RPCAgentEncoder(Serializer serializer) {
		this.serializer = serializer;
	}
}
