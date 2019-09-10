package com.application.controller;

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

import com.application.common.SpiderPattern;
import com.application.config.HttpClientPool;
import com.application.config.ProxyPool;
import com.application.entity.Book;
import com.application.entity.Movie;
import com.application.service.BookService;
import com.application.service.MovieService;
import com.application.util.HttpClientUtil;

/**
 * 
 * 根据豆瓣ID爬取书籍信息（book.douban.com/subject/{书籍ID}）
 */
@RestController
@Slf4j
public class DoubanIdSpiderController {
	
	@Autowired
	BookService bookService;
	
	@Autowired
	MovieService movieService;
	
	@Autowired
    HttpClientPool httpClientPool;
	
	private static  ExecutorService threadPool = Executors.newFixedThreadPool(100);
	
	@RequestMapping("/douban/id/book")
	public void generate(Long index) throws Exception {
		
		//爬虫并发数限制100
		Semaphore semaphore = new Semaphore(100,true);
		//初始化代理池
		ProxyPool.fillProxyPool();
		try {
			for (long i = index; i > 8000000; i--) {
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
				
				if (webContent.equals("应用出错")) {
					log.info("应用出错:{}", getUrl);
					return;
				}
				
				//豆瓣网络服务故障
				while (webContent.equals("开小差")) {
					Thread.sleep(1000);
					webContent = HttpClientUtil.doGet(getUrl);
				}
				
				if (webContent.contains("豆瓣电影") || webContent.contains("豆瓣音乐")) {
					if (webContent.contains("豆瓣电影")) {
						generateMovie(webContent);
					} else {
						log.info("{}资源类型为{}", index, "音乐");
					}
		        	return;
		        }
		        
		        Document doc = Jsoup.parse(webContent);
		        //书名
		        book.setTitle(doc.select("h1 span").get(0).text());
		        /*if (!book.getTitle().matches(SpiderPattern.BOOK_NAME_FILTER_PATTERN)) {
		        	log.info("书名不符合规范：{}", book.getTitle());
		        	return;
		        }*/
		        if (webContent.contains("点击上传封面图片")) {
		        	book.setCoverUrl("https://img3.doubanio.com/f/shire/5522dd1f5b742d1e1394a17f44d590646b63871d/pics/book-DEFAULT-lpic.gif");
		        } else {
		        	//封面URL
		        	try {
		        		book.setCoverUrl(doc.select("img[title=点击看大图]").get(0).attr("src"));
		        	} catch (Exception e) {
		        		log.info("{}封面大图不存在", index);
		        		try {
		        			Element element = doc.select(".nbg").get(0);
		        			book.setCoverUrl(element.attr("href"));
		        		} catch (Exception ex) {
		        			log.info("{}没有封面图", index);
		        			book.setCoverUrl("https://img3.doubanio.com/f/shire/5522dd1f5b742d1e1394a17f44d590646b63871d/pics/book-DEFAULT-lpic.gif");
		        		}
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
		        
		        //ISBN
		        pattern = Pattern.compile(SpiderPattern.DOUBAN_BOOK_ISBN);
		        m = pattern.matcher(webContent);
		        while (m.find()) {
		        	book.setIsbn(m.group(1).trim());
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

		
		/**
		 * 
		 * 电影信息
		 */
		private void generateMovie(String webContent) {
			
			Movie movie = new Movie();
			movie.setDoubanIndex(index);
			Document doc = Jsoup.parse(webContent);
			//电影名
			movie.setTitle(doc.select("h1 span").get(0).text());
			Pattern pattern = Pattern.compile(SpiderPattern.DOUBAN_MOVIE_CHINESE_NAME);
			Matcher m = pattern.matcher(webContent);
			if (m.find()) {
				movie.setTitle(m.group(1));
			} else {
				pattern = Pattern.compile(SpiderPattern.DOUBAN_MOVIE_CHINESE_NAME_2);
				m = pattern.matcher(webContent);
				if (m.find()) {
					movie.setTitle(m.group(1));
				}
			}
			
	        try {
	        	 //年份
	        	 movie.setYear(doc.select("h1 span").get(1).text());
	         } catch (Exception e) {
	        	 e.printStackTrace();
	        	 log.info("{}没有年份", index);
	         }
	        
        	 try {
        		 movie.setCoverUrl(doc.select("img[title=点击看更多海报]").get(0).attr("src"));
        	 } catch (Exception e) {
        		 try {
        			 movie.setCoverUrl(doc.select("img[title=点击上传封面图片]").get(0).attr("src"));
        		 } catch (Exception ex) {
        			 try {
        				 movie.setCoverUrl(doc.select("img[title=点击看大图]").get(0).attr("src"));
        			 } catch (Exception exc) {
        				 log.info("{}没有海报", index);
        			 }
        		 }
        	 }
	        
	         Elements elements = doc.select("#info .attrs");
	        
	         if (elements.size() > 1) {
	        	 //导演
	        	 movie.setDirector(elements.get(0).text());
	        	 //演员
	        	 movie.setActor(elements.get(1).text());
	         } else if (elements.size() > 0) {
	        	 //导演
	        	 movie.setDirector(elements.get(0).text());
	         }
		       
	         String info = doc.getElementById("info").text();
	         try {
	        	 //电影分类
	        	 movie.setMovieType(info.split("类型:")[1].trim().split(" ")[0]);
	         } catch (Exception e) {
	        	 log.info("{}没有分类", index);
	         }
	        
	         //制片国家/地区
	         pattern = Pattern.compile(SpiderPattern.DOUBAN_MOVIE_COUNTRY);
	         m = pattern.matcher(webContent);
	         while (m.find()) {
	        	 movie.setConuntry(m.group(1).trim());
	         }

	         log.info(movie.toString());
	         movieService.insert(movie);
		 }
	}
}

