package com.yzw.shop_online.service;

import com.yzw.shop_online.entity.Category;
import com.yzw.shop_online.entity.IndexCarousel;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yzw
 * @since 2023-11-09
 */
public interface IndexCarouselService extends IService<IndexCarousel> {
   List<IndexCarousel> getList(Integer distributionSite);


}
