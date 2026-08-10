package com.pinshengsheng.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinshengsheng.order.dto.OrderCreateRequest;
import com.pinshengsheng.order.dto.OrderItemCreateRequest;
import com.pinshengsheng.order.client.StockClient;
import com.pinshengsheng.order.entity.Address;
import com.pinshengsheng.order.entity.Order;
import com.pinshengsheng.order.entity.OrderItem;
import com.pinshengsheng.order.mapper.AddressMapper;
import com.pinshengsheng.order.mapper.OrderItemMapper;
import com.pinshengsheng.order.mapper.OrderMapper;
import com.pinshengsheng.order.mapper.ProductSnapshotMapper;
import com.pinshengsheng.order.service.OrderService;
import com.pinshengsheng.order.vo.OrderDetailVO;
import com.pinshengsheng.order.vo.OrderItemSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

// 订单业务：校验下单信息、保存订单和订单快照
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final AddressMapper addressMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductSnapshotMapper productSnapshotMapper;
    private final StockClient stockClient;

    public OrderServiceImpl(
            AddressMapper addressMapper,
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            ProductSnapshotMapper productSnapshotMapper,
            StockClient stockClient) {
        this.addressMapper = addressMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productSnapshotMapper = productSnapshotMapper;
        this.stockClient = stockClient;
    }

    @Override
    @Transactional
    public OrderDetailVO create(Long userId, OrderCreateRequest request) {
        Address address = findUserAddress(userId, request.getAddressId());
        if (address == null) {
            return null;
        }

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<Long, Integer> quantities = new LinkedHashMap<>();

        // 同一个 SKU 重复提交时先合并数量，避免绕过库存校验
        for (OrderItemCreateRequest itemRequest : request.getItems()) {
            quantities.merge(itemRequest.getSkuId(), itemRequest.getQuantity(), Integer::sum);
        }

        // 先全部校验并组装快照，校验通过后再写入订单
        for (Map.Entry<Long, Integer> quantityEntry : quantities.entrySet()) {
            OrderItemSource source = productSnapshotMapper.findAvailableSku(quantityEntry.getKey());
            if (source == null || source.getAvailableStock() < quantityEntry.getValue()) {
                return null;
            }

            BigDecimal itemAmount = source.getPrice()
                    .multiply(BigDecimal.valueOf(quantityEntry.getValue()));
            totalAmount = totalAmount.add(itemAmount);

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(source.getProductId());
            orderItem.setSkuId(source.getSkuId());
            orderItem.setProductName(source.getProductName());
            orderItem.setSkuCode(source.getSkuCode());
            orderItem.setSkuName(source.getSkuName());
            orderItem.setMainImage(source.getMainImage());
            orderItem.setPrice(source.getPrice());
            orderItem.setQuantity(quantityEntry.getValue());
            orderItem.setTotalAmount(itemAmount);
            orderItems.add(orderItem);
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus(0);
        order.setStockLocked(0);
        copyAddressSnapshot(address, order);

        List<OrderItem> lockedItems = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            if (!stockClient.lockStock(orderItem.getSkuId(), orderItem.getQuantity())) {
                releaseLockedItems(lockedItems);
                return null;
            }
            lockedItems.add(orderItem);
        }

        try {
            order.setStockLocked(1);
            orderMapper.insert(order);

            for (OrderItem orderItem : orderItems) {
                orderItem.setOrderId(order.getId());
                orderItemMapper.insert(orderItem);
            }
        } catch (RuntimeException exception) {
            releaseLockedItems(lockedItems);
            throw exception;
        }

        return buildDetail(order, orderItems);
    }

    @Override
    public List<Order> listByUserId(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
                .orderByDesc(Order::getId);
        return orderMapper.selectList(wrapper);
    }

    @Override
    public OrderDetailVO getDetail(Long userId, Long orderId) {
        Order order = getByUserId(userId, orderId);
        if (order == null) {
            return null;
        }

        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId);
        return buildDetail(order, orderItemMapper.selectList(itemWrapper));
    }

    @Override
    public Order getByUserId(Long userId, Long orderId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getId, orderId)
                .eq(Order::getUserId, userId);
        return orderMapper.selectOne(wrapper);
    }

    @Override
    public Order getById(Long orderId) {
        return orderMapper.selectById(orderId);
    }

    @Override
    @Transactional
    public boolean pay(Long userId, Long orderId) {
        Order order = getByUserId(userId, orderId);
        if (order == null || !Integer.valueOf(0).equals(order.getStatus())) {
            return false;
        }

        List<OrderItem> items = findOrderItems(orderId);
        List<OrderItem> confirmedItems = new ArrayList<>();
        if (Integer.valueOf(1).equals(order.getStockLocked())) {
            for (OrderItem item : items) {
                if (!stockClient.confirmStock(item.getSkuId(), item.getQuantity())) {
                    relockItems(confirmedItems);
                    return false;
                }
                confirmedItems.add(item);
            }
        }

        try {
            order.setStatus(1);
            order.setStockLocked(0);
            order.setPayTime(LocalDateTime.now());
            return orderMapper.updateById(order) > 0;
        } catch (RuntimeException exception) {
            relockItems(confirmedItems);
            throw exception;
        }
    }

    @Override
    @Transactional
    public boolean cancel(Long userId, Long orderId) {
        Order order = getByUserId(userId, orderId);
        if (order == null || !Integer.valueOf(0).equals(order.getStatus())) {
            return false;
        }

        List<OrderItem> items = findOrderItems(orderId);
        List<OrderItem> releasedItems = new ArrayList<>();

        if (Integer.valueOf(1).equals(order.getStockLocked())) {
            for (OrderItem item : items) {
                if (!stockClient.releaseStock(item.getSkuId(), item.getQuantity())) {
                    relockItems(releasedItems);
                    return false;
                }
                releasedItems.add(item);
            }
        }

        order.setStatus(4);
        order.setStockLocked(0);
        order.setCancelTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional
    public boolean ship(Long orderId) {
        Order order = getById(orderId);
        if (order == null || !Integer.valueOf(1).equals(order.getStatus())) {
            return false;
        }

        order.setStatus(2);
        order.setDeliveryTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    @Transactional
    public boolean complete(Long userId, Long orderId) {
        Order order = getByUserId(userId, orderId);
        if (order == null || !Integer.valueOf(2).equals(order.getStatus())) {
            return false;
        }

        order.setStatus(3);
        order.setFinishTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public List<Long> listExpiredOrderIds(LocalDateTime cutoff) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Order::getId)
                .eq(Order::getStatus, 0)
                .le(Order::getCreateTime, cutoff)
                .orderByAsc(Order::getId);
        return orderMapper.selectList(wrapper).stream()
                .map(Order::getId)
                .toList();
    }

    @Override
    @Transactional
    public boolean cancelExpiredOrder(Long orderId, LocalDateTime cutoff) {
        Order order = orderMapper.selectByIdForUpdate(orderId);
        if (order == null
                || !Integer.valueOf(0).equals(order.getStatus())
                || order.getCreateTime() == null
                || order.getCreateTime().isAfter(cutoff)) {
            return false;
        }

        List<OrderItem> items = findOrderItems(orderId);
        List<OrderItem> releasedItems = new ArrayList<>();

        if (Integer.valueOf(1).equals(order.getStockLocked())) {
            for (OrderItem item : items) {
                if (!stockClient.releaseStock(item.getSkuId(), item.getQuantity())) {
                    relockItems(releasedItems);
                    return false;
                }
                releasedItems.add(item);
            }
        }

        order.setStatus(4);
        order.setStockLocked(0);
        order.setCancelTime(LocalDateTime.now());
        return orderMapper.updateById(order) > 0;
    }

    @Override
    public List<Order> listForAdmin(Integer status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getId);
        return orderMapper.selectList(wrapper);
    }

    @Override
    public OrderDetailVO getDetailForAdmin(Long orderId) {
        Order order = getById(orderId);
        if (order == null) {
            return null;
        }
        return buildDetail(order, findOrderItems(orderId));
    }

    private Address findUserAddress(Long userId, Long addressId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getId, addressId)
                .eq(Address::getUserId, userId);
        return addressMapper.selectOne(wrapper);
    }

    private void copyAddressSnapshot(Address address, Order order) {
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setProvince(address.getProvince());
        order.setCity(address.getCity());
        order.setDistrict(address.getDistrict());
        order.setDetailAddress(address.getDetailAddress());
    }

    private OrderDetailVO buildDetail(Order order, List<OrderItem> items) {
        OrderDetailVO detail = new OrderDetailVO();
        detail.setOrder(order);
        detail.setItems(items);
        return detail;
    }

    private void releaseLockedItems(List<OrderItem> items) {
        for (OrderItem item : items) {
            stockClient.releaseStock(item.getSkuId(), item.getQuantity());
        }
    }

    private void relockItems(List<OrderItem> items) {
        for (OrderItem item : items) {
            stockClient.lockStock(item.getSkuId(), item.getQuantity());
        }
    }

    private List<OrderItem> findOrderItems(Long orderId) {
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orderId)
                .orderByAsc(OrderItem::getId);
        return orderItemMapper.selectList(itemWrapper);
    }

    private String generateOrderNo() {
        int randomNumber = ThreadLocalRandom.current().nextInt(100, 1000);
        return "PSS" + LocalDateTime.now().format(ORDER_NO_TIME_FORMAT) + randomNumber;
    }
}
