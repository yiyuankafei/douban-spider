package com.application.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.application.entity.BookFromTag;
import com.application.entity.BookFromTagExample;
import com.application.mapper.BookFromTagMapper;
import com.application.mapper.custom.BookFromTagCustomMapper;
import com.application.service.BookFromTagService;

@Service
public class BookFromTagServiceImpl extends BaseServiceImpl<BookFromTagMapper, BookFromTag, BookFromTagExample> implements BookFromTagService {
	
	@Autowired
	BookFromTagCustomMapper bookFromTagCustomMapper;

	@Override
	public void insertBatch(List<BookFromTag> bookList) {
		bookFromTagCustomMapper.insertBatch(bookList);
	}

}
