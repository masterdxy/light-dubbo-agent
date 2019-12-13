package com.github.masterdxy.light.dubbo.agent.common.serialization.json;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.masterdxy.light.dubbo.agent.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午4:48
 */
public class JacksonSerializer implements Serializer<String>{

	private static ObjectMapper objectMapper = new ObjectMapper();
	private static Logger logger = LoggerFactory.getLogger(JacksonSerializer.class);

	@Override
	public String serialize(Object obj) {
		if(obj != null){
			try {
				return objectMapper.writeValueAsString(obj);
			} catch (JsonProcessingException e) {
				logger.warn("jackson serialize failed .",e);
			}
		}
		return null;
	}

	@Override
	public <T> T deserialize(String s, Class<T> aClass) {
		if(s == null){
			return null;
		}
		try {
			return objectMapper.readValue(s,aClass);
		} catch (IOException e) {
			logger.warn("jackson deserialize failed .",e);
			return null;
		}
	}


}
