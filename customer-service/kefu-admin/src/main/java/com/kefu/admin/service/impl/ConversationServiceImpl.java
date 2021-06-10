package com.kefu.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kefu.admin.entity.Conversation;
import com.kefu.admin.mapper.ConversationMapper;
import com.kefu.admin.service.ConversationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author jurui
 * @date 2020-06-08
 */
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {

    /**
     * 查询会话列表
     *
     * @param userId 用户编号
     * @return
     */
    @Override
    public List<Conversation> selectListByUserId(Integer userId) {
        List<Conversation> conversations = baseMapper.selectListByUserId(userId);
        return conversations;
    }

    @Override
    public List<Conversation> selectAllList() {
        List<Conversation> conversations = baseMapper.selectAllList();
        //for (Conversation conversation : conversations) {
        //    System.out.println(conversation.getId()+"========="+conversation.getFromUser());
        //    System.out.println();
        //    System.out.println();
        //}
        return conversations;
    }
}
