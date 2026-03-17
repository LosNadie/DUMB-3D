package com.dumb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.enums.UserRoleEnum;
import com.dumb.common.enums.UserStatusEnum;
import com.dumb.common.exception.BizException;
import com.dumb.dto.request.LoginRequest;
import com.dumb.dto.request.RegisterRequest;
import com.dumb.dto.response.AuthResponse;
import com.dumb.entity.User;
import com.dumb.mapper.UserMapper;
import com.dumb.security.JwtTokenUtil;
import com.dumb.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(AuthenticationManager authenticationManager,
                           JwtTokenUtil jwtTokenUtil,
                           UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userMapper.selectByUsername(userDetails.getUsername());
        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        User existing = userMapper.selectByUsername(request.getUsername());
        if (existing != null) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR, "用户名已存在");
        }
        Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, request.getEmail()));
        if (emailCount > 0) {
            throw new BizException(ResultCodeEnum.PARAM_ERROR, "邮箱已被注册");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole(UserRoleEnum.USER.getCode());
        user.setStatus(UserStatusEnum.ENABLED.getCode());
        userMapper.insert(user);
        String token = jwtTokenUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole());
    }
}
