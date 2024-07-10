package com.easybbs.entity.vo;

import com.easybbs.entity.po.GroupInfo;
import com.easybbs.entity.po.UserContact;

import java.util.List;

/**
 * @ClassName GroupInfoVO
 * @Description 添加描述
 * @Author Suguo
 * @LastChangeDate 2024/7/10 下午10:51
 */

public class GroupInfoVO {
    private GroupInfo groupInfo;
    private List<UserContact> userContactList;

    public List<UserContact> getUserContactList() {
        return userContactList;
    }

    public void setUserContactList(List<UserContact> userContactList) {
        this.userContactList = userContactList;
    }

    public GroupInfo getGroupInfo() {
        return groupInfo;
    }

    public void setGroupInfo(GroupInfo groupInfo) {
        this.groupInfo = groupInfo;
    }
}

