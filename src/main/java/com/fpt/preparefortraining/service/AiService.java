package com.fpt.preparefortraining.service;

import com.fpt.preparefortraining.dto.request.ChatRequest;
import com.fpt.preparefortraining.dto.response.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.util.Map;
import java.util.HashMap;

@Service
public class AiService {
    
    @Value("${ai.service.url:http://localhost:8000}")
    private String aiServiceUrl;

    public ChatResponse askChatbot(ChatRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        String url = aiServiceUrl + "/api/v1/chat";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<ChatRequest> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ChatResponse> response = restTemplate.postForEntity(url, entity, ChatResponse.class);
        return response.getBody();
    }
    
    public void reloadKnowledge(Long projectId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = aiServiceUrl + "/api/v1/reload";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Long> request = new HashMap<>();
        request.put("projectId", projectId);
        HttpEntity<Map<String, Long>> entity = new HttpEntity<>(request, headers);
        
        restTemplate.postForEntity(url, entity, Void.class);
    }
}
