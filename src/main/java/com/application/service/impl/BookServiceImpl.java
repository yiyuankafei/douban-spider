package com.application.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.application.entity.Book;
import com.application.entity.BookExample;
import com.application.mapper.BookMapper;
import com.application.mapper.custom.BookCustomMapper;
import com.application.service.BookService;

@Service
public class BookServiceImpl extends BaseServiceImpl<BookMapper, Book, BookExample> implements BookService {
	
	@Autowired
	BookCustomMapper bookCustomMapper;

	@Override
	public void insertBatch(List<Book> bookList) {
		bookCustomMapper.insertBatch(bookList);
	}

}
