package com.easybbs.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import com.easybbs.entity.config.AppConfig;
import com.easybbs.entity.constants.Constants;
import com.easybbs.entity.dto.SysSettingDto;
import com.easybbs.entity.enums.ResponseCodeEnum;
import com.easybbs.entity.enums.UserContactStatusEnum;
import com.easybbs.entity.enums.UserContactTypeEnum;
import com.easybbs.entity.po.UserContact;
import com.easybbs.exception.BusinessException;
import com.easybbs.mappers.UserContactMapper;
import com.easybbs.redis.RedisComponent;
import com.easybbs.redis.RedisUtils;
import org.springframework.stereotype.Service;

import com.easybbs.entity.enums.PageSize;
import com.easybbs.entity.query.GroupInfoQuery;
import com.easybbs.entity.po.GroupInfo;
import com.easybbs.entity.vo.PaginationResultVO;
import com.easybbs.entity.query.SimplePage;
import com.easybbs.mappers.GroupInfoMapper;
import com.easybbs.service.GroupInfoService;
import com.easybbs.utils.StringTools;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


/**
 *  业务接口实现
 */
@Service("groupInfoService")
public class GroupInfoServiceImpl implements GroupInfoService {

	@Resource
	private RedisComponent 	redisComponent;

	@Resource
	private UserContactMapper userContactMapper;

	@Resource
	private AppConfig appConfig;

	@Resource
	private GroupInfoMapper<GroupInfo, GroupInfoQuery> groupInfoMapper;

	/**
	 * 根据条件查询列表
	 */
	@Override
	public List<GroupInfo> findListByParam(GroupInfoQuery param) {
		return this.groupInfoMapper.selectList(param);
	}

	/**
	 * 根据条件查询列表
	 */
	@Override
	public Integer findCountByParam(GroupInfoQuery param) {
		return this.groupInfoMapper.selectCount(param);
	}

	/**
	 * 分页查询方法
	 */
	@Override
	public PaginationResultVO<GroupInfo> findListByPage(GroupInfoQuery param) {
		int count = this.findCountByParam(param);
		int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();

		SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
		param.setSimplePage(page);
		List<GroupInfo> list = this.findListByParam(param);
		PaginationResultVO<GroupInfo> result = new PaginationResultVO(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
		return result;
	}

	/**
	 * 新增
	 */
	@Override
	public Integer add(GroupInfo bean) {
		return this.groupInfoMapper.insert(bean);
	}

	/**
	 * 批量新增
	 */
	@Override
	public Integer addBatch(List<GroupInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.groupInfoMapper.insertBatch(listBean);
	}

	/**
	 * 批量新增或者修改
	 */
	@Override
	public Integer addOrUpdateBatch(List<GroupInfo> listBean) {
		if (listBean == null || listBean.isEmpty()) {
			return 0;
		}
		return this.groupInfoMapper.insertOrUpdateBatch(listBean);
	}

	/**
	 * 多条件更新
	 */
	@Override
	public Integer updateByParam(GroupInfo bean, GroupInfoQuery param) {
		StringTools.checkParam(param);
		return this.groupInfoMapper.updateByParam(bean, param);
	}

	/**
	 * 多条件删除
	 */
	@Override
	public Integer deleteByParam(GroupInfoQuery param) {
		StringTools.checkParam(param);
		return this.groupInfoMapper.deleteByParam(param);
	}

	/**
	 * 根据GroupId获取对象
	 */
	@Override
	public GroupInfo getGroupInfoByGroupId(String groupId) {
		return this.groupInfoMapper.selectByGroupId(groupId);
	}

	/**
	 * 根据GroupId修改
	 */
	@Override
	public Integer updateGroupInfoByGroupId(GroupInfo bean, String groupId) {
		return this.groupInfoMapper.updateByGroupId(bean, groupId);
	}

	/**
	 * 根据GroupId删除
	 */
	@Override
	public Integer deleteGroupInfoByGroupId(String groupId) {
		return this.groupInfoMapper.deleteByGroupId(groupId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
		Date curDate = new Date();

		if(StringTools.isEmpty(groupInfo.getGroupId())){ //如果空 新增 不然就是修改
			GroupInfoQuery groupInfoQuery = new GroupInfoQuery();
			groupInfoQuery.setGroupOwnerId(groupInfo.getGroupOwnerId());
			//获取系统设置 如果创建群组超限则不允许
			Integer count = this.groupInfoMapper.selectCount(groupInfoQuery);
			SysSettingDto sysSettingDto = redisComponent.getSysSetting();
			if(count>=sysSettingDto.getMaxGroupCount()){
				throw  new BusinessException("最多支持创建"+sysSettingDto.getMaxGroupCount()+"个群聊");
			}
			if(null == avatarFile){
				throw  new BusinessException(ResponseCodeEnum.CODE_600);
			}
			groupInfo.setCreateTime(curDate);
			groupInfo.setGroupId(StringTools.getGroupId());
			this.groupInfoMapper.insert(groupInfo);

			//将群组添加为联系人
			UserContact userContact = new UserContact();
			userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
			userContact.setContactType(UserContactTypeEnum.GROUP.getType());
			userContact.setContactId(groupInfo.getGroupId());
			userContact.setUserId(groupInfo.getGroupOwnerId());
			userContact.setCreateTime(curDate);
			userContact.setLastUpdateTime(curDate);
			this.userContactMapper.insert(userContact);
			//TODO 创建会话
			//TODO 发送消息(bot)
		}else{
			GroupInfo dbInfo = this.groupInfoMapper.selectByGroupId(groupInfo.getGroupId());
			//修改前判断当前用户是不是对应的群主 后端保底
			if(!dbInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())){
				throw  new BusinessException(ResponseCodeEnum.CODE_600);
			}
			this.groupInfoMapper.updateByGroupId(groupInfo, groupInfo.getGroupId());
			//TODO 更新相关表冗余信息
			//TODO 修改群昵称发送ws消息
			if(null == avatarFile){
				return;
			}
			String baseFold = appConfig.getProjectFolder()+ Constants.FILE_FOLDER_FILE;
			File  targetFileFolder = new File(baseFold + Constants.FILE_FOLDER_AVATAR_NAME);
			//没有就建一个
			if(!targetFileFolder.exists()){
				targetFileFolder.mkdirs();
			}
			String filePath = targetFileFolder.getPath()+"/"+groupInfo.getGroupId()+Constants.IMAGE_SUFFIX;
		    //原图与缩略图
			avatarFile.transferTo(new File(filePath));
			avatarCover.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
		}
	}
}