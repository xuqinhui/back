package com.yzw.shop_online.mapper;

import com.yzw.shop_online.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yzw.shop_online.vo.UserOrderGoodsVO;
import io.swagger.models.auth.In;
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
public interface GoodsMapper extends BaseMapper<Goods> {
        List<UserOrderGoodsVO> getGoodsListByOrderId(@Param("id") Integer id);
}
