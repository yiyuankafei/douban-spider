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
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.common.SpiderPattern;
import com.application.common.SpirderUrl;
import com.application.config.ProxyPool;
import com.application.entity.Book;
import com.application.entity.DangdangBookTag;
import com.application.entity.DangdangBookTagExample;
import com.application.service.BookService;
import com.application.service.DangdangBookTagService;
import com.application.util.HttpClientUtil;

/**
 * 
 * 根据标签爬取当当网书籍信息
 */
@RestController
@Slf4j
public class DangdangSpiderController {
	
	@Autowired
	BookService bookService;
	
	@Autowired
	DangdangBookTagService dangdangTagService;
	
	private static ExecutorService threadPool = Executors.newFixedThreadPool(10);
	
	/**
	 * 
	 * 获取所有标签，需要手动清洗
	 */
	@RequestMapping("/dangdang/book/tag")
	public void createTag() throws Exception {
		//获取所有标签
		String webContent = HttpClientUtil.doGet(SpirderUrl.DANGDANG_BOOK_TAG_BASE);
		
		Document doc = Jsoup.parse(webContent);
		Elements elements = doc.select("a[ddt-src]");
		elements.forEach(element -> {
			DangdangBookTag tag = new DangdangBookTag();
			tag.setTagName(element.attr("title"));
			tag.setTagUrl(element.attr("ddt-src"));
			dangdangTagService.insert(tag);
		});
	}
	
	/**
	 * 
	 * 获取每个标签页数
	 */
	@RequestMapping("/dangdang/book/tag/pageNumber")
	public void createTagPageNumber() throws Exception {
		Pattern pattern = Pattern.compile(SpiderPattern.DANGDANG_BOOK_TAG_PAGENUMBER);
		DangdangBookTagExample example = new DangdangBookTagExample();
		example.createCriteria().andTagUrlLike("%cp%");
		List<DangdangBookTag> list = dangdangTagService.selectByExample(example);
		list.forEach(tag -> {
			Integer pageNumber = 1;
			String webContent = HttpClientUtil.doGet(tag.getTagUrl());
			Matcher matcher = pattern.matcher(webContent);
			while (matcher.find()) {
				pageNumber = Integer.parseInt(matcher.group(1));
			}
			tag.setPageNum(pageNumber);
			dangdangTagService.updateByPrimaryKey(tag);
		});
	}
	
	@RequestMapping("/dangdang/book")
	public void generat() throws Exception {
		
		//爬虫并发数限制10
		Semaphore semaphore = new Semaphore(10, true);
		//初始化代理池
		ProxyPool.fillProxyPool();
		
		DangdangBookTagExample example = new DangdangBookTagExample();
		example.createCriteria().andTagUrlLike("%cp%");
		List<DangdangBookTag> bookTagList = dangdangTagService.selectByExample(example);
		
		//遍历标签
		bookTagList.forEach(bookTag -> {
			String url = bookTag.getTagUrl();
			String baseUrl = SpirderUrl.DANGDANG_BOOK_BASE;
			try {
				for (int i = 0; i < bookTag.getPageNum(); i++) {
					String targetUrl = baseUrl + "pg" + i + "-" + url.replace(baseUrl, "");
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
				Elements elements = doc.select(".bigimg li");
				log.info("====开始抓取书籍完毕：{}", url);
				List<Book> bookList = new ArrayList<Book>();
				elements.forEach(element -> {
					
					Book book = new Book();
					
					try {
						book.setTitle(element.select("p[name=title] a").get(0).attr("title"));
						book.setCoverUrl(element.select("a[name=itemlist-picture] img").get(0).attr("src"));
					} catch (Exception e) {
						log.info("[{}]没有封面", book.getTitle());
					}
					
					try {
						book.setAuthor(element.select("a[name=itemlist-author]").get(0).text());
					} catch (Exception e) {
						log.info("[{}]没有作者", book.getTitle());
					}
					try {
						book.setPubOrg(element.select("a[name=P_cbs]").get(0).text());
					} catch (Exception e) {
						log.info("[{}]没有出版机构", book.getTitle());
					}
					try {
						book.setPubDate(element.select(".search_book_author").get(0).select("span").get(1).text().substring(1));
					} catch (Exception e) {
						log.info("[{}]没有出版日期", book.getTitle());
					}
					
					bookList.add(book);
				});
				bookService.insertBatch(bookList);
				log.info("====抓取书籍完毕：", url);
			} catch (Exception e) {
				e.printStackTrace();
				//TODO 抓取失败URL入库
				log.info("******************");
				log.info("====抓取书籍发生异常：", url);
				log.info("******************");
			} finally {
				semaphore.release();
			}
			
		}
		
	}

}

