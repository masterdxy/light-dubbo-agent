package com.github.masterdxy.light.dubbo.agent.bootstrap.configuration;

import com.github.masterdxy.light.dubbo.agent.remote.filter.Filter;
import com.github.masterdxy.light.dubbo.agent.remote.service.RemoteServiceMgr;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.RPCProtocol;
import com.github.masterdxy.light.dubbo.agent.remote.service.protocol.dubbo.DubboGenericServiceProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @author tomoyo
 */
@Configuration
@ConfigurationProperties(locations = "classpath:/rpc/rpc-${spring.profiles.active}.properties", prefix = "rpc")
public class RpcConfiguration {

	/**
	 * rpc.zookeeper=127.0.0.1:2181
	 rpc.protocol=dubbo
	 rpc.appname=rpcagent
	 rpc.worker=10
	 */

	private String zookeeper;
	private String protocol;
	private String appname;

	private String zkproxy;
	private String securecode;

	@Autowired
	private List<Filter> filters;


	@Bean
	public RPCProtocol getRPCProtocol(){
		//default is dubbo
		return new DubboGenericServiceProtocol(zookeeper,appname);
	}

	@Bean
	public RemoteServiceMgr getRemoteServiceMgr(){
		RemoteServiceMgr mgr = new RemoteServiceMgr(getRPCProtocol());
		//pre init consumer TODO add version data to zkproxy response
		mgr.preInit(zkproxy,securecode);
		return mgr;
	}

//	@Bean
//	public FilterChain getFilterChain(){
//		return new FilterChain(filters);
//	}
//
//	@Bean
//	public Invoker getInvoker(){
//		ExecutorService workerExecutor = Executors.newFixedThreadPool(worker,
//				new ThreadFactoryBuilder()
//						.setDaemon(true)
//						.setNameFormat( "rpc-worker-thread-%d")
//						.build());
//		return new Invoker(getFilterChain(),workerExecutor);
//	}



	public String getZookeeper() {
		return zookeeper;
	}

	public void setZookeeper(String zookeeper) {
		this.zookeeper = zookeeper;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}

	public String getZkproxy() {
		return zkproxy;
	}

	public void setZkproxy(String zkproxy) {
		this.zkproxy = zkproxy;
	}

	public String getSecurecode() {
		return securecode;
	}

	public void setSecurecode(String securecode) {
		this.securecode = securecode;
	}
}
