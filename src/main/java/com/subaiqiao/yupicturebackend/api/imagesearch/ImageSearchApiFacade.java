package com.subaiqiao.yupicturebackend.api.imagesearch;

import com.subaiqiao.yupicturebackend.api.imagesearch.model.ImageSearchResult;
import com.subaiqiao.yupicturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.subaiqiao.yupicturebackend.api.imagesearch.sub.GetImageListApi;
import com.subaiqiao.yupicturebackend.api.imagesearch.sub.GetImagePageUrlApi;
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
