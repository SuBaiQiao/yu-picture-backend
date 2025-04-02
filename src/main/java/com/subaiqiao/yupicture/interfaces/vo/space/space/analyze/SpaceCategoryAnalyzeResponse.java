package com.subaiqiao.yupicture.interfaces.vo.space.space.analyze;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SpaceCategoryAnalyzeResponse implements Serializable {
    private static final long serialVersionUID = 6553590937692544370L;
    private String category;
    private Long count;
    private Long totalSize;

}
