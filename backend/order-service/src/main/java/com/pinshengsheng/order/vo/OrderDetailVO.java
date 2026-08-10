package com.pinshengsheng.order.vo;

import com.pinshengsheng.order.entity.Order;
import com.pinshengsheng.order.entity.OrderItem;

import java.util.List;

// 订单详情同时返回订单主信息和商品明细
public class OrderDetailVO {

    private Order order;
    private List<OrderItem> items;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
