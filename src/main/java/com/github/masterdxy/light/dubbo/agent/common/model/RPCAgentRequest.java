package com.github.masterdxy.light.dubbo.agent.common.model;

public class RPCAgentRequest {
	/**
	 * 请求Trace信息
	 */
	private String rl;
	/**
	 * 请求发起时间
	 */
	private long rt; //request time
	/**
	 * 请求接口名
	 */
	private String s;//service
	/**
	 * 请求方法名
	 */
	private String m;//method
	/**
	 * 请求缓存标记
	 */
	private String ck;//cache
	/**
	 * 请求缓存时间
	 */
	private long ct;//cache time
	/**
	 * 请求接口版本
	 */
	private String v;//service version
	/**
	 * 请求附带参数信息
	 */
	private String a;//attachment json


	private String d;//

	public String getRl() {
		return rl;
	}

	public void setRl(String rl) {
		this.rl = rl;
	}

	public long getRt() {
		return rt;
	}

	public void setRt(long rt) {
		this.rt = rt;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public String getM() {
		return m;
	}

	public void setM(String m) {
		this.m = m;
	}

	public String getCk() {
		return ck;
	}

	public void setCk(String ck) {
		this.ck = ck;
	}

	public long getCt() {
		return ct;
	}

	public void setCt(long ct) {
		this.ct = ct;
	}

	public String getD() {
		return d;
	}

	public void setD(String d) {
		this.d = d;
	}


	public String getV() {
		return v;
	}

	public void setV(String v) {
		this.v = v;
	}

	@Override
	public String toString() {
		return "DubboAgentRequest{" +
				"rl='" + rl + '\'' +
				", rt=" + rt +
				", s='" + s + '\'' +
				", m='" + m + '\'' +
				", ck='" + ck + '\'' +
				", ct=" + ct +
				", v='" + v + '\'' +
				", d='" + d + '\'' +
				'}';
	}

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}
}
