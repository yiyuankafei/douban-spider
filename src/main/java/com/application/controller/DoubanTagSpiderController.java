package com.application.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.application.common.SpiderPattern;
import com.application.common.SpirderUrl;
import com.application.config.HttpClientPool;
import com.application.config.ProxyPool;
import com.application.entity.BookFromTag;
import com.application.entity.BookTag;
import com.application.entity.BookTagExample;
import com.application.service.BookFromTagService;
import com.application.service.BookTagService;
import com.application.util.HttpClientUtil;

/**
 * 
 * 根据标签爬取豆瓣书籍信息
 */
@RestController
@Slf4j
public class DoubanTagSpiderController {
	
	@Autowired
	BookFromTagService bookFromTagService;
	
	@Autowired
	BookTagService bookTagService;
	
	@Autowired
    HttpClientPool httpClientPool;
	
	private static  ExecutorService threadPool = Executors.newFixedThreadPool(20);
	
	/**
	 * 
	 * 获取所有标签，以及标签对应数据页数
	 */
	@RequestMapping("/douban/book/tag")
	public void createTag() throws Exception {
		//获取所有标签
		String webContent = HttpClientUtil.doGet(SpirderUrl.DOUBAN_BOOK_TAG_BASE);
        Pattern pattern = Pattern.compile(SpiderPattern.DOUBAN_BOOK_TAG);
        Matcher matcher = pattern.matcher(webContent);
        
        while (matcher.find()) {
        	String tagName = matcher.group(1);
        	//每个标签请求一次详情页，获取页码
            webContent = HttpClientUtil.doGet(SpirderUrl.DOUBAN_BOOK_TAG_BASE + tagName);
            Document doc = Jsoup.parse(webContent);
    		Integer maxPage = 1;
    		try {
    			Element paginatorElement = doc.select(".paginator").get(0);
        		Elements pageElements = paginatorElement.select("a");
        		for (Element page : pageElements) {
        			String text = page.text();
        			if (StringUtils.isNumber(text)) {
        				maxPage = Integer.parseInt(text);
        			}
        			
        		}
    		} catch (Exception e) {
    			log.info("页面无分页");
    		}
    		
    		BookTag bookTag = new BookTag();
    		bookTag.setTagName(tagName);
    		bookTag.setPageNum(maxPage);
    		bookTagService.insert(bookTag);
        }
	}
	
	/**
	 * 
	 * 获取书籍信息
	 */
	@RequestMapping("/douban/tag/book")
	public void generat() throws Exception {
		
		//爬虫并发数限制10
		Semaphore semaphore = new Semaphore(20,true);
		//初始化代理池
		ProxyPool.fillProxyPool();
		
		BookTagExample example = new BookTagExample();
		example.createCriteria().andIdGreaterThan(0);
		List<BookTag> bookTagList = bookTagService.selectByExample(example);
		
		//遍历标签
		bookTagList.forEach(bookTag -> {
			String tag = bookTag.getTagName();
			//豆瓣分页限制，最多只能爬取50页数据
			Integer pageCount = bookTag.getPageNum() > 50 ? 50 : bookTag.getPageNum();
			String baseUrl = SpirderUrl.DOUBAN_BOOK_TAG_BASE + tag;
			try {
				for (int i = 0; i < pageCount; i++) {
					String targetUrl = baseUrl + "?start=" + i * 20;
					semaphore.acquire();
					threadPool.execute(new bookSpider(targetUrl, semaphore));
				}
			} catch (Exception e) {
				log.error("爬取数据失败，路径{}", baseUrl , e);
			}
			
		});
		
	}
	
	class bookSpider implements Runnable {
		
		private String url;
		private Semaphore semaphore;
		
		public bookSpider (String url, Semaphore semaphore) {
			this.url = url;
			this.semaphore = semaphore;
		}

		@Override
		public void run() {
			try {
				String content = HttpClientUtil.doGet(url);
				Document doc = Jsoup.parse(content);
				Elements elements = doc.select(".subject-item");
				log.info("====开始抓取书籍完毕：{}", url);
				List<BookFromTag> bookList = new ArrayList<>();
				elements.forEach(element -> {
					Element img = element.select(".pic img").get(0);
					Element info = element.select(".info").get(0);
					Element pub = info.select(".pub").get(0);
					String pubString = pub.text();
					String[] pubInfo = pubString.split("/");
					
					BookFromTag book = new BookFromTag();
					book.setTitle(info.select("h2 a").get(0).attr("title"));
					book.setCoverUrl(img.attr("src"));
					int length = pubInfo.length;
					book.setAuthor(pubInfo[0].trim());
					book.setPubDate(pubInfo[length-2].trim());
					book.setPubOrg(pubInfo[length-3].trim());
					/*if (pubInfo.length > 1) {
						try {
							if (book.getAuthor().indexOf("[") > 0) {
								book.setPubOrg(pubInfo[2].trim());
								book.setPubDate(pubInfo[3].trim());
							} else {
								book.setPubOrg(pubInfo[1].trim());
								book.setPubDate(pubInfo[2].trim());
							}
						} catch (Exception e) {
							log.error("抓取出本信息异常，图书名：{},链接地址：{}", book.getTitle(), url);
						}
						
					}*/
					//用作爬取方式的标记
					book.setDoubanIndex(1l);
					bookList.add(book);
				});
				bookFromTagService.insertBatch(bookList);
				log.info("====抓取书籍完毕：{}", url);
			} catch (Exception e) {
				e.printStackTrace();
				//TODO 抓取失败URL入库
				log.info("******************");
				log.info("====抓取书籍发生异常：{}", url);
				log.info("******************");
			} finally {
				semaphore.release();
			}
			
		}
		
	}

}

