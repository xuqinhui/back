package com.yzw.shop_online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yzw.shop_online.common.convert.GoodsConvert;
import com.yzw.shop_online.common.exception.ServerException;
import com.yzw.shop_online.common.result.PageResult;
import com.yzw.shop_online.entity.*;
import com.yzw.shop_online.mapper.*;
import com.yzw.shop_online.query.Query;
import com.yzw.shop_online.query.RecommendByTabGoodsQuery;
import com.yzw.shop_online.service.GoodsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzw.shop_online.vo.GoodsVO;
import com.yzw.shop_online.vo.IndexTabGoodsVO;
import com.yzw.shop_online.vo.IndexTabRecommendVO;
import com.yzw.shop_online.vo.RecommendGoodsVO;
import lombok.AllArgsConstructor;


import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
    public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {
        private IndexRecommendMapper indexRecommendMapper;
        private IndexRecommendTabMapper indexRecommendTabMapper;

        private GoodsDetailMapper goodsDetailMapper;

        private GoodsSpecificationMapper goodsSpecificationMapper;

        private GoodsSpecificationDetailMapper goodsSpecificationDetailMapper;
        @Override
        public IndexTabRecommendVO getTabRecommendGoodsByTabId(RecommendByTabGoodsQuery query) {
            IndexRecommend indexRecommend = indexRecommendMapper.selectById(query.getSubType());
            if (indexRecommend == null){
                throw  new ServerException("推荐分类不存在");

            }
            LambdaQueryWrapper<IndexRecommendTab> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(IndexRecommendTab::getRecommendId,indexRecommend.getId());
            List<IndexRecommendTab> tabList = indexRecommendTabMapper.selectList(wrapper);
            if (tabList.size()==0) {
                throw new ServerException("该分类下不存在tab分类");

            }

            List<IndexTabGoodsVO> list=new ArrayList<>();
            for (IndexRecommendTab item:tabList){
                IndexTabGoodsVO tabGoods = new IndexTabGoodsVO();
                tabGoods.setId(item.getId());
                tabGoods.setName(item.getName());
                Page<Goods> page = new Page<>(query.getPage(),query.getPageSize());
                Page<Goods> goodsPage = baseMapper.selectPage(page,new LambdaQueryWrapper<Goods>().eq(Goods::getTabId,item.getId()));
                List<RecommendGoodsVO> goodsList = GoodsConvert.INSTANCE.convertToRecommendGoodsVOList(goodsPage.getRecords());
                PageResult<RecommendGoodsVO> result = new PageResult<>(page.getTotal(),query.getPageSize(),query.getPage(),page.getPages(),goodsList);
                tabGoods.setGoodsItems(result);
                list.add(tabGoods);

            }
                IndexTabRecommendVO recommendVO = new IndexTabRecommendVO();
                recommendVO.setId(indexRecommend.getId());
                recommendVO.setName(indexRecommend.getName());
                recommendVO.setCover(indexRecommend.getCover());
                recommendVO.setSubTypes(list);

            return  recommendVO;
        }

        @Override
        public PageResult<RecommendGoodsVO> getRecommendGoodsByPage(Query query) {
            Page<Goods> page = new Page<>(query.getPage(),query.getPageSize());
            Page<Goods> goodsPage = baseMapper.selectPage(page,null);
            List<RecommendGoodsVO> result = GoodsConvert.INSTANCE.convertToRecommendGoodsVOList(goodsPage.getRecords());
            return new PageResult<>(page.getTotal(),query.getPageSize(),query.getPage(),page.getPages(),result);
        }

        @Override
        public GoodsVO getGoodsDetail(Integer id) {
            Goods goods = baseMapper.selectById(id);
            if (goods == null){
                throw new ServerException("商品不存在");

            }
            GoodsVO goodsVO = GoodsConvert.INSTANCE.convertToGoodsVO(goods);
            List<GoodsDetail> goodsDetails = goodsDetailMapper.selectList(new LambdaQueryWrapper<GoodsDetail>().eq(GoodsDetail::getGoodsId,goods.getId()));
            goodsVO.setProperties(goodsDetails);

            List<GoodsSpecification> specificationList = goodsSpecificationMapper.selectList(new LambdaQueryWrapper<GoodsSpecification>().eq(GoodsSpecification::getGoodsId,goods.getId()));
            goodsVO.setSpecs(specificationList);

            List<GoodsSpecificationDetail> goodsSpecificationDetails = goodsSpecificationDetailMapper.selectList(new LambdaQueryWrapper<GoodsSpecificationDetail>().eq(GoodsSpecificationDetail::getGoodsId,goods.getId()));
            goodsVO.setSkus(goodsSpecificationDetails);

            List<Goods> goodsList = baseMapper.selectList(new LambdaQueryWrapper<Goods>().eq(Goods::getCategoryId,goods.getCategoryId()).ne(Goods::getId,goods.getId()));
            List<RecommendGoodsVO> goodsVOList = GoodsConvert.INSTANCE.convertToRecommendGoodsVOList(goodsList);
            goodsVO.setSimilarProducts(goodsVOList);
            return goodsVO;
        }
    }
