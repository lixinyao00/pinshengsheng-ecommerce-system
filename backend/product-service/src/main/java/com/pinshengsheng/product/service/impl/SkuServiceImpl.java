package com.pinshengsheng.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pinshengsheng.product.dto.SkuSaveRequest;
import com.pinshengsheng.product.entity.Product;
import com.pinshengsheng.product.entity.Sku;
import com.pinshengsheng.product.entity.SkuStock;
import com.pinshengsheng.product.mapper.ProductMapper;
import com.pinshengsheng.product.mapper.SkuMapper;
import com.pinshengsheng.product.mapper.SkuStockMapper;
import com.pinshengsheng.product.service.SkuService;
import com.pinshengsheng.product.vo.SkuStockVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// SKU 是商品的可售规格，库存单独放在 pss_sku_stock 表中维护
@Service
public class SkuServiceImpl implements SkuService {

    private final ProductMapper productMapper;
    private final SkuMapper skuMapper;
    private final SkuStockMapper skuStockMapper;

    public SkuServiceImpl(
            ProductMapper productMapper,
            SkuMapper skuMapper,
            SkuStockMapper skuStockMapper) {
        this.productMapper = productMapper;
        this.skuMapper = skuMapper;
        this.skuStockMapper = skuStockMapper;
    }

    @Override
    public List<SkuStockVO> getSkuList(Long productId) {
        // 一个商品可以有多个 SKU，例如同款耳机的黑色和白色版本
        LambdaQueryWrapper<Sku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sku::getProductId, productId).orderByAsc(Sku::getId);

        List<SkuStockVO> result = new ArrayList<>();
        for (Sku sku : skuMapper.selectList(queryWrapper)) {
            result.add(buildSkuStockVO(sku));
        }
        return result;
    }

    // 新增 SKU 和库存必须在同一个事务中完成
    @Override
    @Transactional
    public SkuStockVO createSku(SkuSaveRequest request) {
        Product product = productMapper.selectById(request.getProductId());
        // SKU 编码在系统内唯一，重复时不能继续写入
        if (product == null || skuCodeExists(request.getSkuCode(), null)) {
            return null;
        }

        Sku sku = new Sku();
        copyRequest(request, sku);
        sku.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        skuMapper.insert(sku);

        // SKU 新建成功后，立刻创建对应库存记录
        SkuStock stock = new SkuStock();
        stock.setSkuId(sku.getId());
        stock.setAvailableStock(request.getAvailableStock() == null
                ? 0 : request.getAvailableStock());
        stock.setLockedStock(0);
        skuStockMapper.insert(stock);

        return buildSkuStockVO(sku);
    }

    @Override
    @Transactional
    public SkuStockVO updateSku(Long id, SkuSaveRequest request) {
        Sku sku = skuMapper.selectById(id);
        Product product = productMapper.selectById(request.getProductId());
        if (sku == null || product == null || skuCodeExists(request.getSkuCode(), id)) {
            return null;
        }

        copyRequest(request, sku);
        if (request.getStatus() != null) {
            sku.setStatus(request.getStatus());
        }
        skuMapper.updateById(sku);

        // 编辑 SKU 时可以顺带修改库存，但库存更新仍走独立方法
        if (request.getAvailableStock() != null) {
            updateAvailableStock(id, request.getAvailableStock());
        }
        return buildSkuStockVO(sku);
    }

    @Override
    @Transactional
    public boolean updateSkuStatus(Long id, Integer status) {
        Sku sku = skuMapper.selectById(id);
        if (sku == null) {
            return false;
        }
        sku.setStatus(status);
        return skuMapper.updateById(sku) > 0;
    }

    @Override
    @Transactional
    public boolean updateAvailableStock(Long skuId, Integer availableStock) {
        Sku sku = skuMapper.selectById(skuId);
        if (sku == null || availableStock == null || availableStock < 0) {
            return false;
        }

        SkuStock stock = getStock(skuId);
        // 兼容早期 SKU 没有库存记录的情况，首次修改时补建库存
        if (stock == null) {
            stock = new SkuStock();
            stock.setSkuId(skuId);
            stock.setAvailableStock(availableStock);
            stock.setLockedStock(0);
            return skuStockMapper.insert(stock) > 0;
        }

        stock.setAvailableStock(availableStock);
        return skuStockMapper.updateById(stock) > 0;
    }

    private boolean skuCodeExists(String skuCode, Long excludeId) {
        LambdaQueryWrapper<Sku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Sku::getSkuCode, skuCode);
        // 编辑时排除自己，避免原编码被误判为重复
        if (excludeId != null) {
            queryWrapper.ne(Sku::getId, excludeId);
        }
        return skuMapper.selectCount(queryWrapper) > 0;
    }

    private void copyRequest(SkuSaveRequest request, Sku sku) {
        sku.setProductId(request.getProductId());
        sku.setSkuCode(request.getSkuCode());
        sku.setName(request.getSkuName());
        sku.setAttributesJson(request.getAttributesJson());
        sku.setPrice(request.getPrice());
    }

    private SkuStock getStock(Long skuId) {
        LambdaQueryWrapper<SkuStock> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuStock::getSkuId, skuId);
        return skuStockMapper.selectOne(queryWrapper);
    }

    private SkuStockVO buildSkuStockVO(Sku sku) {
        // 管理端列表需要同时拿到规格信息和库存信息
        SkuStock stock = getStock(sku.getId());
        SkuStockVO vo = new SkuStockVO();
        vo.setSku(sku);
        vo.setAvailableStock(stock == null ? 0 : stock.getAvailableStock());
        vo.setLockedStock(stock == null ? 0 : stock.getLockedStock());
        return vo;
    }
}
