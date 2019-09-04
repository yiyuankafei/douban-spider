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
import com.application.controller.DoubanMovieSpiderController;
import com.application.entity.Book;
import com.application.entity.BookExample;
import com.application.entity.Movie;
import com.application.entity.MovieExample;
import com.application.mapper.BookMapper;
import com.application.mapper.MovieMapper;



@SpringBootApplication
@Slf4j
@MapperScan({"com.application.mapper", "com.application.mapper.custom"})
public class Application extends SpringBootServletInitializer implements CommandLineRunner {
	
	@Autowired
	DoubanMovieSpiderController movieController;
	
	@Autowired
	DoubanIdSpiderController bookController;
	
	@Autowired
	MovieMapper movieMapper;
	
	@Autowired
	BookMapper bookMapper;
	
	public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

	@Override
	public void run(String... arg0) throws Exception {
		log.info("系统启动完成！");
		
		//书籍
		BookExample example = new BookExample();
		example.createCriteria().andIdGreaterThan(3917950);
		example.setOrderByClause("douban_index limit 1");
		List<Book> list = bookMapper.selectByExample(example);
		log.info("===============开始收集数据，起始index:" + list.get(0).getDoubanIndex());
		bookController.generate(list.get(0).getDoubanIndex() - 1);
		
		//电影
		/*MovieExample example = new MovieExample();
		example.createCriteria().andIdGreaterThan(0);
		example.setOrderByClause("douban_index limit 1");
		List<Movie> list = movieMapper.selectByExample(example);
		log.info("===============开始收集数据，起始index:" + list.get(0).getDoubanIndex());
		movieController.generate(list.get(0).getDoubanIndex() - 1);*/
	}

}
