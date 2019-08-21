package com.application.service;

import java.util.List;

import com.application.entity.Link;
import com.application.entity.LinkExample;

public interface LinkService extends BaseService<Link, LinkExample> {

	void insertBatch(List<Link> linkList);

}
