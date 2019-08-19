package com.application.service.impl;

import org.springframework.stereotype.Service;

import com.application.entity.Book;
import com.application.entity.BookExample;
import com.application.mapper.BookMapper;
import com.application.service.BookService;

@Service
public class BookServiceImpl extends BaseServiceImpl<BookMapper, Book, BookExample> implements BookService {

}
