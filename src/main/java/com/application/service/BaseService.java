package com.application.service;

import com.github.pagehelper.PageInfo;

import java.util.List;

public interface BaseService<Record, Example> {

    Integer countByExample(Example example);

    Integer deleteByExample(Example example);

    Integer deleteByPrimaryKey(Integer id);

    Integer insert(Record record);

    Integer insertSelective(Record record);

    List<Record> selectByExample(Example example);

    Record selectByPrimaryKey(Integer id);

    Integer updateByExampleSelective(Record record, Example example);

    Integer updateByExample(Record record, Example example);

    Integer updateByPrimaryKeySelective(Record record);

    Integer updateByPrimaryKey(Record record);

    Record load(Integer id);

    Integer save(Record record);

    PageInfo<Record> getPageInfo(Example example, Integer page, Integer pageSize);

}
