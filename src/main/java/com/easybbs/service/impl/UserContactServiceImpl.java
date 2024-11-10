package com.easybbs.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SysSettingDto;
import com.easybbs.entity.dto.TokenUserInfoDto;
import com.easybbs.entity.dto.UserContactSearchResultDto;
import com.easybbs.entity.enums.*;
import com.easybbs.entity.po.GroupInfo;
import com.easybbs.entity.po.UserContactApply;
import com.easybbs.entity.po.UserInfo;
import com.easybbs.entity.query.*;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.GroupInfoMapper;
import com.easybbs.mappers.UserContactApplyMapper;
import com.easybbs.mappers.UserInfoMapper;
import com.easybbs.redis.RedisComponent;
import com.easybbs.service.UserContactApplyService;
import com.easybbs.utils.CopyTools;
import jodd.util.ArraysUtil;
import org.springframework.stereotype.Service;

import com.easybbs.entity.po.UserContact;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.mappers.UserContactMapper;
import com.easybbs.service.UserContactService;
import com.easybbs.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;


/**
 * 联系人 业务接口实现
 */
@Service("userContactService")
public class UserContactServiceImpl implements UserContactService {

	@Resource
	private RedisComponent redisComponent;
	@Resource
	private UserContactApplyService userContactApplyService;

	@Resource
	private UserContactMapper<UserContact, UserContactQuery> userContactMapper;

	@Resource
	private UserInfoMapper<UserInfo, UserInfoQuery> userInfoMapper;

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

	@Resource
	private UserContactApplyMapper<UserContactApply, UserContactApplyQuery> userContactApplyMapper;
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
	@Transactional
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
		if(null != userContact &&
				ArraysUtil.contains(new Integer[] {
						UserContactStatusEnum.BLACKLIST_BE.getStatus(),
					    UserContactStatusEnum.BLACKLIST_BE_FIRST.getStatus()
		},userContact.getStatus())) {
			throw new BusinessException("已经被对方拉黑 无法添加");

		}
		if(UserContactTypeEnum.GROUP ==contactTypeEnum){
			GroupInfo groupInfo = groupInfoMapper.selectByGroupId(contactId);
			if(null == groupInfo || GroupStatusEnum.DISSOLUTION.getStatus().equals(groupInfo.getStatus())) {
				throw  new BusinessException("群聊不存在或者已经解散");
			}//
			receiveUserId =groupInfo.getGroupOwnerId();
			joinType =groupInfo.getJoinType();
		}else {
			UserInfo userInfo = userInfoMapper.selectByUserId(contactId);
			if(null == userInfo) {
				throw  new BusinessException(ResponseCodeEnum.CODE_600);
			}
			joinType = userInfo.getJoinType();
		}
		//直接加入不用记录申请
		if(JoinTypeEnum.JOIN.getType().equals(joinType)){
			//TODO 添加联系人
			this.addContact(applyUserId,receiveUserId,contactId,contactTypeEnum.getType(),applyInfo);
			return  joinType;
		}
		//判断有没有申请过
		UserContactApply dbApply = this.userContactApplyMapper.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receiveUserId,contactId);
		if(null == dbApply) {
			UserContactApply contactApply = new UserContactApply();
			contactApply.setApplyUserId(applyUserId);
			contactApply.setContactType(contactTypeEnum.getType());
			contactApply.setReceiveUserId(receiveUserId);
			contactApply.setLastApplyTime(curTime);
			contactApply.setContactId(contactId);
			contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			this.userContactApplyMapper.insert(contactApply);

		}else{
			//如果之前申请过 现在更新状态
			UserContactApply contactApply = new UserContactApply();
			//重置为初始化状态
			contactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());
			contactApply.setLastApplyTime(curTime);
			contactApply.setApplyInfo(applyInfo);
			this.userContactApplyMapper.updateByApplyId(contactApply,dbApply.getApplyId());
		}

		if(dbApply==null || !UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())){
			//TODO 发送ws消息   如果第一次申请 或者之前申请已经被处理过
		}
		return joinType;
	}

	@Override
	public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
		//群聊人数限制
		if(UserContactTypeEnum.GROUP.getType().equals(contactType)){
			UserContactQuery userContactQuery = new UserContactQuery();
			userContactQuery.setContactId(contactId);
			userContactQuery.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			Integer count = userContactMapper.selectCount(userContactQuery);
			SysSettingDto sysSettingDto = redisComponent.getSysSetting();
			if(count>sysSettingDto.getMaxGroupCount()){
				throw new  BusinessException("成员已满无法加入");
			}
		}
		Date currentDate = new Date();
		//同意 双方添加好友
		List<UserContact> contactList = new ArrayList<>();
		//申请人添加对方
		UserContact userContact = new UserContact();
		userContact.setUserId(applyUserId);
		userContact.setContactId(contactId);
		userContact.setContactType(contactType);
		userContact.setCreateTime(currentDate);
		userContact.setLastUpdateTime(currentDate);
		userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
		contactList.add(userContact);
		//如果是申请好友 接受人添加申请人 群组不用添加对方为好友
		if(UserContactTypeEnum.USER.getType().equals(contactType)){
			userContact = new UserContact();
			userContact.setUserId(receiveUserId);
			userContact.setContactId(applyUserId);
			userContact.setContactType(contactType);
			userContact.setCreateTime(currentDate);
			userContact.setLastUpdateTime(currentDate);
			userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			contactList.add(userContact);
		}
		//批量插入
		userContactMapper.insertOrUpdateBatch(contactList);
		//TODO 如果是好友 接收人也添加申请人为好友 有消息发送 添加缓存

		//TODO 创建会话 发送消息
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
		//移除好友
		UserContact userContact = new UserContact();
		userContact.setStatus(statusEnum.getStatus());
		userContactMapper.updateByUserIdAndContactId(userContact,userId,contactId);
		//好友也移除自己
		UserContact friendContact = new UserContact();
		if(UserContactStatusEnum.DEL==statusEnum){ //删除
			friendContact.setStatus(UserContactStatusEnum.DEL_BE.getStatus());
		} else if (UserContactStatusEnum.BLACKLIST==statusEnum) { //拉黑
			friendContact.setStatus(UserContactStatusEnum.BLACKLIST_BE.getStatus());
		}
		userContactMapper.updateByUserIdAndContactId(friendContact,contactId,userId);
		//TODO 从我的好友列表缓存中删除
		//TODO 从好友列表缓存中删除自己
	}


}