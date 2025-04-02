package com.subaiqiao.yupicture.interfaces.vo.space.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 空间用户上传行为分析响应
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceUserAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = -6295939159187110988L;
    /**
     * 时间区间
     */
    private String period;
    /**
     * 上传数量
     */
    private Long count;
}
