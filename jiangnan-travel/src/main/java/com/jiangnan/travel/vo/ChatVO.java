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

    @Schema(description = "AI回复内容")
    private String reply;
    @Schema(description = "会话ID")
    private String sessionId;
    @Schema(description = "消耗的token数")
    private Long tokensUsed;
}
