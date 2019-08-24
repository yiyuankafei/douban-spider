package com.application.service;

import java.util.List;

import com.application.entity.BookFromTag;
import com.application.entity.BookFromTagExample;

public interface BookFromTagService extends BaseService<BookFromTag, BookFromTagExample> {

	void insertBatch(List<BookFromTag> bookList);

}
