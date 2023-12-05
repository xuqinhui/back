package com.yzw.shop_online.service;

import com.yzw.shop_online.common.result.PageResult;
import com.yzw.shop_online.entity.Goods;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yzw.shop_online.query.Query;
import com.yzw.shop_online.query.RecommendByTabGoodsQuery;
import com.yzw.shop_online.vo.GoodsVO;
import com.yzw.shop_online.vo.IndexTabRecommendVO;
import com.yzw.shop_online.vo.RecommendGoodsVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
public interface GoodsService extends IService<Goods> {
    IndexTabRecommendVO getTabRecommendGoodsByTabId(RecommendByTabGoodsQuery query);


    PageResult<RecommendGoodsVO> getRecommendGoodsByPage(Query query);


    GoodsVO getGoodsDetail(Integer id);


}
