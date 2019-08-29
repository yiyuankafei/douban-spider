package com.application.util;



import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.application.config.ProxyPool;
import com.application.entity.IpProxy;

@Slf4j
public class HttpClientUtil {
	
	public static String doGet(String url) {
		
		while(true) {
			HttpGet httpGet = new HttpGet(url);
			
			IpProxy ipProxy = ProxyPool.getInstance();
			HttpHost proxy = new HttpHost(ipProxy.getIp(), ipProxy.getPort());
			RequestConfig config = RequestConfig.custom().setProxy(proxy).build();  
	        httpGet.setConfig(config);
			
	        String webContent = "";
	        try (CloseableHttpClient getClient = HttpClients.createDefault()) {
		        CloseableHttpResponse getResponse = getClient.execute(httpGet);
	        	HttpEntity entity = getResponse.getEntity();
		        webContent = EntityUtils.toString(entity, "UTF-8");
		        if (getResponse.getStatusLine().getStatusCode() != 200) {
		        	if (webContent.indexOf("页面不存在") > 0 || webContent.indexOf("条目不存在") > 0) {
		        		return "页面不存在";
		        	}
		        	if (webContent.indexOf("开小差") > 0 || webContent.indexOf("Connect Host Timeout") > 0) {
		        		log.info(webContent.indexOf("开小差") > 0 ? "开小差" : "Connect Host Timeout");
		        		return "开小差";
		        	}
		        	ProxyPool.changeProxy(ipProxy.getIp());
		        	log.info("URL========:{}", url);
		        	log.info("webContent================:{}", webContent);
		        	continue;
		        }
		        
		        if (webContent.length() < 5000) {
		        	log.info("#################################");
		        	log.info(webContent);
		        	log.info("#################################");
		        	ProxyPool.changeProxy(ipProxy.getIp());
		        	continue;
		        }
	        } catch (Exception  e) {
	        	e.printStackTrace();
	        	//丢包重试
	        	continue;
	        }
	        
	        return webContent;
		}
		
	}

}
