package com.subaiqiao.yupicturebackend.manager.auth;

import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

@Component
public class StpKit {
    public static final String SPACE_TYPE = "space";
    public static final StpLogic DEFAULT = StpUtil.stpLogic;
    public static final StpLogic SPACE = new StpLogic(SPACE_TYPE);
}
