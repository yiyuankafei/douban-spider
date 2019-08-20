package com.application.common;

public class SpiderPattern {
	
	/**
	 * 书籍-出版机构
	 */
	public static String BOOK_PUB_ORG = "<span class=\"pl\">出版社:</span>(.+)<br/>";
	/**
	 * 书籍-出版日期
	 */
	public static String BOOK_PUB_DATE = "<span class=\"pl\">出版年:</span>(.+)<br/>";
	/**
	 * 书籍-作者名
	 */
	public static String BOOK_AUTHOR = "\"@type\": \"Person\",\\s*\"name\": \"(.+)\"";
	
	/**
	 * 书籍标签
	 */
	public static String BOOK_TAG =  "<a href=\"/tag/(.+)\">";

}
