package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.dto.UserContactSearchResultDto;
import com.easybbs.entity.po.UserContactApply;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.service.UserContactApplyService;
import com.easybbs.service.UserContactService;
import com.easybbs.service.UserInfoService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

/**
 * @ClassName UserContactController
 * @Description 添加描述
 * @Author Suguo
 * @LastChangeDate 2024/7/11 下午11:08
 */

@RestController
@RequestMapping("/contact")
public class UserContactController extends  ABaseController {
    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;

    @RequestMapping("/search")
    @GlobalInterceptor
    public ResponseVO search(HttpServletRequest request, @NotEmpty String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContactSearchResultDto userContactSearchResultDto = userContactService.searchContact(tokenUserInfoDto.getUserId(),contactId);
        return getSuccessResponseVO(userContactSearchResultDto);
    }

    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public ResponseVO applyAdd(HttpServletRequest request,
                               @NotEmpty String contactId,
                               @NotEmpty String applyType,
                               String applyInfo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        Integer joinType = userContactService.applyAdd(tokenUserInfoDto,contactId,applyInfo);
        return getSuccessResponseVO(joinType);
    }
}

