package com.folder.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.folder.server.dao.UserDao;
import com.folder.server.dto.ResultDto;
import com.folder.server.dto.UserDto;

@Service
public class UserServiceImp implements UserService {
	
	private ResultDto rDto;
	
	@Autowired UserDao uDao;
	
	public ResultDto findAll() {
		rDto = new ResultDto();
		List<UserDto> resultList = uDao.findAll();
		
		if (resultList != null) {
			rDto.setState(true);
			rDto.setResult(resultList);
		} else {
			rDto.setState(false);
		}
		
		return rDto;
	}
	
	public ResultDto editById(UserDto uDto) {
		return rDto;
	}
	
	public ResultDto delete(int no) {
		return rDto;
	}
	
	public ResultDto save(UserDto uDto) {
		return rDto;
	}
}