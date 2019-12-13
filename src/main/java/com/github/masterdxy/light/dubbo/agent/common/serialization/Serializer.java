package com.github.masterdxy.light.dubbo.agent.common.serialization;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午4:47
 */
public interface Serializer<S> { // <RPCRequest,String>

	String FASTJSON = "fastjson";
	String JACKSON = "jackson";

	 S serialize(Object t);

	<T> T deserialize(S s, Class<T> tClass);

}
