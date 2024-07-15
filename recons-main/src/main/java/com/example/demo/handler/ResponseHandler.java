package com.example.demo.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ResponseHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void sendWebSocketResponse(WebSocketSession session, String message, HttpStatus status, Object responseObj) {
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("message", message);
        responseMap.put("status", status.value());
        responseMap.put("data", responseObj);
        responseMap.put("timestamp", System.currentTimeMillis());
        responseMap.put("date", new java.util.Date());

        try {
            String responseJson = objectMapper.writeValueAsString(responseMap);
            session.sendMessage(new TextMessage(responseJson));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            // Handle JSON processing exception
        } catch (IOException e) {
            e.printStackTrace();
            // Handle IO exception
        }
    }

    public static ResponseEntity<Object> generateResponse(String message, HttpStatus status, Object responseObj) {
        Map<String, Object> map = new HashMap<>();
        map.put("message", message);
        map.put("status", status.value());
        map.put("data", responseObj);
        map.put("timestamp", System.currentTimeMillis());
        map.put("date", new java.util.Date());
        return new ResponseEntity<>(map, status);
    }
}
