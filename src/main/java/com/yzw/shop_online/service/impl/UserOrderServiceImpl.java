package com.yzw.shop_online.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yzw.shop_online.common.convert.UserAddressConvert;
import com.yzw.shop_online.common.convert.UserOrderDetailConvert;
import com.yzw.shop_online.common.enums.OrderStatusEnum;
import com.yzw.shop_online.common.exception.ServerException;
import com.yzw.shop_online.common.result.PageResult;
import com.yzw.shop_online.entity.*;
import com.yzw.shop_online.mapper.*;
import com.yzw.shop_online.query.CancelGoodsQuery;
import com.yzw.shop_online.query.OrderGoodsQuery;
import com.yzw.shop_online.query.OrderPreQuery;
import com.yzw.shop_online.query.OrderQuery;
import com.yzw.shop_online.service.UserOrderGoodsService;
import com.yzw.shop_online.service.UserOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzw.shop_online.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.Literal;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
@Service
public class UserOrderServiceImpl extends ServiceImpl<UserOrderMapper, UserOrder> implements UserOrderService {
    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private UserShoppingCartMapper  userShoppingCartMapper;
    @Autowired
    private UserShippingAddressMapper userShippingAddressMapper;
    @Autowired
    private UserOrderGoodsMapper userOrderGoodsMapper;


    @Autowired
     private  UserOrderGoodsService userOrderGoodsService;

    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> cancelTask;



    @Async
    public void schedulorderCancel(UserOrder userOrder){
        cancelTask = executorService.schedule(() -> {
            if (userOrder.getStatus() == OrderStatusEnum.WAITING_FOR_PAYMENT.getValue()){
                userOrder.setStatus(OrderStatusEnum.CANCELLED.getValue());
                baseMapper.updateById(userOrder);
            }
        },30, TimeUnit.MINUTES);
    }
    public void cancelScheduledTask(){
        if (cancelTask != null && !cancelTask.isDone()){
            cancelTask.cancel(true);
        }
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public Integer addGoodsOrder(UserOrderVO orderVO) {
        BigDecimal totalPrice  =new BigDecimal(0);
        Integer totalCount= 0;
        BigDecimal totalFreight = new BigDecimal(0);
        UserOrder userOrder = new UserOrder();
        userOrder.setUserId(orderVO.getUserId());
        userOrder.setAddressId(orderVO.getAddressId());

        userOrder.setOrderNumber(UUID.randomUUID().toString());
        userOrder.setDeliveryTimeType(orderVO.getDeliveryType());

        userOrder.setStatus(OrderStatusEnum.WAITING_FOR_PAYMENT.getValue());
        if (orderVO.getBuyerMessage() != null){
            userOrder.setBuyerMessage(orderVO.getBuyerMessage());
        }
        userOrder.setPayType(orderVO.getPayType());
        userOrder.setPayChannel(orderVO.getPayChannel());
        baseMapper.insert(userOrder);
        schedulorderCancel(userOrder);
        List<UserOrderGoods> orderGoodsList = new ArrayList<>();
        for (OrderGoodsQuery goodsVO : orderVO.getGoods()){
            Goods goods = goodsMapper.selectById(goodsVO.getId());
            if (goodsVO.getCount()>goods.getInventory()){
                throw new ServerException(goods.getName()+"库存数量不足");
            }
            UserOrderGoods userOrderGoods = new UserOrderGoods();
            userOrderGoods.setGoodsId(goods.getId());
            userOrderGoods.setName(goods.getName());
            userOrderGoods.setCover(goods.getCover());
            userOrderGoods.setOrderId(userOrder.getId());
            userOrderGoods.setCount(goodsVO.getCount());
            userOrderGoods.setAttrsText(goodsVO.getSkus());
            userOrderGoods.setFreight(goods.getFreight());
            userOrderGoods.setPrice(goods.getPrice());

            BigDecimal freight = new BigDecimal(userOrderGoods.getFreight().toString());
            BigDecimal goodsPrice = new BigDecimal(userOrderGoods.getPrice().toString());
            BigDecimal count = new BigDecimal(userOrderGoods.getCount().toString());

            goods.setInventory(goods.getInventory() - goodsVO.getCount());

            goods.setSalesCount(goodsVO.getCount());
            BigDecimal price = goodsPrice.multiply(count).add(freight);
            totalPrice = totalPrice.add(price);
            totalCount += goodsVO.getCount();
            totalFreight = totalFreight.add(freight);
            orderGoodsList.add(userOrderGoods);
            goodsMapper.updateById(goods);
        }

        userOrderGoodsService.batchUserOrderGoods(orderGoodsList);

        userOrder.setTotalPrice(totalPrice.doubleValue());
        userOrder.setTotalCount(totalCount);
        userOrder.setTotalFreight(totalFreight.doubleValue());
        baseMapper.updateById(userOrder);

        return userOrder.getId();
    }

    @Override
    public OrderDetailVO getOrderDetail(Integer id) {
        UserOrder userOrder = baseMapper.selectById(id);
        if (userOrder == null){
            throw new ServerException("订单信息不存在");
        }
        OrderDetailVO orderDetailVO = UserOrderDetailConvert.INSTANCE.convertToORderDetailVO(userOrder);
        orderDetailVO.setTotalMoney(userOrder.getTotalPrice());

        UserShippingAddress userShippingAddress = userShippingAddressMapper.selectById(userOrder.getAddressId());
        if (userShippingAddress == null){
            throw new ServerException("收货信息不存在");
        }
        orderDetailVO.setReceiverContact(userShippingAddress.getReceiver());
        orderDetailVO.setReceiverMobile(userShippingAddress.getContact());
        orderDetailVO.setReceiverAddress(userShippingAddress.getAddress());

        List<UserOrderGoods> list = userOrderGoodsMapper.selectList(new
                        LambdaQueryWrapper<UserOrderGoods>().eq(UserOrderGoods::getOrderId,id));

        orderDetailVO.setSkus(list);
        orderDetailVO.setPayLatestTime(userOrder.getCreateTime().plusMinutes(30));
        if (orderDetailVO.getPayLatestTime().isAfter(LocalDateTime.now())){
            orderDetailVO.getPayLatestTime();
            Duration duration = Duration.between(LocalDateTime.now(),orderDetailVO.getPayLatestTime());
            orderDetailVO.setCountdown(duration.toMillisPart());

        }
        return  orderDetailVO;
    }

    @Override
    public List<UserAddressVO> getAddressListByUserId(Integer userId, Integer addressId) {
        List<UserShippingAddress> list = userShippingAddressMapper.selectList(new LambdaQueryWrapper<UserShippingAddress>().eq(UserShippingAddress::getUserId,userId));
        UserShippingAddress userShippingAddress = null;
        UserAddressVO userAddressVO;
        if (list.size() == 0){
            return null;
        }
        if (addressId != null){
            userShippingAddress = list.stream().filter(item ->
                    item.getId().equals(addressId)).collect(Collectors.toList()).get(0);
            list.remove(userShippingAddress);

        }
        List<UserAddressVO> addressList = UserAddressConvert.INSTANCE.convertToUserAddressVOList(list);
        if (userShippingAddress != null){
            userAddressVO = UserAddressConvert.INSTANCE.convertToUserAddressVO(userShippingAddress);
            userAddressVO.setSelected(true);
            addressList.add(userAddressVO);
        }
        return  addressList;
    }

    @Override
    public SubmitOrderVO getPreOrderDetail(Integer userId) {
        SubmitOrderVO submitOrderVO = new SubmitOrderVO();

        List<UserShoppingCart> cartList = userShoppingCartMapper.selectList(new LambdaQueryWrapper<UserShoppingCart>().eq(UserShoppingCart::getUserId,userId).eq(UserShoppingCart::getSelected,true));
        if (cartList.size() == 0){
            return null;
        }
        List<UserAddressVO> addressList = getAddressListByUserId(userId,null);

        BigDecimal totalPrice = new BigDecimal(0);
        Integer totalCount = 0;
        BigDecimal totalPayPrice = new BigDecimal(0);
        BigDecimal totalFreight = new BigDecimal(0);

        List<UserOrderGoodsVO> goodsList = new ArrayList<>();
        for (UserShoppingCart shoppingCart : cartList){
            Goods goods = goodsMapper.selectById(shoppingCart.getGoodsId());
            UserOrderGoodsVO userOrderGoodsVO = new UserOrderGoodsVO();
            userOrderGoodsVO.setId(goods.getId());
            userOrderGoodsVO.setName(goods.getName());
            userOrderGoodsVO.setPicture(goods.getCover());
            userOrderGoodsVO.setCount(shoppingCart.getCount());
            userOrderGoodsVO.setAttrsText(shoppingCart.getAttrsText());
            userOrderGoodsVO.setPrice(goods.getOldPrice());
            userOrderGoodsVO.setPayPrice(goods.getPrice());
            userOrderGoodsVO.setTotalPrice(goods.getFreight()+goods.getPrice()*shoppingCart.getCount());
            userOrderGoodsVO.setTotalPayPrice(userOrderGoodsVO.getTotalPrice());

            BigDecimal freight = new BigDecimal(goods.getFreight().toString());
            BigDecimal goodsPrice = new BigDecimal(goods.getPrice().toString());
            BigDecimal count = new BigDecimal(shoppingCart.getCount().toString());

            BigDecimal price= goodsPrice.multiply(count).add(freight);
            totalPrice = totalPrice.add(price);
            totalCount += userOrderGoodsVO.getCount();
            totalPayPrice = totalPayPrice.add(new BigDecimal(userOrderGoodsVO.getTotalPayPrice().toString()));
            totalFreight =totalFreight.add(freight);
            goodsList.add(userOrderGoodsVO);

        }
        OrderInfoVO orderInfoVO = new OrderInfoVO();
        orderInfoVO.setGoodsCount(totalCount);
        orderInfoVO.setTotalPayPrice(totalPayPrice.doubleValue());
        orderInfoVO.setTotalPrice(totalPrice.doubleValue());
        orderInfoVO.setPostFee(totalFreight.doubleValue());

        submitOrderVO.setUserAddresses(addressList);
        submitOrderVO.setGoods(goodsList);
        submitOrderVO.setSummary(orderInfoVO);
        return  submitOrderVO;
    }

    @Override
    public SubmitOrderVO getPreNowOrderDetail(OrderPreQuery query) {
        SubmitOrderVO submitOrderVO=new SubmitOrderVO();
        //1、查询用户收货地址
        List<UserAddressVO> addressList=getAddressListByUserId(query.getUserId(),query.getAddressId());

        List<UserOrderGoodsVO> goodList=new ArrayList<>();

        //2、商品信息
        Goods goods= goodsMapper.selectById(query.getId());
        if (goods==null){
            throw new ServerException("商品信息不存在");
        }
        if (query.getCount()>goods.getInventory()){
            throw new ServerException(goods.getName()+"库存数量不足");
        }
        UserOrderGoodsVO userOrderGoodsVO = new UserOrderGoodsVO();
        userOrderGoodsVO.setId(goods.getId());
        userOrderGoodsVO.setName(goods.getName());
        userOrderGoodsVO.setPicture(goods.getCover());
        userOrderGoodsVO.setCount(query.getCount());
        userOrderGoodsVO.setAttrsText(query.getAttrsText());
        userOrderGoodsVO.setPrice(goods.getOldPrice());
        userOrderGoodsVO.setPayPrice(goods.getPrice());

        BigDecimal freight = new BigDecimal(goods.getFreight().toString());
        BigDecimal price = new BigDecimal(goods.getPrice().toString());
        BigDecimal count = new BigDecimal(query.getCount().toString());
        userOrderGoodsVO.setTotalPrice(price.multiply(count).add(freight).doubleValue());
        userOrderGoodsVO.setTotalPayPrice(userOrderGoodsVO.getTotalPrice());
        goodList.add(userOrderGoodsVO);

//       3、费用综述信息
        OrderInfoVO orderInfoVO = new OrderInfoVO();
        orderInfoVO.setGoodsCount(query.getCount());
        orderInfoVO.setTotalPayPrice(userOrderGoodsVO.getTotalPayPrice());
        orderInfoVO.setTotalPrice(userOrderGoodsVO.getTotalPrice());
        orderInfoVO.setPostFee(goods.getFreight());
        orderInfoVO.setDiscountPrice(goods.getDiscount());

        submitOrderVO.setUserAddresses(addressList);
        submitOrderVO.setGoods(goodList);
        submitOrderVO.setSummary(orderInfoVO);
        return submitOrderVO;
    }

    @Override
    public SubmitOrderVO getRepurchaseOrderDetail(Integer id) {
        SubmitOrderVO submitOrderVO = new SubmitOrderVO();

        UserOrder userOrder = baseMapper.selectById(id);
        List<UserAddressVO> addressList = getAddressListByUserId(userOrder.getUserId(),userOrder.getAddressId());

        List<UserOrderGoodsVO> goodsList = goodsMapper.getGoodsListByOrderId(id);
        OrderInfoVO orderInfoVO = new OrderInfoVO();
        orderInfoVO.setGoodsCount(userOrder.getTotalCount());
        orderInfoVO.setTotalPrice(userOrder.getTotalPrice());
        orderInfoVO.setPostFee(userOrder.getTotalFreight());
        orderInfoVO.setTotalPayPrice(userOrder.getTotalPrice());
        orderInfoVO.setDiscountPrice(0.00);
        submitOrderVO.setUserAddresses(addressList);
        submitOrderVO.setGoods(goodsList);
        submitOrderVO.setSummary(orderInfoVO);
        return submitOrderVO;
    }

    @Override
    public PageResult<OrderDetailVO> getOrderList(OrderQuery query) {
        List<OrderDetailVO> list = new ArrayList<>();
        Page<UserOrder> page = new Page<>(query.getPage(),query.getPageSize());

        LambdaQueryWrapper<UserOrder> wrapper =new LambdaQueryWrapper<>();
        wrapper.eq(UserOrder::getUserId,query.getUserId());
        if (query.getOrderType() !=null && query.getOrderType()!=0){
            wrapper.eq(UserOrder::getStatus,query.getOrderType());

        }
        wrapper.orderByDesc(UserOrder::getCreateTime);

        List<UserOrder> orderRecords = baseMapper.selectPage(page,wrapper).getRecords();
        if (orderRecords.size()==0){
            return new PageResult<>(page.getTotal(),query.getPageSize(),query.getPage(),page.getPages(),list);

        }
        for (UserOrder userOrder :orderRecords){
            OrderDetailVO orderDetailVO = UserOrderDetailConvert.INSTANCE.convertToORderDetailVO(userOrder);
            UserShippingAddress userShippingAddress = userShippingAddressMapper.selectById(userOrder.getAddressId());
            if (userShippingAddress != null){
                orderDetailVO.setReceiverContact(userShippingAddress.getReceiver());
                orderDetailVO.setReceiverAddress(userShippingAddress.getAddress());
                orderDetailVO.setReceiverMobile(userShippingAddress.getContact());
            }
            List<UserOrderGoods> userOrderGoods = userOrderGoodsMapper.selectList(new LambdaQueryWrapper<UserOrderGoods>().eq(UserOrderGoods::getOrderId,userOrder.getId()));
            orderDetailVO.setSkus(userOrderGoods);
            list.add(orderDetailVO);

        }
        return new PageResult<>(page.getTotal(),query.getPageSize(),query.getPage(),page.getPages(),list);
    }

    @Override
    public OrderDetailVO cancelOrder(CancelGoodsQuery query) {
        UserOrder userOrder = baseMapper.selectById(query.getId());
        if (userOrder == null){
            throw new ServerException("订单信息不存在");
        }if (userOrder.getStatus() != OrderStatusEnum.WAITING_FOR_PAYMENT.getValue()){
            throw new ServerException("订单已付款，取消失败");
        }
        userOrder.setStatus(OrderStatusEnum.CANCELLED.getValue());
        userOrder.setCancelReason(query.getCancelReason());
        userOrder.setCloseTime(LocalDateTime.now());
        baseMapper.updateById(userOrder);
        OrderDetailVO orderDetailVO = UserOrderDetailConvert.INSTANCE.convertToORderDetailVO(userOrder);
        UserShippingAddress userShippingAddress = userShippingAddressMapper.selectById(userOrder.getAddressId());
        if (userShippingAddress != null){
            orderDetailVO.setReceiverContact(userShippingAddress.getReceiver());
            orderDetailVO.setReceiverAddress(userShippingAddress.getAddress());
            orderDetailVO.setReceiverMobile(userShippingAddress.getContact());
        }
        List<UserOrderGoods> goodsList = userOrderGoodsMapper.selectList(new LambdaQueryWrapper<UserOrderGoods>().eq(UserOrderGoods::getOrderId,userOrder.getId()));
        orderDetailVO.setSkus(goodsList);
        return orderDetailVO;
    }

    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void deleteOrder(List<Integer> ids, Integer userId) {
       LambdaQueryWrapper<UserOrder> wrapper =new LambdaQueryWrapper<>();
       wrapper.eq(UserOrder::getUserId,userId);
       wrapper.eq(UserOrder::getStatus,OrderStatusEnum.WAITING_FOR_REVIEW.getValue()).or().eq(UserOrder::getStatus,
               OrderStatusEnum.COMPLETED.getValue()).or().eq(UserOrder::getStatus,
               OrderStatusEnum.CANCELLED.getValue());
       List<UserOrder> userOrders = baseMapper.selectList(wrapper);

       List<UserOrder> list = userOrders.stream().filter(item ->
               ids.contains(item.getId())).collect(Collectors.toList());
       if (list.size()==0){
           throw new ServerException("暂无可以删除的订单");

       }
       removeByIds(list);
       for (UserOrder userOrder : list){
           userOrderGoodsMapper.delete(new LambdaQueryWrapper<UserOrderGoods>().eq(UserOrderGoods::getOrderId,userOrder.getId()));
       }
    }

    @Override
    public void payOrder(Integer id) {
        UserOrder userOrder = baseMapper.selectById(id);
        if (userOrder == null){
            throw new ServerException("订单不存在");
        }if (userOrder.getStatus()!=OrderStatusEnum.WAITING_FOR_PAYMENT.getValue()){
            throw new ServerException("该订单暂时无法支付");
        }
        userOrder.setStatus(OrderStatusEnum.WAITING_FOR_SHIPMENT.getValue());
        userOrder.setPayTime(LocalDateTime.now());
        baseMapper.updateById(userOrder);

        cancelScheduledTask();
    }

    @Override
    public void consignOrder(Integer id) {
        UserOrder userOrder = baseMapper.selectById(id);
        if (userOrder == null){
            throw new ServerException("订单不存在");
        }if (userOrder.getStatus() != OrderStatusEnum.WAITING_FOR_SHIPMENT.getValue()){
            throw new ServerException("订单已发货");
        }
        userOrder.setStatus(OrderStatusEnum.WAITING_FOR_DELIVERY.getValue());
        userOrder.setConsignTime(LocalDateTime.now());
        baseMapper.updateById(userOrder);
    }

    @Override
    public OrderDetailVO receiptOrder(Integer id) {
        UserOrder userOrder = baseMapper.selectById(id);
        if (userOrder == null){
            throw new ServerException("订单不存在");
        }if (userOrder.getStatus()!=OrderStatusEnum.WAITING_FOR_DELIVERY.getValue()){
            throw new ServerException("暂时不能确认收货");
        }userOrder.setStatus(OrderStatusEnum.WAITING_FOR_REVIEW.getValue());
        userOrder.setEndTime(LocalDateTime.now());
        baseMapper.updateById(userOrder);
        OrderDetailVO orderDetailVO = UserOrderDetailConvert.INSTANCE.convertToORderDetailVO(userOrder);
        UserShippingAddress userShippingAddress = userShippingAddressMapper.selectById(userOrder.getAddressId());
        if (userShippingAddress != null){
            orderDetailVO.setReceiverContact(userShippingAddress.getReceiver());
            orderDetailVO.setReceiverAddress(userShippingAddress.getAddress());
            orderDetailVO.setReceiverMobile(userShippingAddress.getContact());

        }
        List<UserOrderGoods> goodsList = userOrderGoodsMapper.selectList(new LambdaQueryWrapper<UserOrderGoods>().eq(UserOrderGoods::getOrderId,userOrder.getId()));
        orderDetailVO.setSkus(goodsList);
        return  orderDetailVO;
    }


    @Override
    public OrderLogisticVO getOrderLogistics(Integer id) {
        OrderLogisticVO orderLogisticVO = new OrderLogisticVO();
        UserOrder userOrder = baseMapper.selectById(id);
        if (userOrder == null) {
            throw new ServerException("订单信息不存在");
        }

        if (userOrder.getStatus() != OrderStatusEnum.WAITING_FOR_DELIVERY.getValue() && userOrder.getStatus() != OrderStatusEnum.WAITING_FOR_REVIEW.getValue() && userOrder.getStatus() != OrderStatusEnum.COMPLETED.getValue()) {
            throw new ServerException("暂时查询不到物流信息");
        }

        String logistics = "[{\n" +
                "\t\t\"id\": \"1716355305111031810\",\n" +
                "\t\t\"text\": \"小兔兔到了小福家里，请签收\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"1716355305106837507\",\n" +
                "\t\t\"text\": \"小兔兔到了小熊站，小站正在赶往目的地\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"1716355305106837506\",\n" +
                "\t\t\"text\": \"小兔兔到了小猴站，小站正在分发噢\"\n" +
                "\t},\n" +
                "\t{\n" +
                "\t\t\"id\": \"1716355305102643201\",\n" +
                "\t\t\"text\": \"小兔兔已经发货了\"\n" +
                "\t}\n" +
                "]";
        List<LogisticItemVO> list = JSONArray.parseArray(logistics, LogisticItemVO.class);
        orderLogisticVO.setCount(userOrder.getTotalCount());

        JSONObject companyInfo = new JSONObject();
        companyInfo.put("name", "哪李贵了物流速递");
        companyInfo.put("number", "e0deadac-7189-477e-89f0-e7ca4753d139");
        companyInfo.put("tel", "13813811778");
        orderLogisticVO.setCompany(companyInfo);

        int day = 4;
        for (LogisticItemVO object : list) {
            object.setTime(userOrder.getPayTime().plusDays(day));
            day--;
        }

        orderLogisticVO.setList(list);

        return orderLogisticVO;
    }


}
