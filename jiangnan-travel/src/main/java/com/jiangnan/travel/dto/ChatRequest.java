package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "AI对话请求")
public class ChatRequest {

    @NotBlank(message = "消息不能为空")
    @Schema(description = "用户消息内容")
    private String message;

    @Schema(description = "会话ID，用于多轮对话")
    private String sessionId;
}
