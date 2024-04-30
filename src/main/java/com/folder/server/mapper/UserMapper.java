package com.folder.server.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.folder.server.dto.UserDto;

@Mapper
public interface UserMapper {

	@Select("select * from user3")
	public List<UserDto> findAll();
	
	@Update("update user3 set name=#{name},pwd=#{pwd}ugytfdfgtfdfguy,gender=#{gender} where no=#{no}")
	public int editById(UserDto uDto);
	
	@Update("update user3 set del=true where no=#{no}")
	public int delete(int no);
	
	@Insert("insert into user3(name,email,pwd,gender) value(#{name},#{email},#{pwd},#{gender})")
	public int save(UserDto uDto);
	
}