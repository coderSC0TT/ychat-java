package com.easybbs.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.easybbs.entity.enums.BeautyAccountStatusEnum;
import com.easybbs.entity.enums.UserContactTypeEnum;
import com.easybbs.entity.enums.UserStatusEnum;
import com.easybbs.entity.po.UserInfoBeauty;
import com.easybbs.mappers.UserInfoBeautyMapper;
import org.springframework.stereotype.Service;

import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.query.UserInfoQuery;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.mappers.UserInfoMapper;
import com.easybbs.service.UserInfoService;
import com.easybbs.utils.StringTools;


/**
 * 用户信息 业务接口实现
 */
@Service("userInfoService")
public class UserInfoServiceImpl implements UserInfoService {

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

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
	 *
	 * @return
	 */
	@Override
	public Map<String, Object> register(String email, String nickName, String password) {
		Map<String,Object> result = new HashMap<String,Object>();
		UserInfo userInfo = this.userInfoMapper.selectByEmail(email);
		if(null!=userInfo) {
			String userId = StringTools.getUserId();
			UserInfoBeauty beautyAccount= (UserInfoBeauty) this.userInfoBeautyMapper.selectByEmail(email);
			//靓号必须有且没有被使用
			Boolean useBeautyAccount = null !=beautyAccount && BeautyAccountStatusEnum.NO_USE.getStatus().equals(beautyAccount.getStatus()) ;
			if(useBeautyAccount) {
				userId= UserContactTypeEnum.USER.getPrefix()+ beautyAccount.getUserId();
			}
			Date curDate = new Date();
			userInfo =new UserInfo();
			userInfo.setUserId(userId);
			userInfo.setNickName(nickName);
			userInfo.setEmail(email);
			userInfo.setPassword(StringTools.encodeByMD5(password));
			userInfo.setCreateTime(curDate);
			userInfo.setStatus(String.valueOf(UserStatusEnum.ENABLE.getStatus()));
			userInfo.setLastOffTime(curDate.getTime());
			this.userInfoMapper.insert(userInfo);

			if(useBeautyAccount){
				//更新靓号使用状态
				UserInfoBeauty updateBeauty = new UserInfoBeauty();
				updateBeauty.setStatus(BeautyAccountStatusEnum.USEED.getStatus());
				this.userInfoBeautyMapper.updateById(updateBeauty,beautyAccount.getId());
			} //TODO 创造机器人好友
		}else {
			result.put("success",false);
			result.put("errorMsg","邮箱已存在");
			return result;
		}

		return result;
	}


}