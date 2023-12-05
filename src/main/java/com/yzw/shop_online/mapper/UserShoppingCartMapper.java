package com.yzw.shop_online.mapper;

import com.yzw.shop_online.entity.UserShoppingCart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzw.shop_online.vo.CartGoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
@Mapper
public interface UserShoppingCartMapper extends BaseMapper<UserShoppingCart> {
     List<CartGoodsVO> getCartGoodsInfo(@Param("id") Integer id);
}
