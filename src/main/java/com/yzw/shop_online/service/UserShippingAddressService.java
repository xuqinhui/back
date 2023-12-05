package com.yzw.shop_online.service;

import com.yzw.shop_online.entity.UserShippingAddress;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yzw.shop_online.vo.AddressVO;
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
public interface UserShippingAddressService extends IService<UserShippingAddress> {
    Integer saveShippingAddress(AddressVO addressVO);

    Integer editShippingAddress(AddressVO addressVO);


   // List<UserShippingAddress> getShippingAddressList();

   // AddressVO getAddressDetail(Integer id);

   // Integer deleteAddress(Integer id);


    List<AddressVO> getList(Integer userId);

    AddressVO getAddressInfo(Integer id);

    void removeShippingAddress(Integer id);

}
