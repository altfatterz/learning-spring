package org.example.meetingsummarizeragent;

import java.util.List;

public record MeetingSummary(
        String title,
        String highLevelSummary,
        List<String> actionItems,
        List<String> participants
) {
}