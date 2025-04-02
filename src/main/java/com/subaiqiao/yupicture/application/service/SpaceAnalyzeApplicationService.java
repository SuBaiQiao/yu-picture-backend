package com.subaiqiao.yupicture.application.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.subaiqiao.yupicture.interfaces.dto.space.space.analyze.*;
import com.subaiqiao.yupicture.domain.space.entity.Space;
import com.subaiqiao.yupicture.domain.user.entity.User;
import com.subaiqiao.yupicture.interfaces.vo.space.SpaceVO;
import com.subaiqiao.yupicture.interfaces.vo.space.space.analyze.*;

import java.util.List;

/**
* @author 12947
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2025-02-16 17:38:31
*/
public interface SpaceAnalyzeApplicationService extends IService<Space> {
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
