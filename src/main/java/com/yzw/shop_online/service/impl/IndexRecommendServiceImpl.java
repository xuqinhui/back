package com.yzw.shop_online.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yzw.shop_online.common.convert.IndexRecommendConvert;
import com.yzw.shop_online.entity.IndexRecommend;
import com.yzw.shop_online.mapper.IndexRecommendMapper;
import com.yzw.shop_online.service.IndexRecommendService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yzw.shop_online.vo.IndexRecommendVO;
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
public class IndexRecommendServiceImpl extends ServiceImpl<IndexRecommendMapper, IndexRecommend> implements IndexRecommendService {

    @Override
    public List<IndexRecommendVO> getList() {
        LambdaQueryWrapper<IndexRecommend> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IndexRecommend::getCreateTime);
        List<IndexRecommend> list = baseMapper.selectList(wrapper);
        List<IndexRecommendVO> results = IndexRecommendConvert.INSTANCE.convertToUserVoList(list);
        return results;
    }
}
