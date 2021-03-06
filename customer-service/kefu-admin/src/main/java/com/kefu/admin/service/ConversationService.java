package com.kefu.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kefu.admin.entity.Conversation;

import java.util.List;

/**
 * @author jurui
 * @date 2020-06-08
 */
public interface ConversationService extends IService<Conversation> {

    /**
     * 查询会话列表
     *
     * @param userId 用户编号
     * @return
     */
    List<Conversation> selectListByUserId(Integer userId);

    List<Conversation> selectAllList();
}
