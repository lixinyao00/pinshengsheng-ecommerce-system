package com.pinshengsheng.auth.service.impl;

import com.pinshengsheng.auth.dto.RegisterRequest;
import com.pinshengsheng.auth.model.UserAccount;
import com.pinshengsheng.auth.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class UserServiceImpl implements UserService {

    private final JdbcTemplate jdbcTemplate;

    public UserServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public UserAccount findByUsername(String username) {
        return queryOne("SELECT id, username, password_hash, nickname, role, status "
                + "FROM pss_user WHERE username = ? LIMIT 1", username);
    }

    @Override
    public UserAccount findById(Long id) {
        return queryOne("SELECT id, username, password_hash, nickname, role, status "
                + "FROM pss_user WHERE id = ? LIMIT 1", id);
    }

    @Override
    public UserAccount register(RegisterRequest request) {
        String passwordHash = hashPassword(request.getPassword());

        try {
            jdbcTemplate.update(
                    "INSERT INTO pss_user(username, password_hash, nickname, role, status) VALUES (?, ?, ?, 'USER', 1)",
                    request.getUsername(),
                    passwordHash,
                    request.getNickname()
            );
        } catch (DuplicateKeyException exception) {
            return null;
        }

        return findByUsername(request.getUsername());
    }

    @Override
    public boolean matchesPassword(String rawPassword, String passwordHash) {
        return hashPassword(rawPassword).equals(passwordHash);
    }

    private UserAccount queryOne(String sql, Object parameter) {
        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            UserAccount user = new UserAccount();
            user.setId(resultSet.getLong("id"));
            user.setUsername(resultSet.getString("username"));
            user.setPasswordHash(resultSet.getString("password_hash"));
            user.setNickname(resultSet.getString("nickname"));
            user.setRole(resultSet.getString("role"));
            user.setStatus(resultSet.getInt("status"));
            return user;
        }, parameter).stream().findFirst().orElse(null);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte value : bytes) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("密码加密失败", exception);
        }
    }
}
