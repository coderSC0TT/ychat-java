package com.easybbs.controller;

import java.io.IOException;
import java.util.List;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.enums.GroupStatusEnum;
import com.easybbs.entity.enums.UserContactStatusEnum;
import com.easybbs.entity.po.UserContact;
import com.easybbs.entity.query.GroupInfoQuery;
import com.easybbs.entity.po.GroupInfo;
import com.easybbs.entity.query.UserContactQuery;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.service.GroupInfoService;
import com.easybbs.service.UserContactService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 *  Controller
 */
@RestController("groupInfoController")
@RequestMapping("/group")
public class GroupInfoController extends ABaseController{

	@Resource
	private UserContactService userContactService;

	@Resource
	private GroupInfoService groupInfoService;

	@RequestMapping("/saveGroup")
	@GlobalInterceptor //校验登录
	public  ResponseVO saveGroup(HttpServletRequest request,
								 String groupId,
								 @NotEmpty String groupName,
								 String groupNotice,
								 @NotNull Integer joinType,
								 MultipartFile avatarFile,
								 MultipartFile avatarCover) throws IOException {
		TokenUserInfoDto tokenUserInfoDto =  getTokenUserInfo(request);
		//组装
		GroupInfo groupInfo = new GroupInfo();
		groupInfo.setGroupId(groupId);
		groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfo.setGroupName(groupName);
		groupInfo.setGroupNotice(groupNotice);
		groupInfo.setJoinType(joinType);
		this.groupInfoService.saveGroup(groupInfo,avatarFile,avatarCover);
		return getSuccessResponseVO(null);
	}

	@RequestMapping("/loadMygroup")
	//@GlobalInterceptor //校验登录
	public  ResponseVO loadMygroup(HttpServletRequest request) throws IOException {
		TokenUserInfoDto tokenUserInfoDto =  getTokenUserInfo(request);
		GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
		groupInfoQuery.setGroupOwnerId(tokenUserInfoDto.getUserId());
		groupInfoQuery.setOrderBy("create_time desc");
		List<GroupInfo> groupInfoList = this.groupInfoService.findListByParam(groupInfoQuery);
		return getSuccessResponseVO(groupInfoList);
	}
	@RequestMapping("/getGroupInfo")
	//@GlobalInterceptor //校验登录
	public  ResponseVO getGroupInfo(HttpServletRequest request,
									@NotEmpty String groupId) throws IOException {
		TokenUserInfoDto tokenUserInfoDto =  getTokenUserInfo(request);
		//判断是否在群中
		UserContact userContact = this.userContactService.getUserContactByUserIdAndContactId(tokenUserInfoDto.getUserId(),groupId);
		if(null == userContact || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
			throw  new BusinessException("你不在群聊或者群聊不存在或已经解散");
		}
		GroupInfo groupInfo = this.groupInfoService.getGroupInfoByGroupId(groupId);
		if(null == groupInfo || !GroupStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())){
			throw  new BusinessException("群聊不存在或者已解散");
		}
		UserContactQuery userContactQuery = new UserContactQuery();
		userContactQuery.setContactId(groupId);
		//查找成员人数
		Integer memberCount = this.userContactService.findCountByParam(userContactQuery);
		groupInfo.setMemberCount(memberCount);
		return getSuccessResponseVO(groupInfo);
	}


}