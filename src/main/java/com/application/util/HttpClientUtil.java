package com.application.util;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.TruncatedChunkException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.application.config.ProxyPool;
import com.application.entity.IpProxy;

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
	        	;
		        CloseableHttpResponse getResponse = getClient.execute(httpGet);
	        	HttpEntity entity = getResponse.getEntity();
		        webContent = EntityUtils.toString(entity, "UTF-8");
		        if (getResponse.getStatusLine().getStatusCode() != 200) {
		        	if (webContent.indexOf("<title>页面不存在</title>") > 0) {
		        		return "页面不存在";
		        	}
		        	ProxyPool.changeProxy(ipProxy.getIp());
		        	System.out.println("URL========:" + url);
		        	System.out.println("webContent================" + webContent);
		        	continue;
		        }
		        
		        if (webContent.length() < 5000) {
		        	System.out.println("#################################");
		        	System.out.println(webContent);
		        	System.out.println("#################################");
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
