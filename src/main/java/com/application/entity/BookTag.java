package com.application.entity;

public class BookTag {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column book_tag.id
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column book_tag.tag_name
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    private String tagName;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column book_tag.page_num
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    private Integer pageNum;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column book_tag.id
     *
     * @return the value of book_tag.id
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column book_tag.id
     *
     * @param id the value for book_tag.id
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column book_tag.tag_name
     *
     * @return the value of book_tag.tag_name
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column book_tag.tag_name
     *
     * @param tagName the value for book_tag.tag_name
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    public void setTagName(String tagName) {
        this.tagName = tagName == null ? null : tagName.trim();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column book_tag.page_num
     *
     * @return the value of book_tag.page_num
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    public Integer getPageNum() {
        return pageNum;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column book_tag.page_num
     *
     * @param pageNum the value for book_tag.page_num
     *
     * @mbg.generated Mon Aug 19 14:04:13 CST 2019
     */
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
}