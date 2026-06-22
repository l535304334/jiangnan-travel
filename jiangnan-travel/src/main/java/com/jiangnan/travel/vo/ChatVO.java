package com.jiangnan.travel.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatVO {

    private String reply;
    private String sessionId;
    private Long tokensUsed;
}
