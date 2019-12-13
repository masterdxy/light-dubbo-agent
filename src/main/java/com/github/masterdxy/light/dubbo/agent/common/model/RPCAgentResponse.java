package com.github.masterdxy.light.dubbo.agent.common.model;

public class RPCAgentResponse {

	public String rl;

	//agent
	public byte s; //status

	//agent
	//public String m; // message

	//rpc response json
	public Object d; // data

	private Object a;

	public String getRl() {
		return rl;
	}

	public void setRl(String rl) {
		this.rl = rl;
	}

	public byte getS() {
		return s;
	}

	public void setS(byte s) {
		this.s = s;
	}

	public Object getD() {
		return d;
	}

	public void setD(Object d) {
		this.d = d;
	}


	public Object getA() {
		return a;
	}

	public void setA(Object a) {
		this.a = a;
	}
}
