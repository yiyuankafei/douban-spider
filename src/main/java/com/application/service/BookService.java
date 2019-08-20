package com.application.service;

import java.util.List;

import com.application.entity.Book;
import com.application.entity.BookExample;

public interface BookService extends BaseService<Book, BookExample> {

	void insertBatch(List<Book> bookList);

}
