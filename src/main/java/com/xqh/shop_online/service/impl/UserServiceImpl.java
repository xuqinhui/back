package com.yzw.shop_online.service.impl;

import com.yzw.shop_online.entity.User;
import com.yzw.shop_online.mapper.UserMapper;
import com.yzw.shop_online.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

}
