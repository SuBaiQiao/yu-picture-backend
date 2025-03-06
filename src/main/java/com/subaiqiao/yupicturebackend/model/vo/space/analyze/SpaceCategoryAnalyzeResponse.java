package com.subaiqiao.yupicturebackend.model.vo.space.analyze;

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
