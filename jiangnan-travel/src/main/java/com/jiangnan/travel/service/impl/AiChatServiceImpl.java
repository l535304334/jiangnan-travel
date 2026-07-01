package com.jiangnan.travel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.openai.models.ChatCompletionAssistantMessageParam;
import com.openai.models.ChatCompletionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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

    // ponytail: volatile cache, TTL 10min — avoid DB queries on every chat request
    private volatile String cachedSystemPrompt;
    private volatile long cachedPromptAt;
    private static final long PROMPT_CACHE_TTL_MS = 10 * 60 * 1000;

    @Override
    public ChatVO chat(ChatRequest request, Long userId) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        // 保存用户消息
        saveLog(userId, sessionId, "user", request.getMessage());

        // 构建系统提示词（含文旅知识）
        String systemPrompt = buildSystemPrompt();

        // 构建带多轮上下文的对话参数
        ChatCompletionCreateParams params = buildChatParams(sessionId, systemPrompt, request.getMessage());

        String reply;
        long tokensUsed;
        try {
            ChatCompletion completion = deepSeekClient.chat().completions().create(params);
            reply = completion.choices().get(0).message().content().orElse("抱歉，我没能理解您的问题。");
            tokensUsed = completion.usage().map(u -> u.totalTokens()).orElse(0L);
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败", e);
            reply = getFallbackReply(request.getMessage());
            tokensUsed = 0L;
        }

        // 保存AI回复
        saveLog(userId, sessionId, "assistant", reply);

        return ChatVO.builder()
                .reply(reply)
                .sessionId(sessionId)
                .tokensUsed(tokensUsed)
                .build();
    }

    @Override
    public SseEmitter chatStream(ChatRequest request, Long userId) {
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        // 保存用户消息
        saveLog(userId, sessionId, "user", request.getMessage());

        SseEmitter emitter = new SseEmitter(60000L);

        // 构建系统提示词（含文旅知识）
        String systemPrompt = buildSystemPrompt();

        // 构建带多轮上下文的对话参数
        ChatCompletionCreateParams params = buildChatParams(sessionId, systemPrompt, request.getMessage());

        // 使用 CompletableFuture 异步推送流式数据
        CompletableFuture.runAsync(() -> {
            StringBuilder fullReply = new StringBuilder();
            try {
                deepSeekClient.chat().completions().createStreaming(params)
                        .stream()
                        .forEach(chunk -> {
                            String delta = chunk.choices().get(0).delta().content().orElse("");
                            if (!delta.isEmpty()) {
                                fullReply.append(delta);
                                try {
                                    emitter.send(SseEmitter.event().name("delta").data(delta));
                                } catch (IOException e) {
                                    emitter.completeWithError(e);
                                    return; // stop stream processing, client already gone
                                }
                            }
                        });

                // 发送完成事件
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                emitter.complete();
            } catch (Exception e) {
                log.error("DeepSeek 流式调用失败", e);
                String fallback = getFallbackReply(request.getMessage());
                try {
                    emitter.send(SseEmitter.event().name("delta").data(fallback));
                    emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                    emitter.complete();
                    fullReply.append(fallback);
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }

            // 保存AI回复
            saveLog(userId, sessionId, "assistant", fullReply.toString());
        });

        return emitter;
    }

    @Override
    public List<String> getSessions(Long userId) {
        // 查询该用户的去重 sessionId 列表，按最后消息时间倒序
        List<AiChatLog> logs = aiChatLogMapper.selectList(
                new LambdaQueryWrapper<AiChatLog>()
                        .eq(userId != null, AiChatLog::getUserId, userId)
                        .select(AiChatLog::getSessionId)
                        .groupBy(AiChatLog::getSessionId)
                        .orderByDesc(AiChatLog::getCreateTime));
        return logs.stream()
                .map(AiChatLog::getSessionId)
                .distinct()
                .toList();
    }

    @Override
    public List<AiChatLog> getSessionMessages(String sessionId, Long userId) {
        LambdaQueryWrapper<AiChatLog> wrapper = new LambdaQueryWrapper<AiChatLog>()
                .eq(AiChatLog::getSessionId, sessionId)
                .orderByAsc(AiChatLog::getCreateTime);
        // 只返回属于当前用户的会话消息，userId 为 null 时匹配匿名会话
        if (userId != null) {
            wrapper.eq(AiChatLog::getUserId, userId);
        }
        return aiChatLogMapper.selectList(wrapper);
    }

    /**
     * 构建带多轮上下文的对话参数
     * 从数据库加载同 session 的历史消息作为上下文
     */
    private ChatCompletionCreateParams buildChatParams(String sessionId, String systemPrompt, String userMessage) {
        var builder = ChatCompletionCreateParams.builder()
                .model(model)
                .maxTokens(1024)
                .temperature(0.7);

        // 1. 添加系统提示词
        builder.addSystemMessage(systemPrompt);

        // 2. 加载历史消息（按时间倒序取最近21条，再反转；避免加载全量会话历史）
        List<AiChatLog> history = aiChatLogMapper.selectList(
                new LambdaQueryWrapper<AiChatLog>()
                        .eq(AiChatLog::getSessionId, sessionId)
                        .orderByDesc(AiChatLog::getCreateTime)
                        .last("LIMIT 21"));
        java.util.Collections.reverse(history);

        // 跳过刚保存的当前用户消息（最后一条），取前面最多20条
        int historySize = history.size();
        int startIdx = Math.max(0, historySize - 21); // 21 = 20条历史 + 1条当前
        for (int i = startIdx; i < historySize - 1; i++) {
            AiChatLog log = history.get(i);
            if ("user".equals(log.getRole())) {
                builder.addUserMessage(log.getContent());
            } else if ("assistant".equals(log.getRole())) {
                builder.addMessage(ChatCompletionAssistantMessageParam.builder().content(log.getContent()).build());
            }
        }

        // 3. 添加当前用户消息
        builder.addUserMessage(userMessage);

        return builder.build();
    }

    private String buildSystemPrompt() {
        // ponytail: return cached prompt if still fresh, avoiding DB queries every chat request
        String cached = cachedSystemPrompt;
        if (cached != null && System.currentTimeMillis() - cachedPromptAt < PROMPT_CACHE_TTL_MS) {
            return cached;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("你叫江小游，是「江南出行」智慧服务平台的出行助手。");
        sb.append("\"江\"取自江西与江南，\"小游\"意为伴你悠然出行。\n\n");
        sb.append("你的角色定位：\n");
        sb.append("- 你是一个温暖亲切、知识丰富的出行导游助手\n");
        sb.append("- 你既能解决出行问题，又能介绍江西风土人情\n");
        sb.append("- 你了解江西每个城市的历史文化和特色美食\n\n");
        sb.append("你的核心能力：\n");
        sb.append("1. 出行服务：帮助用户下单、查价、查路线、解答优惠券和安全问题\n");
        sb.append("2. 文旅推荐：根据用户兴趣推荐江西景点，讲景点背后的故事\n");
        sb.append("3. 闲聊互动：可以聊江西美食、历史、民俗\n\n");
        sb.append("回答风格：\n");
        sb.append("- 用温暖亲切的语气，像朋友一样交流\n");
        sb.append("- 回答问题简洁清晰（200字以内），可以适当反问引导对话\n");
        sb.append("- 当用户问江西景点时，先给出有画面感的描述，再补充文化背景\n");
        sb.append("- 如果不知道答案，诚实说不知道，不要编造\n\n");
        sb.append("江西省文旅知识（动态加载）：\n");

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
        String result = sb.toString();
        cachedSystemPrompt = result;
        cachedPromptAt = System.currentTimeMillis();
        return result;
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
