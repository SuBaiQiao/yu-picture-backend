package com.subaiqiao.yupicture.interfaces.dto.space.space.analyze;

import lombok.Data;

import java.io.Serializable;

@Data
public class SpaceRankAnalyzeRequest implements Serializable {

    private static final long serialVersionUID = -3139999893815471441L;
    private Integer topN = 10;

}
