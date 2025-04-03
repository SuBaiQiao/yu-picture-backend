package com.subaiqiao.yupicture.interfaces.dto.user;

import lombok.Data;

/**
 * @author Caozhaoyu
 * @date 2025年04月03日 12:43
 */
@Data
public class VipCode {
    // 兑换码
    private String code;

    // 是否已经使用
    private boolean hasUsed;
}
