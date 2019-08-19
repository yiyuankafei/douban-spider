package com.application.controller;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLException;

import lombok.extern.slf4j.Slf4j;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.TruncatedChunkException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.common.SpiderPattern;
import com.application.config.HttpClientPool;
import com.application.config.ProxyPool;
import com.application.entity.Book;
import com.application.entity.IpProxy;
import com.application.service.BookService;

@RestController
@Slf4j
public class SpiderController {
	
	@Autowired
	BookService bookService;
	
	@Autowired
    HttpClientPool httpClientPool;
	
	private static  ExecutorService threadPool = Executors.newFixedThreadPool(10);
	
	@RequestMapping("/douban/book")
	public void generat() throws Exception {
		
		//爬虫并发数限制10
		Semaphore semaphore = new Semaphore(10,true);
		//初始化代理池
		ProxyPool.fillProxyPool();
		
		try {
			for (long i = 20427187l; i < 99999999; i++) {
				semaphore.acquire();
				threadPool.execute(new bookSpider(i, semaphore));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	class bookSpider implements Runnable {
		
		private Long index;
		private Semaphore semaphore;
		
		public bookSpider (Long index, Semaphore semaphore) {
			this.index = index;
			this.semaphore = semaphore;
		}

		@Override
		public void run() {
			
			Book book = new Book();
			book.setDoubanIndex(index);
			String getUrl = "https://book.douban.com/subject/" + index;
			String webContent = "";
			
			try {
				Elements elements = null;
				while (elements == null || elements.isEmpty() || elements.get(0).text().equals("禁止访问")) {
					
					HttpGet httpGet = new HttpGet(getUrl);
					httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; W…) Gecko/20100101 Firefox/68.0");
					httpGet.setHeader("Accept-Encoding", null);
					
					IpProxy ipProxy = ProxyPool.getInstance();
					log.info("使用IP:{}-Thread{}---index:{}", ipProxy.getIp(), Thread.currentThread().getName(), index);
					HttpHost proxy = new HttpHost(ipProxy.getIp(), ipProxy.getPort());
					RequestConfig config = RequestConfig.custom().setProxy(proxy).build();  
			        httpGet.setConfig(config);
			        
					//CloseableHttpClient getClient = httpClientPool.getHttpClient();
			        CloseableHttpClient getClient = HttpClients.createDefault();
					
					try (CloseableHttpResponse getResponse = getClient.execute(httpGet)) {
						HttpEntity entity = getResponse.getEntity();
						webContent = EntityUtils.toString(entity, "UTF-8");
					} catch (TruncatedChunkException | SSLException e) {
			        	//丢包重试
			        	elements = null;
			        	continue;
			        }
			        
			        
			        if (webContent.contains("豆瓣电影") || webContent.contains("豆瓣音乐")) {
			        	log.info("{}资源类型为{}", index, webContent.contains("豆瓣电影") ? "电影" : "音乐");
			        	return;
			        }
			        
			        Document doc = Jsoup.parse(webContent);
			        elements = doc.getElementsByTag("title");
			        if (!elements.isEmpty()) {
			        	try {
			        		Element element = elements.get(0);
				        	String title = element.text();
				        	log.info("title:{}", title);
					        if (title.equals("页面不存在") || title.equals("条目不存在")) {
					        	log.info("{}：资源不存在", index);
					        	return;
					        }
				        	//书名
					        book.setTitle(title.substring(0,title.length() - 4));
					        if (!webContent.contains("点击上传封面图片")) {
					        	//封面URL
						        book.setCoverUrl(doc.select("img[title=点击看大图]").get(0).attr("src"));
					        }
			        	} catch (Exception e) {
			        		log.info("解析封面触发更新IP：{}", index, e);
			        		ProxyPool.changeProxy(ipProxy.getIp());
			        	}
			        } else {
			        	log.info("title为空,触发更新IP:{}" ,index);
			        	log.info(webContent);
			        	ProxyPool.changeProxy(ipProxy.getIp());
			        }
			        
				}
		        
		        //出版机构
		        Pattern pattern = Pattern.compile(SpiderPattern.BOOK_PUB_ORG);
		        Matcher m = pattern.matcher(webContent);
		        while (m.find()) {
		        	book.setPubOrg(m.group(1).trim());
		        }
		        
		        //出版日期
		        pattern = Pattern.compile(SpiderPattern.BOOK_PUB_DATE);
		        m = pattern.matcher(webContent);
		        while (m.find()) {
		        	book.setPubDate(m.group(1).trim());
		        }
		        
		        //作者
		        pattern = Pattern.compile(SpiderPattern.BOOK_AUTHOR);
		        m = pattern.matcher(webContent);
		        while (m.find()) {
		        	book.setAuthor(m.group(1).trim());
		        }
		        
		        log.info(book.toString());
		        bookService.insert(book);
			} catch(Exception e) {
				e.printStackTrace();
				log.info("爬取数据异常{}:", index, e);
				return;
			} finally {
				semaphore.release();
			}
		}
		
	}

}

