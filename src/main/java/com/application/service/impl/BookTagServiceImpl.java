package com.application.service.impl;

import org.springframework.stereotype.Service;

import com.application.entity.BookTag;
import com.application.entity.BookTagExample;
import com.application.mapper.BookTagMapper;
import com.application.service.BookTagService;

@Service
public class BookTagServiceImpl extends BaseServiceImpl<BookTagMapper, BookTag, BookTagExample> implements BookTagService {

}
