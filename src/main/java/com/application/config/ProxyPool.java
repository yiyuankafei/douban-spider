package com.application.config;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.application.entity.GetIpResponse;
import com.application.entity.IpProxy;

@Slf4j
public class ProxyPool {
	
	private static ConcurrentHashMap<String, IpProxy> map = new ConcurrentHashMap<>();
	
	private static volatile IpProxy currentProxy = new IpProxy();

	public static IpProxy getInstance() {
		return currentProxy;
	}
	
	public static IpProxy changeProxy(String ip) throws Exception {
		log.info("更换IP，过期IP:{},当前IP:{}", ip, currentProxy.getIp());
		
		if (ip.equals(currentProxy.getIp())) {
			synchronized (ProxyPool.class) {
				if (ip.equals(currentProxy.getIp())) {
					map.remove(ip);
					if (map.size() == 0) {
						fillProxyPool();
					} else {
						currentProxy = map.get(map.keys().nextElement());
						log.info("更换IP：" + currentProxy.getIp());
					}
				}
			}
		} else {
			log.info("IP已被其他线程更换");
		}
		return currentProxy;
	}
	
	public synchronized static void fillProxyPool() throws Exception {
		
		String license = "LA96B92B5557687P";
		String time = String.valueOf(System.currentTimeMillis() / 1000);
		String secret = "3004471B31E11014";
		
		MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update((license + time + secret).getBytes());
        String sign = new BigInteger(1, md5.digest()).toString(16);
		
		String url = "http://api-ip.abuyun.com/obtain?license=" + license + "&time=" + time + "&sign=" + sign + "&cnt=1";
		
		
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity entity = httpResponse.getEntity();
        String webContent = EntityUtils.toString(entity, "UTF-8");
        
        log.info("获取IP：" + webContent);
        
        GetIpResponse response = JSONObject.parseObject(webContent, GetIpResponse.class);
		response.getProxies().forEach(proxy -> {
			String[] split = proxy.split(":");
			IpProxy ipProxy = new IpProxy();
			ipProxy.setIp(split[0]);
			ipProxy.setPort(Integer.parseInt(split[1]));
			map.put(split[0], ipProxy);
		});
		currentProxy = map.get(map.keys().nextElement());
		log.info("填充完毕：当前IP：{}", currentProxy.getIp());
	}
	
	
	
	public static void main(String[] args) throws Exception {
		String license = "LA96B92B5557687P";
		String time = String.valueOf(System.currentTimeMillis() / 1000);
		String secret = "3004471B31E11014";
		
		MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update((license + time + secret).getBytes());
        String sign = new BigInteger(1, md5.digest()).toString(16);
        //String url = "http://api-ip.abuyun.com/obtain?license=" + license + "&time=" + time + "&sign=" + sign + "&cnt=1";
        //String url = "http://api-ip.abuyun.com/whitelist/add?license=" + license + "&time=" + time + "&sign=" + sign + "&ip=180.156.155.136";
        String url = "http://api-ip.abuyun.com/whitelist/list?license=" + license + "&time=" + time + "&sign=" + sign;
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpClient httpClient = HttpClients.createDefault();
		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
		HttpEntity entity = httpResponse.getEntity();
        String webContent = EntityUtils.toString(entity, "UTF-8");
        System.out.println(webContent);
        
	}
}
