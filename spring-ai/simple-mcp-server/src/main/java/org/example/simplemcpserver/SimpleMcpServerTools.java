package org.example.simplemcpserver;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class SimpleMcpServerTools {

    private static final Logger logger = Logger.getLogger(SimpleMcpServerTools.class.getName());

    @McpTool(name = "investing-simplified-latest-videos", description = "I will return Investing Simplified - Professor G (Nolan Gouveia) latest 3 Youtube videos")
    public String getLatestVideos() {
        logger.info("getLatestVideos called");
        return """
                - ** If I could pick just one GROWTH ETF 2026? (SPMO, VGT, QQQM) ** - Nov 10, 2025
                
                    https://www.youtube.com/watch?v=SpNreWy58rE&t=3s

                - ** Finally swapping SCHD to this Value ETF (time for more gains) ** - Nov 8, 2025
                
                    https://www.youtube.com/watch?v=Skzq4PJ19xs&t=15s
                
                - ** Where is this stock market going in 2026? (My opinion) ** - Nov 5, 2025
                
                    https://www.youtube.com/watch?v=A0wLptLsaWI&t=1s
                
                """;
    }

}
