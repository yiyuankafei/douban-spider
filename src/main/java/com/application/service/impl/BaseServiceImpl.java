package com.application.service.impl;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.base.Preconditions;
import com.application.service.BaseService;

@Slf4j
public abstract class BaseServiceImpl<Mapper, Record, Example> implements BaseService<Record, Example> {


    @Autowired
    public Mapper mapper;

    @Override
    public Integer countByExample(Example example) {
        try {
            Method countByExample = mapper.getClass().getDeclaredMethod("countByExample", example.getClass());
            Object result = countByExample.invoke(mapper, example);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
            log.error("countByExample error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    @Override
    public Integer deleteByExample(Example example) {
        try {
            Method deleteByExample = mapper.getClass().getDeclaredMethod("deleteByExample", example.getClass());
            Object result = deleteByExample.invoke(mapper, example);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
        	log.error("deleteByExample error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    @Override
    public Integer deleteByPrimaryKey(Integer id) {
        try {
            Method deleteByPrimaryKey = mapper.getClass().getDeclaredMethod("deleteByPrimaryKey", id.getClass());
            Object result = deleteByPrimaryKey.invoke(mapper, id);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
        	log.error("deleteByPrimaryKey error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    @Override
    public Integer insert(Record record) {
        try {
            Method insert = mapper.getClass().getDeclaredMethod("insert", record.getClass());
            Object result = insert.invoke(mapper, record);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
            log.error("insert error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    @Override
    public Integer insertSelective(Record record) {
        try {
            Method insertSelective = mapper.getClass().getDeclaredMethod("insertSelective", record.getClass());
            Object result = insertSelective.invoke(mapper, record);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
            log.error("insertSelective error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }


    @SuppressWarnings("unchecked")
	@Override
    public List<Record> selectByExample(Example example) {
        try {
            Method selectByExample = mapper.getClass().getDeclaredMethod("selectByExample", example.getClass());
            Object result = selectByExample.invoke(mapper, example);
            return (List<Record>) result;
        } catch (ReflectiveOperationException e) {
            log.error("selectByExample error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }


    @SuppressWarnings("unchecked")
	@Override
    public Record selectByPrimaryKey(Integer id) {
        try {
            Method selectByPrimaryKey = mapper.getClass().getDeclaredMethod("selectByPrimaryKey", id.getClass());
            Object result = selectByPrimaryKey.invoke(mapper, id);
            return (Record) result;
        } catch (ReflectiveOperationException e) {
            log.error("selectByPrimaryKey error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

	@Override
    public Integer updateByExampleSelective(Record record, Example example) {
        try {
            Method updateByExampleSelective = mapper.getClass().getDeclaredMethod("updateByExampleSelective", record.getClass(), example.getClass());
            Object result = updateByExampleSelective.invoke(mapper, record, example);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
            log.error("updateByExampleSelective error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    @Override
    public Integer updateByExample(Record record, Example example) {
        try {
            Method updateByExample = mapper.getClass().getDeclaredMethod("updateByExample", record.getClass(), example.getClass());
            Object result = updateByExample.invoke(mapper, record, example);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
            log.error("updateByExample error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }


    @Override
    public Integer updateByPrimaryKeySelective(Record record) {
        try {
            Method updateByPrimaryKeySelective = mapper.getClass().getDeclaredMethod("updateByPrimaryKeySelective", record.getClass());
            Object result = updateByPrimaryKeySelective.invoke(mapper, record);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
            log.error("updateByPrimaryKeySelective error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }


    @Override
    public Integer updateByPrimaryKey(Record record) {
        try {
            Method updateByPrimaryKey = mapper.getClass().getDeclaredMethod("updateByPrimaryKey", record.getClass());
            Object result = updateByPrimaryKey.invoke(mapper, record);
            return Integer.parseInt(String.valueOf(result));
        } catch (ReflectiveOperationException e) {
            log.error("updateByPrimaryKey error,{}", ExceptionUtils.getFullStackTrace(e));
        }
        return null;
    }

    @Override
    public Record load(Integer id) {
        Preconditions.checkNotNull(id, "id不能为空");
        return selectByPrimaryKey(id);
    }


    @Override
    public Integer save(Record record) {
        if (record != null) {
            try {
                Method getId = record.getClass().getDeclaredMethod("getId");
                try {
                    Method getCreatedDt = record.getClass().getDeclaredMethod("getCreatedDt");
                    Date createdDt = (Date) getCreatedDt.invoke(record);
                    if (createdDt == null) {
                        Method setCreatedDt = record.getClass().getDeclaredMethod("setCreatedDt", Date.class);
                        setCreatedDt.invoke(record, new Date());
                    }
                } catch (ReflectiveOperationException e) {
                    //ignore
                }
                Object id = getId.invoke(record);
                if (id == null) {
                    id = insertSelective(record);
                } else {
                    id = updateByPrimaryKeySelective(record);
                }
                return Integer.parseInt(String.valueOf(id));
            } catch (ReflectiveOperationException e) {
                log.error("save error,{}", ExceptionUtils.getFullStackTrace(e));
            }

        }

        return null;
    }


    @Override
    public PageInfo<Record> getPageInfo(Example example, Integer page, Integer pageSize) {

        PageHelper.startPage(page, pageSize);

        List<Record> list = selectByExample(example);

        PageInfo<Record> pageInfo = new PageInfo<>(list);

        return pageInfo;
    }

}
