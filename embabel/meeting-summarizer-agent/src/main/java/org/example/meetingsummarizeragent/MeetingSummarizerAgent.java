package org.example.meetingsummarizeragent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;

import java.util.List;

@Agent(description = "An agent that transforms raw transcripts into structured summaries.")
public class MeetingSummarizerAgent {

    @Action(description = "Extracts names of people mentioned in the text")
    public List<String> extractParticipants(String transcript) {
        // The framework handles the LLM call to find names based on the description
        return null;
    }

    @AchievesGoal(description = "Create a summary. Focus specifically on technical decisions.")
    public MeetingSummary summarize(String transcript, String technicalFocusArea) {
        // Embabel uses 'technicalFocusArea' as additional context for the LLM
        return null;
    }
}