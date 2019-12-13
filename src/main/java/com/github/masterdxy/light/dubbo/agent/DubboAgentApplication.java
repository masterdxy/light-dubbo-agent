package com.github.masterdxy.light.dubbo.agent;

import com.github.masterdxy.light.dubbo.agent.bootstrap.DubboAgentBanner;
import org.apache.commons.lang.StringUtils;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author: dongxinyu
 * @date: 17/5/8 上午11:13
 */
@SpringBootApplication(scanBasePackages = "com.github.masterdxy.light.dubbo.agent")
public class DubboAgentApplication {


	public static void main(String[] args) {

		SpringApplication springApplication = new SpringApplication(DubboAgentApplication.class);
		springApplication.setBanner(new DubboAgentBanner());
		springApplication.setBannerMode(Banner.Mode.CONSOLE);
		springApplication.run(args);


		synchronized (DubboAgentApplication.class){
			while (true)
			{
				try {
					DubboAgentApplication.class.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}


}
