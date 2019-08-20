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
import com.application.entity.Book;
import com.application.entity.BookTag;
import com.application.entity.BookTagExample;
import com.application.service.BookService;
import com.application.service.BookTagService;
import com.application.util.HttpClientUtil;

@RestController
@Slf4j
public class SpiderController {
	
	@Autowired
	BookService bookService;
	
	@Autowired
	BookTagService bookTagService;
	
	@Autowired
    HttpClientPool httpClientPool;
	
	private static  ExecutorService threadPool = Executors.newFixedThreadPool(10);
	
	@RequestMapping("/douban/book/tag")
	public void createTag() throws Exception {
		generateTags();
	}
	
	@RequestMapping("/douban/book")
	public void generat() throws Exception {
		
		//爬虫并发数限制10
		Semaphore semaphore = new Semaphore(10,true);
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
			String baseUrl = SpirderUrl.BOOK_TAG_BASE + tag;
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
				System.out.println("====开始抓取书籍完毕：" + url);
				List<Book> bookList = new ArrayList<Book>();
				elements.forEach(element -> {
					Element img = element.select(".pic img").get(0);
					Element info = element.select(".info").get(0);
					Element pub = info.select(".pub").get(0);
					String pubString = pub.text();
					String[] pubInfo = pubString.split("/");
					
					Book book = new Book();
					book.setTitle(info.select("h2 a").get(0).attr("title"));
					book.setCoverUrl(img.attr("src"));
					book.setAuthor(pubInfo[0].trim());
					if (pubInfo.length > 1) {
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
						
					}
					bookList.add(book);
				});
				bookService.insertBatch(bookList);
				System.out.println("====抓取书籍完毕：" + url);
			} catch (Exception e) {
				e.printStackTrace();
				//TODO 抓取失败URL入库
				System.out.println("******************");
				System.out.println("====抓取书籍发生异常：" + url);
				System.out.println("******************");
			} finally {
				semaphore.release();
			}
			
		}
		
	}
	
	private void generateTags() throws Exception {
		
		//获取所有标签
		String webContent = HttpClientUtil.doGet(SpirderUrl.BOOK_TAG_BASE);
        Pattern pattern = Pattern.compile(SpiderPattern.BOOK_TAG);
        Matcher matcher = pattern.matcher(webContent);
        
        while (matcher.find()) {
        	String tagName = matcher.group(1);
        	//每个标签请求一次详情页，获取页码
            webContent = HttpClientUtil.doGet(SpirderUrl.BOOK_TAG_BASE + tagName);
            Document doc = Jsoup.parse(webContent);
    		Integer maxPage = 1;
    		try {
    			Element paginatorElement = doc.select(".paginator").get(0);
        		Elements pageElements = paginatorElement.select("a");
        		for (Element page : pageElements) {
        			String text = page.text();
        			if (StringUtils.isNumber(text)) {
        				Integer thisPage = Integer.parseInt(text);
            			if (thisPage > maxPage) {
            				maxPage = thisPage;
            			}
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

}

