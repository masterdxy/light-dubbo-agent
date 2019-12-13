package com.github.masterdxy.light.dubbo.agent.common;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import java.nio.charset.Charset;

/**
 * @author: dongxinyu
 * @date: 17/6/9 下午2:06
 */
public interface Constants {

	String APP_NAME = "DubboAgent";

	ChannelBuffer DELIMITER = ChannelBuffers.wrappedBuffer("\r\n\n\r".getBytes(Charset.defaultCharset()));

	int MAX_FRAME_LENGTH = 1024 * 50;


	public static final byte SUCCESS = 1;


	//EXCEPTION [10,20]
	public static final byte REMOTE_SERVICE_EXCEPTION = 10;
	public static final byte AGENT_FILTER_EXCEPTION = 11;
	public static final byte INVOKE_EXCEPTION = 12;

	//[30,40]
	public static final byte RESPONSE_TIMEOUT = 30;
	public static final byte RESPONSE_REJECTED = 31;
	public static final byte SHORT_CIRCUITED = 32;
	public static final byte CIRCUITED_BREAKER_OPEN = 33;


	public static final byte UNKNOWN_EXCEPTION = 20;

}
