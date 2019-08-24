package com.application.common;

public class SpiderPattern {
	
	/**
	 * 豆瓣书籍-出版机构
	 */
	public static String DOUBAN_BOOK_PUB_ORG = "<span class=\"pl\">出版社:</span>(.+)<br/>";
	/**
	 * 豆瓣书籍-出版日期
	 */
	public static String DOUBAN_BOOK_PUB_DATE = "<span class=\"pl\">出版年:</span>(.+)<br/>";
	/**
	 * 豆瓣书籍-作者名
	 */
	public static String DOUBAN_BOOK_AUTHOR = "\"@type\": \"Person\",\\s*\"name\": \"(.+)\"";
	
	/**
	 * 豆瓣书籍-标签
	 */
	public static String DOUBAN_BOOK_TAG =  "<a href=\"/tag/(.+)\">";
	
	/**
	 * 当当书籍-标签
	 */
	public static String DANGDANG_BOOK_TAG =  "<a href=\"/tag/(.+)\">";
	
	/**
	 * 当当书籍-分页标签
	 */
	public static String DANGDANG_BOOK_TAG_PAGENUMBER =  "bottom-page-turn\">(\\w+)</a>";
	
	/**
	 * 书名过滤(中文、字母、数字、个别符号)
	 */

	public static String BOOK_NAME_FILTER_PATTERN = "[\\u4e00-\\u9fa5|\\w|[-:：，,\"]]*";
}
