package com.kefu.admin.controller.chat;

import com.kefu.admin.service.ConversationService;
import com.kefu.common.vo.ResultVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jurui
 * @date 2020-06-08
 */
@Slf4j
@RestController
@RequestMapping("/conversation")
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    @GetMapping("/list/{userId}")
    public ResultVo getConversationList(@PathVariable Integer userId) {
        log.info("获取会话列表数据,userId={}", userId);
        return ResultVo.success(conversationService.selectListByUserId(userId));
    }

    @GetMapping("/alllist")
    public ResultVo getAllConversationList() {
        return ResultVo.success(conversationService.selectAllList());
    }
}
