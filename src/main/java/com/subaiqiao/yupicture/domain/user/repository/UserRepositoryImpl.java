package com.subaiqiao.yupicture.domain.user.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.infrastructure.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * @author Caozhaoyu
 * @date 2025年04月02日 19:37
 */
@Service
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {
}
