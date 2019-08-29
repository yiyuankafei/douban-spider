package com.application.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.druid.util.StringUtils;
import com.application.entity.Book;
import com.application.entity.BookExample;
import com.application.entity.BookExample.Criteria;
import com.application.service.BookService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

@RestController
public class BookController {
	
	@Autowired
	BookService service;
	
	@RequestMapping("/api/books")
	public PageInfo<Book> get(String name,
					@RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
					@RequestParam(value = "pageSize",defaultValue = "10") int pageSize) {
		
		BookExample exmaple = new BookExample();
		Criteria criteria = exmaple.createCriteria().andIdGreaterThanOrEqualTo(0);
		if (!StringUtils.isEmpty(name)) {
			criteria.andTitleLike("%" + name + "%");
		}
		
		PageHelper.startPage(pageNum, pageSize);
		List<Book> list = service.selectByExample(exmaple);
		PageInfo<Book> pageList = new PageInfo<>(list);
		return pageList;
	}
}
