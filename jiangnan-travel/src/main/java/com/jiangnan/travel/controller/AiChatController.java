package com.jiangnan.travel.controller;

import com.jiangnan.travel.common.Result;
import com.jiangnan.travel.dto.ChatRequest;
import com.jiangnan.travel.service.AiChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Tag(name = "AI客服", description = "AI智能客服对话")
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    @Operation(summary = "AI对话", description = "与AI客服进行对话")
    public Result<?> chat(@Valid @RequestBody ChatRequest request, Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return Result.ok(aiChatService.chat(request, userId));
    }
}
