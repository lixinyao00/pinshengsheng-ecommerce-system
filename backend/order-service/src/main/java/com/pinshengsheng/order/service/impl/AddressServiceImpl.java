package com.pinshengsheng.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.pinshengsheng.order.dto.AddressSaveRequest;
import com.pinshengsheng.order.entity.Address;
import com.pinshengsheng.order.mapper.AddressMapper;
import com.pinshengsheng.order.service.AddressService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 收货地址业务，包括默认地址的切换规则
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    @Override
    public List<Address> listByUserId(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getId);
        return addressMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public Address create(Long userId, AddressSaveRequest request) {
        Address address = new Address();
        address.setUserId(userId);
        copyRequest(request, address);

        boolean firstAddress = addressMapper.selectCount(
                new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId)
        ) == 0;
        boolean shouldBeDefault = firstAddress || Integer.valueOf(1).equals(request.getIsDefault());

        if (shouldBeDefault) {
            clearDefaultAddress(userId);
        }
        address.setIsDefault(shouldBeDefault ? 1 : 0);
        addressMapper.insert(address);
        return address;
    }

    @Override
    @Transactional
    public Address update(Long userId, Long addressId, AddressSaveRequest request) {
        Address address = findUserAddress(userId, addressId);
        if (address == null) {
            return null;
        }

        copyRequest(request, address);
        if (Integer.valueOf(1).equals(request.getIsDefault())) {
            clearDefaultAddress(userId);
            address.setIsDefault(1);
        }
        addressMapper.updateById(address);
        return address;
    }

    @Override
    @Transactional
    public boolean delete(Long userId, Long addressId) {
        Address address = findUserAddress(userId, addressId);
        if (address == null) {
            return false;
        }

        addressMapper.deleteById(addressId);
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            Address nextDefault = listByUserId(userId).stream().findFirst().orElse(null);
            if (nextDefault != null) {
                nextDefault.setIsDefault(1);
                addressMapper.updateById(nextDefault);
            }
        }
        return true;
    }

    @Override
    @Transactional
    public boolean setDefault(Long userId, Long addressId) {
        Address address = findUserAddress(userId, addressId);
        if (address == null) {
            return false;
        }

        clearDefaultAddress(userId);
        address.setIsDefault(1);
        return addressMapper.updateById(address) > 0;
    }

    private Address findUserAddress(Long userId, Long addressId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getId, addressId)
                .eq(Address::getUserId, userId);
        return addressMapper.selectOne(wrapper);
    }

    private void clearDefaultAddress(Long userId) {
        LambdaUpdateWrapper<Address> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0);
        addressMapper.update(null, wrapper);
    }

    private void copyRequest(AddressSaveRequest request, Address address) {
        address.setReceiverName(request.getReceiverName());
        address.setReceiverPhone(request.getReceiverPhone());
        address.setProvince(request.getProvince());
        address.setCity(request.getCity());
        address.setDistrict(request.getDistrict());
        address.setDetailAddress(request.getDetailAddress());
    }
}
