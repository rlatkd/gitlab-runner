package com.folder.server.dao;

import java.util.List;

import com.folder.server.dto.UserDto;

public interface UserDao {
	
	public List<UserDto> findAll();
	public int editById(UserDto uDto);
	public int delete(int no);
	public int save(UserDto uDto);
	
}