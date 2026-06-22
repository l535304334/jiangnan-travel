package com.jiangnan.travel.service.impl;

import com.jiangnan.travel.dto.ChatRequest;
import com.jiangnan.travel.entity.AiChatLog;
import com.jiangnan.travel.entity.CityLandmark;
import com.jiangnan.travel.entity.CityQuote;
import com.jiangnan.travel.mapper.AiChatLogMapper;
import com.jiangnan.travel.mapper.CityLandmarkMapper;
import com.jiangnan.travel.mapper.CityQuoteMapper;
import com.jiangnan.travel.service.AiChatService;
import com.jiangnan.travel.vo.ChatVO;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private final OpenAIClient deepSeekClient;
    private final AiChatLogMapper aiChatLogMapper;
    private final CityLandmarkMapper cityLandmarkMapper;
    private final CityQuoteMapper cityQuoteMapper;

    @Value("${deepseek.model}")
    private String model;

    @Override
    public ChatVO chat(ChatRequest request, Long userId) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        // 保存用户消息
        saveLog(userId, sessionId, "user", request.getMessage());

        // 构建系统提示词（含文旅知识）
        String systemPrompt = buildSystemPrompt();

        // 调用 DeepSeek
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(model)
                .addSystemMessage(systemPrompt)
                .addUserMessage(request.getMessage())
                .maxTokens(1024)
                .temperature(0.7)
                .build();

        String reply;
        long tokensUsed;
        try {
            ChatCompletion completion = deepSeekClient.chat().completions().create(params);
            reply = completion.choices().get(0).message().content().orElse("抱歉，我没能理解您的问题。");
            tokensUsed = 0L;
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            reply = getFallbackReply(request.getMessage());
            tokensUsed = 0;
        }

        // 保存AI回复
        saveLog(userId, sessionId, "assistant", reply);

        return ChatVO.builder()
                .reply(reply)
                .sessionId(sessionId)
                .tokensUsed(tokensUsed)
                .build();
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("你是「江南出行」智慧服务平台的AI客服助手。");
        sb.append("你的职责是：帮助用户解决网约车相关问题（下单、计价、优惠券、行程安全等），");
        sb.append("同时适当介绍江西省的文旅特色。");
        sb.append("请用亲切友好的语气回复，回复长度控制在200字以内。\n\n");
        sb.append("江西省文旅知识：\n");

        // 加载地标
        List<CityLandmark> landmarks = cityLandmarkMapper.selectList(null);
        for (CityLandmark lm : landmarks) {
            sb.append("- ").append(lm.getCity()).append(" ").append(lm.getName())
                    .append("：").append(lm.getDescription()).append("\n");
        }

        // 加载寄语
        List<CityQuote> quotes = cityQuoteMapper.selectList(null);
        if (!quotes.isEmpty()) {
            sb.append("\n文化寄语：\n");
            for (CityQuote q : quotes) {
                sb.append("- [").append(q.getCity()).append("] ").append(q.getContent());
                if (q.getAuthor() != null && !q.getAuthor().isEmpty()) {
                    sb.append(" ——").append(q.getAuthor());
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String getFallbackReply(String message) {
        if (message.contains("下单") || message.contains("叫车") || message.contains("打车")) {
            return "您可以在首页输入目的地，系统会为您预估价格并匹配附近司机。下单后会实时显示司机位置。";
        }
        if (message.contains("价格") || message.contains("费用") || message.contains("多少钱")) {
            return "我们的计费采用阶梯计价：起步价含3公里，3-50公里按车型单价计费，50公里以上远程有优惠。具体价格下单前会为您预估。";
        }
        if (message.contains("安全") || message.contains("保障")) {
            return "江南出行提供全程行程分享、一键报警、AI风控引擎实时守护您的出行安全。";
        }
        if (message.contains("优惠") || message.contains("券")) {
            return "新用户注册即送优惠券！您可以在「我的-优惠券」中查看和使用。";
        }
        return "感谢您的咨询！江南出行客服正在为您服务。您想了解下单、计价、安全、优惠券，还是江西省内文旅信息呢？";
    }

    private void saveLog(Long userId, String sessionId, String role, String content) {
        AiChatLog log = new AiChatLog();
        log.setUserId(userId);
        log.setSessionId(sessionId);
        log.setRole(role);
        log.setContent(content);
        log.setTokensUsed(0);
        aiChatLogMapper.insert(log);
    }
}
