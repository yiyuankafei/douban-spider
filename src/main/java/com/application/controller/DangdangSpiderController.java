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
import com.application.entity.Book;
import com.application.entity.DangdangBookTag;
import com.application.entity.DangdangBookTagExample;
import com.application.service.BookService;
import com.application.service.DangdangBookTagService;
import com.application.util.HttpClientUtil;

@RestController
@Slf4j
public class DangdangSpiderController {
	
	@Autowired
	BookService bookService;
	
	@Autowired
	DangdangBookTagService dangdangTagService;
	
	private static  ExecutorService threadPool = Executors.newFixedThreadPool(10);
	
	@RequestMapping("/dangdang/book/tag")
	public void createTag() throws Exception {
		generateTags();
	}
	
	@RequestMapping("/dangdang/book/tag/pageNumber")
	public void createTagPageNumber() throws Exception {
		generateTagsPageNumber();
	}
	
	@RequestMapping("/dangdang/book")
	public void generat() throws Exception {
		
		//爬虫并发数限制10
		Semaphore semaphore = new Semaphore(10,true);
		//初始化代理池
		//ProxyPool.fillProxyPool();
		
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
				System.out.println("====开始抓取书籍完毕：" + url);
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
		String webContent = HttpClientUtil.doGet(SpirderUrl.DANGDANG_BOOK_TAG_BASE);
		
		Document doc = Jsoup.parse(webContent);
		Elements elements = doc.select("a[ddt-src]");
		elements.forEach(element -> {
			String tagName = element.attr("title");
			String url = element.attr("ddt-src");
			DangdangBookTag tag = new DangdangBookTag();
			tag.setTagName(tagName);
			tag.setTagUrl(url);
			dangdangTagService.insert(tag);
		});
	}
	
	private void generateTagsPageNumber() throws Exception {
		
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
	
	public static void main(String[] args) throws Exception {
		/*//获取所有标签
				String webContent = HttpClientUtil.doGet(SpirderUrl.DANGDANG_BOOK_TAG_BASE);
				
				Document doc = Jsoup.parse(webContent);
				Elements elements = doc.select("a[ddt-src]");
				elements.forEach(element -> {
					String tagName = element.attr("title");
					String url = element.attr("ddt-src");
					System.out.println(tagName);
					System.out.println(url);
				});*/
		System.out.println(1);
		String s = "http://category.dangdang.com/cp51.12.00.00.00.00.html";
		String x = "http://category.dangdang.com/";
		System.out.println(s.replace(x, ""));
	}

}

