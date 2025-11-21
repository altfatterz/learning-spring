package org.example.openaichatdemo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final SimpleLoggerAdvisor simpleLoggerAdvisor;

    public ChatController(ChatClient.Builder builder, SimpleLoggerAdvisor simpleLoggerAdvisor) {
        this.chatClient = builder.build();
        this.simpleLoggerAdvisor = simpleLoggerAdvisor;
    }

    @GetMapping("/api/chat")
    public String chat(@RequestParam String prompt) {
        return chatClient.prompt().user(prompt).advisors(simpleLoggerAdvisor).call().content();
    }

    @GetMapping("/api/chat-verbose")
    public ChatResponse chatVerbose(@RequestParam String prompt) {
        return chatClient.prompt().user(prompt).advisors(simpleLoggerAdvisor).call().chatResponse();
    }


}
