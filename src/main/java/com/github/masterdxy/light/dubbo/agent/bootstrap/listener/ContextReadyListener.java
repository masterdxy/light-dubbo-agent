package com.github.masterdxy.light.dubbo.agent.bootstrap.listener;

import com.google.common.util.concurrent.AbstractIdleService;
import com.github.masterdxy.light.dubbo.agent.connector.AbstractNetworkServer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: dongxinyu
 * @date: 17/5/10 下午3:49
 */
@Component
public class ContextReadyListener implements ApplicationListener<ApplicationReadyEvent> {

	private static final Logger logger = LoggerFactory.getLogger(ContextReadyListener.class);


	@Override
	public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
		ConfigurableApplicationContext applicationContext = applicationReadyEvent.getApplicationContext();
		Map<String, AbstractNetworkServer> serverMap = applicationContext.getBeansOfType(AbstractNetworkServer.class);
		startAgentServers(serverMap);


		System.out.println("Started.");
	}

	private void startAgentServers(Map<String,AbstractNetworkServer> serverMap){
		if (serverMap != null && serverMap.size() > 0) {
			logger.info("Starting Agent Server : [{}]", StringUtils.join(serverMap.keySet(),","));
			serverMap.values().forEach(AbstractIdleService::startAsync);
		}else{
			logger.info("No agent server to start. ");
		}
	}


}
