package com.application.entity;

import lombok.Data;

@Data
public class IpProxy {
	
	/**
	 * IP地址
	 */
	private String ip;
	
	/**
	 * 端口
	 */
	private Integer port;

}
