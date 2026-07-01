package com.jiangnan.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "AI对话请求")
public class ChatRequest {

    @NotBlank(message = "消息不能为空")
    @Size(max = 2000, message = "消息长度不能超过2000字符")
    @Schema(description = "用户消息内容", example = "今天天气怎么样？")
    private String message;

    @Schema(description = "会话ID，用于多轮对话", example = "session_001")
    private String sessionId;
}
