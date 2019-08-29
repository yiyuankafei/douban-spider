package com.application;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;

import com.application.controller.DoubanIdSpiderController;
import com.application.entity.Book;
import com.application.entity.BookExample;
import com.application.mapper.BookMapper;



@SpringBootApplication
@Slf4j
@MapperScan({"com.application.mapper", "com.application.mapper.custom"})
public class Application extends SpringBootServletInitializer implements CommandLineRunner {
	
	@Autowired
	DoubanIdSpiderController controller;
	
	@Autowired
	BookMapper mapper;
	
	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

	@Override
	public void run(String... arg0) throws Exception {
		log.info("系统启动完成！");
		
		BookExample example = new BookExample();
		example.setOrderByClause("douban_index desc limit 1");
		List<Book> list = mapper.selectByExample(example);
		log.info("===============开始收集数据，起始index:" + list.get(0).getDoubanIndex());
		controller.generat(list.get(0).getDoubanIndex());
	}

}
