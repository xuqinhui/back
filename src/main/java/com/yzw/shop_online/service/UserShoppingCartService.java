package com.yzw.shop_online.service;

import com.yzw.shop_online.entity.UserShoppingCart;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yzw.shop_online.query.CartQuery;
import com.yzw.shop_online.query.EditCartQuery;
import com.yzw.shop_online.vo.CartGoodsVO;
import io.swagger.models.auth.In;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
public interface UserShoppingCartService extends IService<UserShoppingCart> {
    //添加购物车
    CartGoodsVO addShopCart(CartQuery query);

    List<CartGoodsVO> shopCartList(Integer userId);

    CartGoodsVO editCart(EditCartQuery query);

    void removeCartGoods(Integer userId,List<Integer> ids);

    void editCartSelected(Boolean selected,Integer userId);
}
