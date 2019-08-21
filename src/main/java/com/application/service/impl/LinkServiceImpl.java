package com.application.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.application.entity.Link;
import com.application.entity.LinkExample;
import com.application.mapper.LinkMapper;
import com.application.mapper.custom.LinkCustomMapper;
import com.application.service.LinkService;

@Service
public class LinkServiceImpl extends BaseServiceImpl<LinkMapper, Link, LinkExample> implements LinkService {
	
	@Autowired
	LinkCustomMapper linkCustomMapper;

	@Override
	public void insertBatch(List<Link> linkList) {
		linkCustomMapper.insertBatch(linkList);
	}
	

}
