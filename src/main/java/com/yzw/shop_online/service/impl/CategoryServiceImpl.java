package com.yzw.shop_online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yzw.shop_online.common.convert.GoodsConvert;
import com.yzw.shop_online.common.enums.CategoryRecommendEnum;
import com.yzw.shop_online.controller.GoodsController;
import com.yzw.shop_online.entity.Category;
import com.yzw.shop_online.entity.Goods;
import com.yzw.shop_online.mapper.CategoryMapper;
import com.yzw.shop_online.mapper.GoodsMapper;
import com.yzw.shop_online.service.CategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzw.shop_online.vo.CategoryChildrenGoodsVO;
import com.yzw.shop_online.vo.CategoryVO;
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
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    private final GoodsMapper goodsMapper;
    @Override
    public List<Category> getIndexCategoryList() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        //
        wrapper.eq(Category::getIsRecommend, CategoryRecommendEnum.ALL_RECOMMEND.getValue());
        wrapper.orderByDesc(Category::getCreateTime);
        List<Category> list= baseMapper.selectList(wrapper);
        return list;
    }

    @Override
    public List<CategoryVO> getCategoryList() {
        List<CategoryVO> list = new ArrayList<>();

        LambdaQueryWrapper<Category> wrapper =new LambdaQueryWrapper<>();
        wrapper.eq(Category::getIsRecommend,CategoryRecommendEnum.ALL_RECOMMEND.getValue()).or();
        List<Category> categories = baseMapper.selectList(wrapper);
        LambdaQueryWrapper<Goods> queryWrapper= new LambdaQueryWrapper<>();
        for (Category category : categories){
            CategoryVO categoryVO = new CategoryVO();
            categoryVO.setId(category.getId());
            categoryVO.setName(category.getName());
            categoryVO.setIcon(category.getIcon());
            wrapper.clear();
            wrapper.eq(Category::getParentId,category.getId());
            List<Category> childCategories = baseMapper.selectList(wrapper);
            List<CategoryChildrenGoodsVO> categoryChildrenGoodsList=new ArrayList<>();
            for (Category item:childCategories){
                CategoryChildrenGoodsVO childrenGoodsVO = new CategoryChildrenGoodsVO();
                childrenGoodsVO.setId(item.getId());
                childrenGoodsVO.setName(item.getName());
                childrenGoodsVO.setIcon(item.getIcon());
                childrenGoodsVO.setParentId(category.getId());
                childrenGoodsVO.setParentName(category.getName());
                queryWrapper.clear();
                List<Goods> goodsList = goodsMapper.selectList(queryWrapper.eq(Goods::getCategoryId,item.getId()));
                List<RecommendGoodsVO> goodsVOList = GoodsConvert.INSTANCE.convertToRecommendGoodsVOList(goodsList);
                childrenGoodsVO.setGoods(goodsVOList);
                categoryChildrenGoodsList.add(childrenGoodsVO);
            }
               categoryVO.setChildren(categoryChildrenGoodsList);
            list.add(categoryVO);
        }
        return  list;
    }
}
