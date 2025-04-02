package com.subaiqiao.yupicture.infrastructure.api.imagesearch;

import com.subaiqiao.yupicture.infrastructure.api.imagesearch.model.ImageSearchResult;
import com.subaiqiao.yupicture.infrastructure.api.imagesearch.sub.GetImageFirstUrlApi;
import com.subaiqiao.yupicture.infrastructure.api.imagesearch.sub.GetImageListApi;
import com.subaiqiao.yupicture.infrastructure.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        return GetImageListApi.getImageList(imageFirstUrl);
    }

}
