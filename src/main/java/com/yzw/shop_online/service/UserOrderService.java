package com.yzw.shop_online.service;

import com.yzw.shop_online.common.result.PageResult;
import com.yzw.shop_online.entity.UserOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yzw.shop_online.entity.UserOrderGoods;
import com.yzw.shop_online.query.CancelGoodsQuery;
import com.yzw.shop_online.query.OrderPreQuery;
import com.yzw.shop_online.query.OrderQuery;
import com.yzw.shop_online.vo.*;
import io.swagger.models.auth.In;
import org.springframework.transaction.annotation.Transactional;

import java.net.Inet4Address;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
public interface UserOrderService extends IService<UserOrder> {
    Integer addGoodsOrder(UserOrderVO orderVO);

    public OrderDetailVO getOrderDetail(Integer id);

    List<UserAddressVO> getAddressListByUserId(Integer userId,Integer addressId);

    SubmitOrderVO getPreOrderDetail(Integer userId);

    SubmitOrderVO getPreNowOrderDetail(OrderPreQuery query);

    SubmitOrderVO getRepurchaseOrderDetail(Integer id);

    PageResult<OrderDetailVO> getOrderList(OrderQuery query);

    OrderDetailVO cancelOrder(CancelGoodsQuery query);
    void deleteOrder(List<Integer> ids, Integer userId);

    void payOrder(Integer id);

    void consignOrder(Integer id);

    OrderDetailVO receiptOrder(Integer id);

    OrderLogisticVO getOrderLogistics(Integer id);

}
