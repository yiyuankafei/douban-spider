package com.application.entity;

import java.util.List;

import lombok.Data;

@Data
public class GetIpResponse {
	
	private Integer expire;
	
	private Integer code;
	
	private Integer left;
	
	private Integer used;
	
	private List<String> proxies;

}
