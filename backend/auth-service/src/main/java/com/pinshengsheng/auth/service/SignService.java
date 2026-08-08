package com.pinshengsheng.auth.service;

public interface SignService {

    boolean sign(Long userId, Integer day);

    boolean signed(Long userId, Integer day);

    Long signCount(Long userId);
}
