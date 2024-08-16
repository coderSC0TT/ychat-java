package com.easybbs.service.impl;

import java.io.File;
import java.util.*;

import javax.annotation.Resource;

import com.easybbs.entity.config.AppConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.UserInfoBeauty;
import com.easybbs.entity.vo.UserInfoVO;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.UserInfoBeautyMapper;
import com.easybbs.redis.RedisComponent;
import com.easybbs.utils.CopyTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;

import com.easybbs.entity.query.UserInfoQuery;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.mappers.UserInfoMapper;
import com.easybbs.service.UserInfoService;
import com.easybbs.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private RedisComponent redisComponent;

	@Resource
	private AppConfig appConfig;

	@Resource
	private UserInfoBeautyMapper userInfoBeautyMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserInfo> findListByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserInfoQuery param) {
		return this.userInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserInfo> findListByPage(UserInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserInfo> list = this.findListByParam(param);
		PaginationResultVO<UserInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserInfo bean) {
		return this.userInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserInfo bean, UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserInfoQuery param) {
		StringTools.checkParam(param);
		return this.userInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据UserId获取对象
	 */
	@Override
	public UserInfo getUserInfoByUserId(String userId) {
		return this.userInfoMapper.selectByUserId(userId);
	}

	/**
	 * 根据UserId修改
	 */
	@Override
	public Integer updateUserInfoByUserId(UserInfo bean, String userId) {
		return this.userInfoMapper.updateByUserId(bean, userId);
	}

	/**
	 * 根据UserId删除
	 */
	@Override
	public Integer deleteUserInfoByUserId(String userId) {
		return this.userInfoMapper.deleteByUserId(userId);
	}

	/**
	 * 根据Email获取对象
	 */
	@Override
	public UserInfo getUserInfoByEmail(String email) {
		return this.userInfoMapper.selectByEmail(email);
	}

	/**
	 * 根据Email修改
	 */
	@Override
	public Integer updateUserInfoByEmail(UserInfo bean, String email) {
		return this.userInfoMapper.updateByEmail(bean, email);
	}

	/**
	 * 根据Email删除
	 */
	@Override
	public Integer deleteUserInfoByEmail(String email) {
		return this.userInfoMapper.deleteByEmail(email);
	}

	/**
	 * 注册
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void register(String email, String nickName, String password) {
		Map<String, Object> result = new HashMap<String, Object>();
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if (null != userInfo) {
			throw new BusinessException("邮箱账号已经存在");
		}

		String userId = StringTools.getUserId();
		UserInfoBeauty beautyAccount = (UserInfoBeauty) this.userInfoBeautyMapper.selectByEmail(email);
		//靓号必须有且没有被使用
		Boolean useBeautyAccount = null != beautyAccount && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus());
		if (useBeautyAccount) {
			userId = UserContactTypeEnum.USER.getPrefix() + beautyAccount.getUserId();
		}
		Date curDate = new Date();
		userInfo = new UserInfo();
		userInfo.setUserId(userId);
		userInfo.setNickName(nickName);
		userInfo.setEmail(email);
		//TODO 加密方式注解优化
		userInfo.setPassword(StringTools.encodeByMD5(password));
		userInfo.setCreateTime(curDate);
		userInfo.setStatus(String.valueOf(UserStatusEnum.ENABLE.getStatus()));
		userInfo.setLastOffTime(curDate.getTime());
		this.userInfoMapper.insert(userInfo);

		if (useBeautyAccount) {
			//更新靓号使用状态
			UserInfoBeauty updateBeauty = new UserInfoBeauty();
			updateBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
			this.userInfoBeautyMapper.updateById(updateBeauty, beautyAccount.getId());
		} //TODO 创造机器人好友


	}

	@Override
	public UserInfoVO login(String email, String password) {
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if(null == userInfo || !userInfo.getPassword().equals(StringTools.encodeByMD5(password))) {
			throw new BusinessException("账号密码不存在");
		}
		if(UserStatusEnum.DISABLE.equals(userInfo.getStatus())) {
			throw new BusinessException("账号已禁用");
		}
		//TODO 查询联系人 查询我的群组
		TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);

		Long lastHeartBeat= redisComponent.getUserHeartBeat(userInfo.getUserId());
		if(null != lastHeartBeat) {
			throw new BusinessException("此账号已在别处登录 请退出后再登录");
		}
		//保存信息到redis中
		String token = StringTools.encodeByMD5(tokenUserInfoDto.getUserId()+StringTools.getRandomString(Constants.LENGTH_20));
		tokenUserInfoDto.setToken(token);
		redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

		//登录时返回全部信息 缓存到客户端 省去再查的过程
		UserInfoVO userInfoVO= CopyTools.copy(userInfo,UserInfoVO.class);
		userInfoVO.setToken(tokenUserInfoDto.getToken());
		userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
		userInfoVO.setJoinType(JoinTypeEnum.APPLY.getType());
		return userInfoVO;
	}



	//判断是否为管理员
	private TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo) {
		TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
		tokenUserInfoDto.setUserId(userInfo.getUserId());
		tokenUserInfoDto.setNickName(userInfo.getNickName());

		String adminEmails = appConfig.getAdminEmails();
		if (!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminEmails.split(","), userInfo.getEmail())) {
			tokenUserInfoDto.setAdmin(true);
		} else {
			tokenUserInfoDto.setAdmin(false);
		}
		return tokenUserInfoDto;
	}

	@Override
	public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) {
		if(avatarFile!=null){
			String baseFoder = appConfig.getProjectFolder()+Constants.FILE_FOLDER_FILE;
			File targetFileFolder = new File(baseFoder+Constants.FILE_FOLDER_AVATAR_NAME);
			if(!targetFileFolder.exists()){ //如果目录不存在 手动创建
				targetFileFolder.mkdirs();
			}
		}
	}


}