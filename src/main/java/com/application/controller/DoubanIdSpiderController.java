package com.application.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.common.SpiderPattern;
import com.application.config.HttpClientPool;
import com.application.config.ProxyPool;
import com.application.entity.Book;
import com.application.service.BookService;
import com.application.util.HttpClientUtil;

@RestController
@Slf4j
public class DoubanIdSpiderController {
	
	@Autowired
	BookService bookService;
	
	@Autowired
    HttpClientPool httpClientPool;
	
	private static  ExecutorService threadPool = Executors.newFixedThreadPool(20);
	
	@RequestMapping("/douban/id/book")
	public void generat() throws Exception {
		
		//爬虫并发数限制10
		Semaphore semaphore = new Semaphore(20,true);
		//初始化代理池
		ProxyPool.fillProxyPool();
		
		try {
			for (long i = 26779591l; i < 99999999; i++) {
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
			
			try {
			
				Book book = new Book();
				book.setDoubanIndex(index);
				String getUrl = "https://book.douban.com/subject/" + index;
				String webContent = HttpClientUtil.doGet(getUrl);
				
				if (webContent.equals("页面不存在")) {
					log.info("URL页面不存在：{}", getUrl);
					return;
				}
				
				if (webContent.contains("豆瓣电影") || webContent.contains("豆瓣音乐")) {
		        	log.info("{}资源类型为{}", index, webContent.contains("豆瓣电影") ? "电影" : "音乐");
		        	return;
		        }
		        
		        Document doc = Jsoup.parse(webContent);
		        //书名
		        book.setTitle(doc.select("h1 span").get(0).text());
		        if (!book.getTitle().matches(SpiderPattern.BOOK_NAME_FILTER_PATTERN)) {
		        	log.info("书名不符合规范：{}", book.getTitle());
		        	return;
		        }
		        if (!webContent.contains("点击上传封面图片")) {
		        	//封面URL
		        	try {
		        		book.setCoverUrl(doc.select("img[title=点击看大图]").get(0).attr("src"));
		        	} catch (Exception e) {
		        		log.info("{}封面不存在", index);
		        		return;
		        	}
		        }
			        
				//出版机构
		        Pattern pattern = Pattern.compile(SpiderPattern.DOUBAN_BOOK_PUB_ORG);
		        Matcher m = pattern.matcher(webContent);
		        while (m.find()) {
		        	book.setPubOrg(m.group(1).trim());
		        }
		        
		        //出版日期
		        pattern = Pattern.compile(SpiderPattern.DOUBAN_BOOK_PUB_DATE);
		        m = pattern.matcher(webContent);
		        while (m.find()) {
		        	book.setPubDate(m.group(1).trim());
		        }
		        
		        //作者(取第一列)
		        pattern = Pattern.compile(SpiderPattern.DOUBAN_BOOK_AUTHOR);
		        m = pattern.matcher(webContent);
		        if (m.find()) {
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

