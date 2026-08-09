package com.pinshengsheng.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pinshengsheng.order.entity.Address;
import org.apache.ibatis.annotations.Mapper;

// 提供收货地址的基础增删改查能力
@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}
