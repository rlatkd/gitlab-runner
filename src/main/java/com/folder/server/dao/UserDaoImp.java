package com.folder.server.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.folder.server.dto.UserDto;
import com.folder.server.mapper.UserMapper;

@Repository
public class UserDaoImp implements UserDao {

	@Autowired UserMapper uMapper;
	
	public List<UserDto> findAll() {
		return uMapper.findAll();
	}
	
	public int editById(UserDto uDto) {
		return 0;
	}
	
	public int delete(int no) {
		return 0;
	}
	
	public int save(UserDto uDto) {
		return 0;
	}
}