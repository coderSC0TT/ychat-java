package com.easybbs.service.impl;

import java.util.List;

import javax.annotation.Resource;

import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.dto.UserContactSearchResultDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.GroupInfo;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.query.GroupInfoQuery;
import com.easybbs.entity.query.UserInfoQuery;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.GroupInfoMapper;
import com.easybbs.mappers.UserInfoMapper;
import com.easybbs.utils.CopyTools;
import org.springframework.stereotype.Service;

import com.easybbs.entity.query.UserContactQuery;
import com.easybbs.entity.po.UserContact;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.mappers.UserContactMapper;
import com.easybbs.service.UserContactService;
import com.easybbs.utils.StringTools;


/**
 * 联系人 业务接口实现
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

	@Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;
	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<UserContact> findListByParam(UserContactQuery param) {
		return this.userContactMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(UserContactQuery param) {
		return this.userContactMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<UserContact> findListByPage(UserContactQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<UserContact> list = this.findListByParam(param);
		PaginationResultVO<UserContact> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(UserContact bean) {
		return this.userContactMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<UserContact> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<UserContact> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.userContactMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(UserContact bean, UserContactQuery param) {
		StringTools.checkParam(param);
		return this.userContactMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(UserContactQuery param) {
		StringTools.checkParam(param);
		return this.userContactMapper.deleteByParam(param);
	}

	/**
	 * 根据UserIdAndContactId获取对象
	 */
	@Override
	public UserContact getUserContactByUserIdAndContactId(String userId, String contactId) {
		return this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId修改
	 */
	@Override
	public Integer updateUserContactByUserIdAndContactId(UserContact bean, String userId, String contactId) {
		return this.userContactMapper.updateByUserIdAndContactId(bean, userId, contactId);
	}

	/**
	 * 根据UserIdAndContactId删除
	 */
	@Override
	public Integer deleteUserContactByUserIdAndContactId(String userId, String contactId) {
		return this.userContactMapper.deleteByUserIdAndContactId(userId, contactId);
	}

	@Override
	public UserContactSearchResultDto searchContact(String userId, String contactId) {
		//找搜索的是好友还是群
		UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
		if(null == typeEnum) {
			return null;
		}
		UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
		switch (typeEnum){
			case USER:
				UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
				if(null == userInfo) {
					return null;
				}
				resultDto = CopyTools.copy(userInfo,UserContactSearchResultDto.class);
				break;
			case GROUP:
				GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
				if(null == groupInfo) {
					return null;
				}
				resultDto = CopyTools.copy(groupInfo,UserContactSearchResultDto.class);
				break;
		}
		resultDto.setContactType(typeEnum.toString());
		resultDto.setContactId(contactId);
		if(userId.equals(contactId)){
			resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			return resultDto;
		}
		//查询是否为好友
		UserContact userContact =this.userContactMapper.selectByUserIdAndContactId(userId, contactId);
		resultDto.setStatus(userContact==null?null:userContact.getStatus());
		return resultDto;
	}

	@Override
	public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
		UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
		if(null== contactTypeEnum) {
			throw  new BusinessException(ResponseCodeEnum.CODE_600);
		}
		//申请人
		String applyUserId = tokenUserInfoDto.getUserId();

		//默认申请信息
		applyInfo =StringTools.isEmpty(applyInfo)? String.format(Constants.APPLY_INFO_TEMPLATE ,tokenUserInfoDto.getNickName()):applyInfo;
		Long curTime = System.currentTimeMillis();
		String receiveUserId = contactId;
		Integer joinType =null;
		//查询对方是否是自己好友 如果已经被拉黑则不可添加
		UserContact userContact = userContactMapper.selectByUserIdAndContactId(applyUserId, contactId);
		if(null != userContact &&UserContactStatusEnum.BLACKLIST_BE.getStatus().equals(userContact.getStatus())) {
			throw new BusinessException("已经被对方拉黑 无法添加");

		}
		if(UserContactTypeEnum.GROUP ==contactTypeEnum){
			GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
			if(null == groupInfo || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
				throw  new BusinessException("群聊不存在或者已经解散");
			}
			receiveUserId =groupInfo.getGroupOwnerId();
			joinType =groupInfo.getJoinType();
		}else {
			UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
			if(null == userInfo) {
				throw  new BusinessException(ResponseCodeEnum.CODE_600);
			}
			joinType = userInfo.getJoinType();
		}
		return 1;
	}
}