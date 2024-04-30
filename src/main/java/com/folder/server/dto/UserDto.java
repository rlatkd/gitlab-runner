package com.folder.server.dto;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UserDto {
	
	private int no;
	private String name;
	private String email;
	private String pwd;
	private Boolean gender;
	private Boolean del;
	private LocalDate regDate;
	
}
