package com.github.masterdxy.light.dubbo.agent.connector.tcp.codec;

import com.alibaba.fastjson.JSONException;
import com.github.masterdxy.light.dubbo.agent.common.model.RPCAgentRequest;
import com.github.masterdxy.light.dubbo.agent.common.serialization.Serializer;
import org.apache.commons.lang.StringUtils;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午7:34
 */
public class RPCAgentDecoder extends OneToOneDecoder {

	private Serializer serializer;

	private static final Logger logger = LoggerFactory.getLogger(RPCAgentDecoder.class);

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (msg == null || !(msg instanceof ChannelBuffer)) {
			logger.warn("RPCAgentDecoder decode error : msg is null or type is not instanceof ChannelBuffer. {}", msg);
			return msg;
		}

		String jsonStr = ((ChannelBuffer) msg).toString(Charset.defaultCharset());
		try {
			if (StringUtils.isNotEmpty(jsonStr)) {
				return serializer.deserialize(jsonStr, RPCAgentRequest.class);
			} else {
				logger.warn("decode error : msg is empty.");
			}
		} catch (JSONException jsonException) {
			logger.error("decode json error raw : {}", jsonStr);
		}
		return new RPCAgentRequest();
	}

	public RPCAgentDecoder(Serializer serializer) {
		this.serializer = serializer;
	}
}
