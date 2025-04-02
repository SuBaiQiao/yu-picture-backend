package com.subaiqiao.yupicture.interfaces.dto.user;

import com.subaiqiao.yupicture.infrastructure.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 创建用户请求
 */
@EqualsAndHashCode(callSuper = true) // 解决@Data的警告
@Data
public class UserQueryRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = 1126980057962120103L;
    private Long id;
    private String userName;
    private String userAccount;
    private String userAvatar;
    private String userProfile;
    private String userRole;

}
