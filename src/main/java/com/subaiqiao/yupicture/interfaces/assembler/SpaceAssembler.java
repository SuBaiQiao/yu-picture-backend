package com.subaiqiao.yupicture.interfaces.assembler;

import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.interfaces.dto.space.space.SpaceAddRequest;
import com.subaiqiao.yupicture.interfaces.dto.space.space.SpaceEditRequest;
import com.subaiqiao.yupicture.interfaces.dto.space.space.SpaceUpdateRequest;
import org.springframework.beans.BeanUtils;

public class SpaceAssembler {

    public static Space toSpaceEntity(SpaceAddRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceUpdateRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }

    public static Space toSpaceEntity(SpaceEditRequest request) {
        Space space = new Space();
        BeanUtils.copyProperties(request, space);
        return space;
    }
}
