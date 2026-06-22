package com.jiangnan.travel.service;

import com.jiangnan.travel.dto.ChatRequest;
import com.jiangnan.travel.vo.ChatVO;

public interface AiChatService {

    ChatVO chat(ChatRequest request, Long userId);
}
