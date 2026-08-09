package com.pinshengsheng.order.service;

import com.pinshengsheng.order.dto.AddressSaveRequest;
import com.pinshengsheng.order.entity.Address;

import java.util.List;

public interface AddressService {

    List<Address> listByUserId(Long userId);

    Address create(Long userId, AddressSaveRequest request);

    Address update(Long userId, Long addressId, AddressSaveRequest request);

    boolean delete(Long userId, Long addressId);

    boolean setDefault(Long userId, Long addressId);
}
