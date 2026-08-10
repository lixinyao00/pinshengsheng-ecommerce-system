package com.pinshengsheng.order.mapper;

import com.pinshengsheng.order.vo.OrderItemSource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

// 查询下单时需要保存的商品和 SKU 快照
@Mapper
public interface ProductSnapshotMapper {

    @Select("""
            SELECT p.id AS product_id,
                   p.name AS product_name,
                   p.main_image,
                   s.id AS sku_id,
                   s.sku_code,
                   s.name AS sku_name,
                   s.price,
                   st.available_stock
            FROM pss_sku s
            JOIN pss_product p ON p.id = s.product_id
            JOIN pss_sku_stock st ON st.sku_id = s.id
            WHERE s.id = #{skuId}
              AND s.status = 1
              AND p.status = 1
            """)
    OrderItemSource findAvailableSku(Long skuId);
}
