package com.application.mapper;

import com.application.entity.DangdangBookTag;
import com.application.entity.DangdangBookTagExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface DangdangBookTagMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    long countByExample(DangdangBookTagExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int deleteByExample(DangdangBookTagExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int insert(DangdangBookTag record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int insertSelective(DangdangBookTag record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    List<DangdangBookTag> selectByExample(DangdangBookTagExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    DangdangBookTag selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int updateByExampleSelective(@Param("record") DangdangBookTag record, @Param("example") DangdangBookTagExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int updateByExample(@Param("record") DangdangBookTag record, @Param("example") DangdangBookTagExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int updateByPrimaryKeySelective(DangdangBookTag record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table dangdang_book_tag
     *
     * @mbg.generated Tue Aug 20 17:55:26 CST 2019
     */
    int updateByPrimaryKey(DangdangBookTag record);
}