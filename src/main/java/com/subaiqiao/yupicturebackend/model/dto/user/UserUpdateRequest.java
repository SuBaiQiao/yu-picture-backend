package com.subaiqiao.yupicturebackend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建用户请求
 */
@Data
public class UserUpdateRequest implements Serializable {
    private static final long serialVersionUID = 1126980057962120103L;
    private Long id;
    private String userName;
    private String userAccount;
    private String userAvatar;
    private String userProfile;
    private String userRole;

}
