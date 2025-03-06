package com.subaiqiao.yupicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceAddRequest;
import com.subaiqiao.yupicturebackend.model.dto.space.SpaceQueryRequest;
import com.subaiqiao.yupicturebackend.model.dto.space.analyze.*;
import com.subaiqiao.yupicturebackend.model.entity.Space;
import com.subaiqiao.yupicturebackend.model.entity.User;
import com.subaiqiao.yupicturebackend.model.vo.SpaceVO;
import com.subaiqiao.yupicturebackend.model.vo.space.analyze.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 12947
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-16 17:38:31
*/
public interface SpaceAnalyzeService extends IService<Space> {
    /**
     * 统计空间的使用率
     * @param spaceUsageAnalyzeRequest 请求参数
     * @param loginUser 登录用户
     * @return 响应结果
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 统计空间的分类使用率
     * @param spaceCategoryAnalyzeRequest 查询条件
     * @param loginUser 登录用户
     * @return 响应结果
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片标签分析
     * @param spaceTagAnalyzeRequest 查询条件
     * @param loginUser 登录用户
     * @return 响应结果
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 获取空间图片大小分析
     * @param spaceSizeAnalyzeRequest 查询条件
     * @param loginUser 登录用户
     * @return 响应结果
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 获取空间用户上传行为分析
     * @param spaceUserAnalyzeRequest 查询条件
     * @param loginUser 登录用户
     * @return 响应结果
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    /**
     * 获取空间排行分析
     * @param spaceRankAnalyzeRequest 查询条件
     * @param loginUser 登录用户
     * @return 响应结果
     */
    List<SpaceVO> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);
}
