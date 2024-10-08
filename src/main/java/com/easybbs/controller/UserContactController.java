package com.easybbs.controller;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.dto.UserContactSearchResultDto;
import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.enums.ResponseCodeEnum;
import com.easybbs.entity.enums.UserContactStatusEnum;
import com.easybbs.entity.enums.UserContactTypeEnum;
import com.easybbs.entity.po.UserContact;
import com.easybbs.entity.po.UserContactApply;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.query.UserContactApplyQuery;
import com.easybbs.entity.query.UserContactQuery;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.entity.vo.UserInfoVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.UserContactApplyService;
import com.easybbs.service.UserContactService;
import com.easybbs.service.UserInfoService;
import com.easybbs.utils.CopyTools;
import jodd.util.ArraysUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

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

    //获取好友申请列表
    @RequestMapping("/loadApply")
    @GlobalInterceptor
    public ResponseVO loadApply(HttpServletRequest request,Integer pageNo) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContactApplyQuery applyQuery = new UserContactApplyQuery();
        applyQuery.setOrderBy("last_apply_time desc");
        applyQuery.setReceiveUserId(tokenUserInfoDto.getUserId());
        applyQuery.setPageNo(pageNo);
        applyQuery.setPageSize(PageSize.SIZE15.getSize());
        applyQuery.setQueryContactInfo(true);
        PaginationResultVO resultVO = userContactApplyService.findListByPage(applyQuery);
        return getSuccessResponseVO(resultVO);
    }
    //获取好友申请列表
    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public ResponseVO dealWithApply(HttpServletRequest request,@NotEmpty Integer applyId,@NotEmpty Integer status) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        this.userContactApplyService.dealWithApply(tokenUserInfoDto.getUserId(),applyId,status);
        return getSuccessResponseVO(null);
    }

    //获取联系人
    @RequestMapping("/loadContact")
    @GlobalInterceptor
    public ResponseVO loadContact(HttpServletRequest request, @NotNull String contactType) {
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByName(contactType);
        if(null == contactTypeEnum) {
            throw  new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContactQuery contactQuery = new UserContactQuery();
        contactQuery.setUserId(tokenUserInfoDto.getUserId());
        contactQuery.setContactType(contactTypeEnum.getType());
        if(contactTypeEnum == UserContactTypeEnum.USER){
            //查用户信息
            contactQuery.setQueryContactUserInfo(true);
        }else if(UserContactTypeEnum.GROUP == contactTypeEnum){
            contactQuery.setQueryGroupInfo(true);
            contactQuery.setExcludeBygroup(true);
        }
        contactQuery.setOrderBy("last_update_time desc");
        contactQuery.setStatusArray(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus(), //被拉黑也可看见 只是不能发消息
        });

        List<UserContact> contactList=userContactService.findListByParam(contactQuery);
        return getSuccessResponseVO(contactList);
    }

    //点头像获取联系人详情 不一定是好友
    @RequestMapping("/getContactInfo")
    @GlobalInterceptor
    public ResponseVO getContactInfo(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        //判断是不是联系人
        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
        if(userContact!=null){
            userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
        }

        return getSuccessResponseVO(userInfoVO);
    }


    //查联系人详情 必须是好友
    @RequestMapping("/getContactUserInfo")
    @GlobalInterceptor
    public ResponseVO getContactUserInfo(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        UserContact userContact = userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),contactId);
        if(userContact==null || !ArraysUtil.contains(new Integer[]{
                UserContactStatusEnum.FRIEND.getStatus(),
                UserContactStatusEnum.DEL_BE.getStatus(),
                UserContactStatusEnum.BLACKLIST_BE.getStatus(), //注意没有首次被拉黑 没有这些情况就是有问腿
        },userContact.getStatus())){
          throw  new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = userInfoService.getUserInfoByUserId(contactId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);

        return getSuccessResponseVO(userInfoVO);
    }


    //删除联系人(先获取 才能删除拉黑)
    @RequestMapping("/delContact")
    @GlobalInterceptor
    public ResponseVO delContact(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.DEL);
        return getSuccessResponseVO(null);
    }

    //拉黑联系人
    @RequestMapping("/delContact2BlackList")
    @GlobalInterceptor
    public ResponseVO delContact2BlackList(HttpServletRequest request, @NotNull String contactId) {
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
        userContactService.removeUserContact(tokenUserInfoDto.getUserId(),contactId,UserContactStatusEnum.BLACKLIST);
        return getSuccessResponseVO(null);
    }
}

