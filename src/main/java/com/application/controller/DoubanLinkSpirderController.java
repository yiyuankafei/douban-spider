package com.application.controller;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.application.common.SpiderPattern;
import com.application.entity.Book;
import com.application.entity.Link;
import com.application.entity.LinkExample;
import com.application.service.BookService;
import com.application.service.LinkService;
import com.application.util.HttpClientUtil;


@RestController
@Slf4j
public class DoubanLinkSpirderController {
	
	@Autowired
	LinkService linkService;
	
	@Autowired
	BookService bookService;
	
	@RequestMapping("/douban/link")
	public void generateLink(String initTitle, String initLink) {
		//初始化链接，作为遍历入口
		Link entrance = new Link();
		entrance.setLink(initLink);
		entrance.setTitle(initTitle);
		LinkExample initExample = new LinkExample();
		initExample.createCriteria().andTitleEqualTo(initTitle);
		if (linkService.countByExample(initExample) == 0) {
			linkService.insertSelective(entrance);
		}
		
		LinkExample example = new LinkExample();
		example.createCriteria().andFlagEqualTo(0);
		while (linkService.countByExample(example) > 0) {
			List<Link> linkList = linkService.selectByExample(example);
			System.out.println("***********************");
			System.out.println("待遍历链接个数：" + linkList.size());
			System.out.println("***********************");
			linkList.forEach(link -> {
				getLinks(link.getLink());
				link.setFlag(1);
				linkService.updateByPrimaryKey(link);
			});
		}
	}
	
	@Transactional
	private void getLinks(String url) {
		String webContent = HttpClientUtil.doGet(url);
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
		
	}

	/**
	 * 
	 * 解析书籍信息，插入book表
	 */
	private void generateBook(String webContent, Document doc) {
		// TODO Auto-generated method stub
		Book book = new Book();
		//书名
		book.setTitle(doc.select("h1 span").get(0).text());
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
        
        //作者
        pattern = Pattern.compile(SpiderPattern.DOUBAN_BOOK_AUTHOR);
        m = pattern.matcher(webContent);
        while (m.find()) {
        	book.setAuthor(m.group(1).trim());
        }
        
        bookService.insert(book);
	}
}
