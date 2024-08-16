package com.easybbs.controller;

import java.io.IOException;
import java.util.List;

import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.query.UserInfoQuery;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.vo.ResponseVO;
import com.easybbs.entity.vo.UserInfoVO;
import com.easybbs.service.UserInfoService;
import com.easybbs.utils.CopyTools;
import com.easybbs.utils.StringTools;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 用户信息 Controller
 */
@RestController("userInfoController")
@RequestMapping("/userInfo")
public class UserInfoController extends ABaseController{

	@Resource
	private UserInfoService userInfoService;

	@RequestMapping("/getUserInfo")
	public ResponseVO getUserInfo(HttpServletRequest request){
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserInfo userInfo = userInfoService.getUserInfoByUserId(tokenUserInfoDto.getUserId());
		UserInfoVO userInfoVO = CopyTools.copy(userInfo,UserInfoVO.class);
		userInfoVO.setAdmin(tokenUserInfoDto.getAdmin()); //适不适合返回password
		return  getSuccessResponseVO(userInfoVO);
	}

	@RequestMapping("/saveUserInfo")
	public ResponseVO saveUserInfo(HttpServletRequest request, UserInfo userInfo, MultipartFile avatarFile,
								   MultipartFile avatarCover) throws IOException {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		userInfo.setUserId(tokenUserInfoDto.getUserId());
		userInfo.setPassword(null);
		userInfo.setStatus(null);
		userInfo.setCreateTime(null);
		userInfo.setLastLoginTime(null);//不能让随便改
		this.userInfoService.updateUserInfo(userInfo, avatarFile, avatarCover);
		return  getUserInfo(request);
	}


	@RequestMapping("/updatePassword")
	public ResponseVO updatePassword(HttpServletRequest request, @NotNull String password) throws IOException {
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfo(request);
		UserInfo userInfo = new UserInfo();
		userInfo.setPassword(StringTools.encodeByMD5(password));
		this.userInfoService.updateUserInfoByUserId(userInfo,tokenUserInfoDto.getUserId());
		//TODO 修改密码后强制退出
		return  getUserInfo(request);
	}


	/**
	 * 根据条件分页查询
	 */
	@RequestMapping("/loadDataList")
	public ResponseVO loadDataList(UserInfoQuery query){
		return getSuccessResponseVO(userInfoService.findListByPage(query));
	}

	/**
	 * 新增
	 */
	@RequestMapping("/add")
	public ResponseVO add(UserInfo bean) {
		userInfoService.add(bean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增
	 */
	@RequestMapping("/addBatch")
	public ResponseVO addBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 批量新增/修改
	 */
	@RequestMapping("/addOrUpdateBatch")
	public ResponseVO addOrUpdateBatch(@RequestBody List<UserInfo> listBean) {
		userInfoService.addBatch(listBean);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId查询对象
	 */
	@RequestMapping("/getUserInfoByUserId")
	public ResponseVO getUserInfoByUserId(String userId) {
		return getSuccessResponseVO(userInfoService.getUserInfoByUserId(userId));
	}

	/**
	 * 根据UserId修改对象
	 */
	@RequestMapping("/updateUserInfoByUserId")
	public ResponseVO updateUserInfoByUserId(UserInfo bean,String userId) {
		userInfoService.updateUserInfoByUserId(bean,userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据UserId删除
	 */
	@RequestMapping("/deleteUserInfoByUserId")
	public ResponseVO deleteUserInfoByUserId(String userId) {
		userInfoService.deleteUserInfoByUserId(userId);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email查询对象
	 */
	@RequestMapping("/getUserInfoByEmail")
	public ResponseVO getUserInfoByEmail(String email) {
		return getSuccessResponseVO(userInfoService.getUserInfoByEmail(email));
	}

	/**
	 * 根据Email修改对象
	 */
	@RequestMapping("/updateUserInfoByEmail")
	public ResponseVO updateUserInfoByEmail(UserInfo bean,String email) {
		userInfoService.updateUserInfoByEmail(bean,email);
		return getSuccessResponseVO(null);
	}

	/**
	 * 根据Email删除
	 */
	@RequestMapping("/deleteUserInfoByEmail")
	public ResponseVO deleteUserInfoByEmail(String email) {
		userInfoService.deleteUserInfoByEmail(email);
		return getSuccessResponseVO(null);
	}
}