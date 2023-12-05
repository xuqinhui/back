package com.yzw.shop_online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yzw.shop_online.common.convert.AddressConvert;
import com.yzw.shop_online.common.enums.AddressDefaultEnum;
import com.yzw.shop_online.common.enums.CategoryRecommendEnum;
import com.yzw.shop_online.common.exception.ServerException;
import com.yzw.shop_online.entity.Category;
import com.yzw.shop_online.entity.UserShippingAddress;
import com.yzw.shop_online.mapper.IndexRecommendMapper;
import com.yzw.shop_online.mapper.UserShippingAddressMapper;
import com.yzw.shop_online.service.UserShippingAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzw.shop_online.vo.AddressVO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
@Service
@AllArgsConstructor
public class UserShippingAddressServiceImpl extends ServiceImpl<UserShippingAddressMapper, UserShippingAddress> implements UserShippingAddressService {
    private UserShippingAddressMapper userShippingAddressMapper;
    @Override
    public Integer saveShippingAddress(AddressVO addressVO) {
        UserShippingAddress convert = AddressConvert.INSTANCE.convert(addressVO);
        if (addressVO.getIsDefault()== AddressDefaultEnum.DEFAULT_ADDRESS.getValue()){
            List<UserShippingAddress> list= baseMapper.selectList(new LambdaQueryWrapper<UserShippingAddress>().eq(UserShippingAddress::getIsDefault,AddressDefaultEnum.DEFAULT_ADDRESS.getValue()));
            if (list.size()>0){
                throw new ServerException("已经存在默认路径，请勿重复操作");

            }
        }
        save(convert);
        return convert.getId();

    }

    @Override
    public Integer editShippingAddress(AddressVO addressVO) {

        UserShippingAddress userShippingAddress = baseMapper.selectById(addressVO.getId());
        if (userShippingAddress == null){
            throw new ServerException("地址不存在");
        }
        if (addressVO.getIsDefault() == AddressDefaultEnum.DEFAULT_ADDRESS.getValue()){
            //查询该用户是否已经存在默认地址
            UserShippingAddress address = baseMapper.selectOne(new LambdaQueryWrapper<UserShippingAddress>().eq(UserShippingAddress::getUserId,
                    addressVO.getUserId()).eq(UserShippingAddress::getIsDefault,
                    AddressDefaultEnum.DEFAULT_ADDRESS.getValue()));
            //如果存在将之前的默认地址修改掉
            if (address!=null){
                address.setIsDefault(AddressDefaultEnum.NOT_DEFAULT_ADDRESS.getValue());
                updateById(address);
            }
        }
        UserShippingAddress address= AddressConvert.INSTANCE.convert(addressVO);
        updateById(address);
        return address.getId();
    }

    @Override
    public List<AddressVO> getList(Integer userId) {
        LambdaQueryWrapper<UserShippingAddress> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserShippingAddress::getUserId, userId);
//        根据是否为默认地址和创建时间倒序排列
        wrapper.orderByDesc(UserShippingAddress::getIsDefault);
        wrapper.orderByDesc(UserShippingAddress::getCreateTime);
        List<UserShippingAddress> list = baseMapper.selectList(wrapper);
        List<AddressVO> results = AddressConvert.INSTANCE.convertToAddressVOList(list);
        return results;
    }

    @Override
    public AddressVO getAddressInfo(Integer id) {
        UserShippingAddress userShippingAddress = baseMapper.selectById(id);
        if (userShippingAddress == null) {
            throw new ServerException("地址不存在");
        }
        AddressVO addressVO = AddressConvert.INSTANCE.convertToAddressVO(userShippingAddress);
        return addressVO;
    }

    @Override
    public void removeShippingAddress(Integer id) {
        removeById(id);
    }


//    @Override
//    public List<UserShippingAddress> getShippingAddressList() {
//        LambdaQueryWrapper<UserShippingAddress> wrapper = new LambdaQueryWrapper<>();
//        //wrapper.eq(UserShippingAddress::getIsDefault,AddressDefaultEnum.NOT_DEFAULT_ADDRESS.getValue());
//        wrapper.orderByDesc(UserShippingAddress::getCreateTime);
//        List<UserShippingAddress> list= baseMapper.selectList(wrapper);
//        return list;
//    }
//
//    @Override
//    public AddressVO getAddressDetail(Integer id) {
//           UserShippingAddress userShippingAddress = baseMapper.selectById(id);
//           if (userShippingAddress == null){
//               throw new ServerException("地址不存在");
//           }
//           AddressVO addressVO = AddressConvert.INSTANCE.convertToAddressVO(userShippingAddress);
//           return addressVO;
//    }
//
//    @Override
//    public Integer deleteAddress(Integer id) {
//        UserShippingAddress userShippingAddress = baseMapper.selectById(id);
//        if (userShippingAddress == null) {
//            throw new ServerException("地址不存在");
//        }
//        return userShippingAddressMapper.deleteById(id);
//    }




}
