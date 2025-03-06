package com.subaiqiao.yupicturebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 空间用户上传行为分析请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpaceUserAnalyzeRequest extends SpaceAnalyzeRequest {
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 时间维度：day / week / month
     */
    private String timeDimension;
}
