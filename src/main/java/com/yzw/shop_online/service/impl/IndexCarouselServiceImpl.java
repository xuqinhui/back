package com.yzw.shop_online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yzw.shop_online.entity.IndexCarousel;
import com.yzw.shop_online.mapper.IndexCarouselMapper;
import com.yzw.shop_online.service.IndexCarouselService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class IndexCarouselServiceImpl extends ServiceImpl<IndexCarouselMapper, IndexCarousel> implements IndexCarouselService {

    @Override
    public List<IndexCarousel> getList(Integer distributionSite) {
        LambdaQueryWrapper<IndexCarousel> wrapper = new LambdaQueryWrapper<>();
       if (distributionSite !=null){
           wrapper.eq(IndexCarousel::getType,distributionSite);

       }
       wrapper.orderByDesc(IndexCarousel::getCreateTime);

       List<IndexCarousel> list = baseMapper.selectList(wrapper);

       return list;


    }
}
