package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.ChatRequest;
import com.jiangnan.travel.entity.AiChatLog;
import com.jiangnan.travel.vo.ChatVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface AiChatService {

    ChatVO chat(ChatRequest request, Long userId);

    SseEmitter chatStream(ChatRequest request, Long userId);

    List<String> getSessions(Long userId);

    List<AiChatLog> getSessionMessages(String sessionId);
}
