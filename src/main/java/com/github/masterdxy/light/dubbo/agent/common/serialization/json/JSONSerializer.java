package com.github.masterdxy.light.dubbo.agent.common.serialization.json;

import com.alibaba.fastjson.JSON;
import com.github.masterdxy.light.dubbo.agent.common.serialization.Serializer;

/**
 * @author: dongxinyu
 * @date: 17/5/8 下午4:48
 */
public class JSONSerializer implements Serializer<String>{

	@Override
	public String serialize(Object obj) {
		if(obj != null){
			return JSON.toJSONString(obj);
		}
		return null;
	}

	@Override
	public <T> T deserialize(String s, Class<T> aClass) {
		if(s == null){
			return null;
		}
		return JSON.parseObject(s,aClass);
	}
}
