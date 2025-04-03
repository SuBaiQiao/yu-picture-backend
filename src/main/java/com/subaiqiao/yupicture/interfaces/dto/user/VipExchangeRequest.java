package com.subaiqiao.yupicture.interfaces.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Caozhaoyu
 * @date 2025年04月03日 12:43
 */
@Data
public class VipExchangeRequest implements Serializable {
    private static final long serialVersionUID = 8735650154179439661L;

    // 兑换码
    private String vipCode;
}
