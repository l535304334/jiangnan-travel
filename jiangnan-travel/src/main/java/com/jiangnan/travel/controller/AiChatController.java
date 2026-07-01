package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.ChatRequest;
import com.jiangnan.travel.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI客服", description = "AI智能客服对话")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI客服进行对话")
    public Result<?> chat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(aiChatService.chat(request, userId));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "AI流式对话", description = "SSE流式AI对话，逐字返回回复内容")
    public SseEmitter chatStream(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return aiChatService.chatStream(request, userId);
    }

    @GetMapping("/sessions")
    @Operation(summary = "会话列表", description = "获取用户的AI对话会话列表")
    public Result<?> getSessions(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(aiChatService.getSessions(userId));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    @Operation(summary = "会话消息", description = "获取指定会话的全部消息")
    public Result<?> getSessionMessages(@PathVariable String sessionId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(aiChatService.getSessionMessages(sessionId, userId));
    }
}
