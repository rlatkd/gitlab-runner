package com.folder.server.service;

import com.folder.server.dto.ResultDto;
import com.folder.server.dto.UserDto;

public interface UserService {
	
	public ResultDto findAll();
	public ResultDto editById(UserDto uDto);
	public ResultDto delete(int no);
	public ResultDto save(UserDto uDto);
	
}