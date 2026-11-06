package com.aicode.mapper;

import com.mybatisflex.core.BaseMapper;
import com.aicode.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 映射层。
 *
 * @author <a href="https://github.com/Bob-bx11">Bob-bx11</a>
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

}
