package com.application.controller;

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
import com.application.config.HttpClientPool;
import com.application.config.ProxyPool;
import com.application.entity.Movie;
import com.application.service.MovieService;
import com.application.util.HttpClientUtil;

/**
 * 
 * 根据豆瓣ID爬取电影信息（movie.douban.com/subject/{电影ID}）
 */
@RestController
@Slf4j
public class DoubanMovieSpiderController {
	
	@Autowired
	MovieService movieService;
	
	@Autowired
    HttpClientPool httpClientPool;
	
	private static  ExecutorService threadPool = Executors.newFixedThreadPool(100);
	
	@RequestMapping("/douban/id/movie")
	public void generate(Long index) throws Exception {
		
		//爬虫并发数限制100
		Semaphore semaphore = new Semaphore(100,true);
		//初始化代理池
		ProxyPool.fillProxyPool();
		try {
			for (long i = index; i > 0; i--) {
				semaphore.acquire();
				threadPool.execute(new MovieSpider(i, semaphore));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	class MovieSpider implements Runnable {
		
		private Long index;
		private Semaphore semaphore;
		
		public MovieSpider (Long index, Semaphore semaphore) {
			this.index = index;
			this.semaphore = semaphore;
		}

		@Override
		public void run() {
			try {
				Movie movie = new Movie();
				movie.setDoubanIndex(index);
				String getUrl = "https://movie.douban.com/subject/" + index;
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
				
				if (webContent.contains("豆瓣读书") || webContent.contains("豆瓣音乐")) {
		        	log.info("{}资源类型为{}", index, webContent.contains("豆瓣音乐") ? "音乐" : "读书");
		        	return;
		        }
		        
		        Document doc = Jsoup.parse(webContent);
		        //电影名
		        movie.setTitle(doc.select("h1 span").get(0).text());
		        try {
		        	//年份
		        	movie.setYear(doc.select("h1 span").get(1).text());
		        } catch (Exception e) {
		        	e.printStackTrace();
		        	log.info("{}没有年份", index);
		        }
		        
		        if (!webContent.contains("点击上传封面图片")) {
		        	try {
		        		movie.setCoverUrl(doc.select("img[title=点击看更多海报]").get(0).attr("src"));
		        	} catch (Exception e) {
		        		e.printStackTrace();
		        		log.info("{}没有海报", index);
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
		        Pattern pattern = Pattern.compile(SpiderPattern.DOUBAN_MOVIE_COUNTRY);
		        Matcher m = pattern.matcher(webContent);
		        while (m.find()) {
		        	movie.setConuntry(m.group(1).trim());
		        }

		        log.info(movie.toString());
		        movieService.insert(movie);
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

