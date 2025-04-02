package com.subaiqiao.yupicture.domain.picture.repository;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.subaiqiao.yupicture.domain.picture.entity.Picture;
import com.subaiqiao.yupicture.infrastructure.mapper.PictureMapper;
import org.springframework.stereotype.Service;

@Service
public class PictureRepositoryImpl extends ServiceImpl<PictureMapper, Picture> implements PictureRepository {
}
