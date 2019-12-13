package com.github.masterdxy.light.dubbo.agent.remote.invoke;

/**
 * @author: dongxinyu
 * @date: 17/5/11 下午4:30
 */
public class InvokeException extends RuntimeException{

	public InvokeException(String message) {
		super(message);
	}

	public InvokeException(String message, Throwable cause) {
		super(message, cause);
	}
}
