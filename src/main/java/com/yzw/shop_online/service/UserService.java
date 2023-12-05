package com.yzw.shop_online.service;

import com.yzw.shop_online.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yzw.shop_online.query.UserLoginQuery;
import com.yzw.shop_online.vo.LoginResultVO;
import com.yzw.shop_online.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
public interface UserService extends IService<User> {

          //用户登录
          LoginResultVO login(UserLoginQuery query);

          User getUserInfo(Integer userId);

          UserVO editUserInfo(UserVO userVO);

          String editUserAvatar(Integer userId, MultipartFile file);
}
