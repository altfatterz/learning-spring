package org.example.simplemcpclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
    private final ChatClient chatClient;
    private final SimpleLoggerAdvisor simpleLoggerAdvisor;

    public ChatController(ChatClient.Builder builder,
                          SimpleLoggerAdvisor simpleLoggerAdvisor,
                          ToolCallbackProvider tools) {
        this.simpleLoggerAdvisor = simpleLoggerAdvisor;
        Arrays.asList(tools.getToolCallbacks()).forEach(
                toolCallback -> log.info("Tool callback found: {}", toolCallback.getToolDefinition())
        );
        this.chatClient = builder.defaultToolCallbacks(tools).build();

    }

    @GetMapping("/api/chat")
    public String chat(@RequestParam String prompt) {
        return chatClient.prompt().user(prompt).advisors(simpleLoggerAdvisor).call().content();
    }


}
