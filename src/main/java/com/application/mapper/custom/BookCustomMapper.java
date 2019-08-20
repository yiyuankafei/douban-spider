package com.application.mapper.custom;

import java.util.List;

import com.application.entity.Book;

public interface BookCustomMapper {

	void insertBatch(List<Book> bookList);

}
