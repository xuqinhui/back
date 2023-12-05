package com.yzw.shop_online.common.convert;

import com.yzw.shop_online.entity.UserOrder;
import com.yzw.shop_online.vo.OrderDetailVO;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserOrderDetailConvert {
    UserOrderDetailConvert INSTANCE = Mappers.getMapper(UserOrderDetailConvert.class);

    OrderDetailVO convertToORderDetailVO(UserOrder userOrder);


}
