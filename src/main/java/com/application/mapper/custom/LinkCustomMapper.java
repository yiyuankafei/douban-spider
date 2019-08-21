package com.application.mapper.custom;

import java.util.List;

import com.application.entity.Link;

public interface LinkCustomMapper {

	void insertBatch(List<Link> linkList);

}
