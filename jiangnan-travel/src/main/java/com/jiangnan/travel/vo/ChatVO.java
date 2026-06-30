package com.jiangnan.travel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI对话响应")
public class ChatVO {

    @Schema(description = "AI回复内容", example = "今天无锡天气晴朗，气温15-22℃。")
    private String reply;
    @Schema(description = "会话ID", example = "session_001")
    private String sessionId;
    @Schema(description = "消耗的token数", example = "150")
    private Long tokensUsed;
}
