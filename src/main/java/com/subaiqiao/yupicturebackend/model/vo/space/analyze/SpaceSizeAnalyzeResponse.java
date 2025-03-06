package com.subaiqiao.yupicturebackend.model.vo.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceSizeAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = -6295939159187110988L;
    private String sizeRange;
    private Long count;
}
