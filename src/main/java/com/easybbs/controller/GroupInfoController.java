package com.easybbs.controller;

import java.util.List;

import com.easybbs.annotation.GlobalInterceptor;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.query.GroupInfoQuery;
import com.easybbs.entity.po.GroupInfo;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.service.GroupInfoService;
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
	private GroupInfoService groupInfoService;

	@RequestMapping("/saveGroup")
	@GlobalInterceptor //校验登录
	public  ResponseVO saveGroup(HttpServletRequest request,
								 String groupId,
								 @NotEmpty String groupName,
								 String groupNotice,
								 @NotNull Integer joinType,
								 MultipartFile avatarFile,
								 MultipartFile avatarCover){
		TokenUserInfoDto tokenUserInfoDto =  getTokenUserInfo(request);
		return getSuccessResponseVO(null);
	}
}