package com.github.masterdxy.light.dubbo.agent.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;

/**
 * @author: dongxinyu
 * @date: 17/5/31 上午10:14
 */
public class NetUtil {

	private static Logger logger = LoggerFactory.getLogger(NetUtil.class);

	public static String getEth0Host() {
		Enumeration<NetworkInterface> nifs;
		try {
			nifs = NetworkInterface.getNetworkInterfaces();
			while (nifs.hasMoreElements()) {
				NetworkInterface nif = nifs.nextElement();

				// 获得与该网络接口绑定的 IP 地址，一般只有一个
				Enumeration<InetAddress> addresses = nif.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();

					if (addr instanceof Inet4Address && "eth0".equalsIgnoreCase(nif.getName())) { // 只关心 IPv4 地址
						return addr.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			logger.warn("getEth0Host error ", e);
		}
		return null;
	}

	public static String getHostname(){
		try{
			InetAddress inetAddress = InetAddress.getLocalHost();
			return inetAddress.getHostName();
		}catch(UnknownHostException e){
			System.out.println("unknown host!");
		}
		return "unknown";
	}
}
