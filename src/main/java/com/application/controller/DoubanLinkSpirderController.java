package com.application.controller;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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

import com.application.common.SpiderPattern;
import com.application.config.ProxyPool;
import com.application.entity.Book;
import com.application.entity.Link;
import com.application.entity.LinkExample;
import com.application.service.BookService;
import com.application.service.LinkService;
import com.application.util.HttpClientUtil;


/**
 * 
 * 根据   “喜欢XXX的人也喜欢”  爬取豆瓣书籍信息
 */
@RestController
@Slf4j
public class DoubanLinkSpirderController {
	
	@Autowired
	LinkService linkService;
	
	@Autowired
	BookService bookService;
	
	private  ExecutorService threadPool = Executors.newFixedThreadPool(20);
	
	private LinkedBlockingQueue<Link> queue = new LinkedBlockingQueue<>();
	
	@RequestMapping("/douban/link")
	public void generateLink(String initTitle, String initLink) throws Exception {
		//http://localhost:9098/douban/link?initTitle=%E8%83%8C%E5%8C%85%E5%8D%81%E5%B9%B4&initLink=https://book.douban.com/subject/5264779/
		//初始化链接，作为遍历入口
		Link entrance = new Link();
		entrance.setLink(initLink);
		entrance.setTitle(initTitle);
		LinkExample initExample = new LinkExample();
		initExample.createCriteria().andTitleEqualTo(initTitle);
		if (linkService.countByExample(initExample) == 0) {
			linkService.insertSelective(entrance);
		}
		//爬虫并发数限制10
		Semaphore semaphore = new Semaphore(20,true);
		//初始化代理池
		ProxyPool.fillProxyPool();
		
		LinkExample example = new LinkExample();
		example.createCriteria().andFlagEqualTo(0);
		while (linkService.countByExample(example) > 0) {
			CountDownLatch latch = new CountDownLatch(1);
			List<Link> linkList = linkService.selectByExample(example);
			log.info("***********************");
			log.info("待遍历链接个数：{}", linkList.size());
			log.info("***********************");
			queue.addAll(linkList);
			dojob(semaphore, latch);
			//刚开始数据量少，会爬取重复数据, 用书名作为唯一索引防止重复数据入库
			latch.await();
		}
	}
	
	private void dojob(Semaphore semaphore, CountDownLatch latch) throws Exception {
		while (!queue.isEmpty()) {
			semaphore.acquire();
			Link link = queue.poll();
			threadPool.execute(new bookSpider(link, semaphore));
		}
		//TODO 最初只有一条数据，这里释放后马上又会执行到这里 待优化
		latch.countDown();
	}

	
	class bookSpider implements Runnable {
		
		private Link link;
		private Semaphore semaphore;
		
		public bookSpider (Link link, Semaphore semaphore) {
			this.link = link;
			this.semaphore = semaphore;
		}

		@Override
		public void run() {
			try {
				String webContent = HttpClientUtil.doGet(link.getLink());
				if (webContent.equals("页面不存在")) {
					log.info("页面不存在：{},{}", link.getTitle(), link.getLink());
					return;
				}
				Document doc = Jsoup.parse(webContent);
				
				//爬取书籍信息
				generateBook(webContent, doc);
				
				//遍历推荐列表（喜欢读"XXXX"的人也喜欢  · · · · · · ）
				Element element = doc.select(".clearfix").get(0);
				Elements linkElements = element.select("dl[class!=clear]");
				linkElements.forEach(linkElement -> {
					Link link = new Link();
					try {
						String bookLink = linkElement.select("dt a").get(0).attr("href");
						if (bookLink.indexOf("ebook") > 0) {
							//电子书,暂时不作处理
						} else {
							link.setTitle(linkElement.select("dd a").get(0).text());
							link.setLink(bookLink);
							LinkExample example = new LinkExample();
							example.createCriteria().andTitleEqualTo(link.getTitle());
							if (linkService.countByExample(example) == 0) {
								linkService.insertSelective(link);
							}
						}
					} catch (Exception e) {
						System.out.println(linkElement.toString());
						log.info("解析异常");
					}
				});
				link.setFlag(1);
				linkService.updateByPrimaryKey(link);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				semaphore.release();
			}
		}
	}
	
	/**
	 * 
	 * 解析书籍信息，插入book表
	 */
	private void generateBook(String webContent, Document doc) {
		Book book = new Book();
		//书名
		book.setTitle(doc.select("h1 span").get(0).text());
		log.info("正在处理书籍：{}", book.getTitle());
		try {
			//封面图片地址
			book.setCoverUrl(doc.select("img[title=点击看大图]").get(0).attr("src"));
		} catch (Exception e) {
			log.info("解析异常，没有点击看大图");
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
        
        try {
        	bookService.insert(book);
        } catch (Exception e) {
        	e.printStackTrace();
        	log.info("数据已存在");
        }
        
	}
}
