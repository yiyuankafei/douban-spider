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
	 * 豆瓣书籍标签
	 */
	public static String DOUBAN_BOOK_TAG =  "<a href=\"/tag/(.+)\">";
	
	/**
	 * 当当书籍标签
	 */
	public static String DANGDANG_BOOK_TAG =  "<a href=\"/tag/(.+)\">";
	
	/**
	 * 当当分页标签
	 */
	public static String DANGDANG_BOOK_TAG_PAGENUMBER =  "bottom-page-turn\">(\\w+)</a>";

}
