package com.application.service.impl;

import org.springframework.stereotype.Service;

import com.application.entity.Movie;
import com.application.entity.MovieExample;
import com.application.mapper.MovieMapper;
import com.application.service.MovieService;

@Service
public class MovieServiceImpl extends BaseServiceImpl<MovieMapper, Movie, MovieExample> implements MovieService {
	
}
