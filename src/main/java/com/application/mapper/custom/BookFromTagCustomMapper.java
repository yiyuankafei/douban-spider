package com.application.mapper.custom;

import java.util.List;

import com.application.entity.BookFromTag;

public interface BookFromTagCustomMapper {

	void insertBatch(List<BookFromTag> bookList);

}
