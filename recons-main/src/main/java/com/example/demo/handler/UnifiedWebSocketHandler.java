package com.example.demo.handler;

import com.example.demo.model.*;
import com.example.demo.service.DataStreamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UnifiedWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ObjectMapper objectMapper;

    private DataStreamService dataStreamService;

    @Autowired
    public void setDataStreamService(DataStreamService dataStreamService) {
        this.dataStreamService = dataStreamService;
    }

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();
    private final List<UserSession> userSessions = new ArrayList<>();
    private final List<UserSessionForward> userSessionsForward = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        userSessions.add(new UserSession(session, null, null));
        userSessionsForward.add(new UserSessionForward(session, null, null, 0, 0));
        ResponseHandler.sendWebSocketResponse(session, "Connection established", HttpStatus.OK, null);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        userSessions.removeIf(userSession -> userSession.getSession().equals(session));
        userSessionsForward.removeIf(userSession -> userSession.getSession().equals(session));
        ResponseHandler.sendWebSocketResponse(session, "Connection closed", HttpStatus.OK, null);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            if (isSpotBanksMessage(payload)) {
                handleSpotBanksMessage(session, payload);
            } else if (isCurrencyPairMessage(payload)) {
                handleCurrencyPairMessage(session, payload);
            } else if (isSpotForwardMessage(payload)) {
                handleSpotForwardMessage(session, payload);
            } else {
                ResponseHandler.sendWebSocketResponse(session, "Error: Invalid message format", HttpStatus.BAD_REQUEST, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseHandler.sendWebSocketResponse(session, "Error processing request", HttpStatus.INTERNAL_SERVER_ERROR, null);
        }
    }

    private boolean isSpotBanksMessage(String payload) {
        try {
            objectMapper.readValue(payload, SpotBanks.class);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void handleSpotBanksMessage(WebSocketSession session, String payload) throws IOException {
        SpotBanks spotBanks = objectMapper.readValue(payload, SpotBanks.class);
        Object spotValues = dataStreamService.processSpotPriceRequest(spotBanks);
        ResponseEntity<Object> responseEntity = ResponseHandler.generateResponse("Spot Banks processed successfully", HttpStatus.OK, spotValues);
        ResponseHandler.sendWebSocketResponse(session, "Spot Banks processed successfully", HttpStatus.OK, responseEntity.getBody());
        
        broadcastMessage("Spot Banks processed successfully", spotValues);
    }

    private boolean isCurrencyPairMessage(String payload) {
        try {
            objectMapper.readValue(payload, CurrencyPairMessage.class);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void handleCurrencyPairMessage(WebSocketSession session, String payload) throws IOException {
        CurrencyPairMessage currencyPairMessage = objectMapper.readValue(payload, CurrencyPairMessage.class);
        for (UserSession userSession : userSessions) {
            if (userSession.getSession().equals(session)) {
                userSession.setPairType(currencyPairMessage.getPairType());
                userSession.setCcyPair(currencyPairMessage.getCcyPair());
                break;
            }
        }
        ResponseEntity<Object> responseEntity = ResponseHandler.generateResponse("Currency Pair processed successfully", HttpStatus.OK, currencyPairMessage);
        ResponseHandler.sendWebSocketResponse(session, "Currency Pair processed successfully", HttpStatus.OK, responseEntity.getBody());
        
        broadcastMessage("Currency Pair processed successfully", currencyPairMessage);
    }

    private boolean isSpotForwardMessage(String payload) {
        try {
            objectMapper.readValue(payload, SpotForwardMessage.class);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void handleSpotForwardMessage(WebSocketSession session, String payload) throws IOException {
        SpotForwardMessage spotForwardMessage = objectMapper.readValue(payload, SpotForwardMessage.class);
        for (UserSessionForward userSession : userSessionsForward) {
            if (userSession.getSession().equals(session)) {
                userSession.setPairType(spotForwardMessage.getPairType());
                userSession.setCcyPair(spotForwardMessage.getCcyPair());
                userSession.setNoOfDays(spotForwardMessage.getNoOfDays());
                userSession.setAmount(spotForwardMessage.getAmount());
                break;
            }
        }
        ResponseEntity<Object> responseEntity = ResponseHandler.generateResponse("Spot Forward processed successfully", HttpStatus.OK, spotForwardMessage);
        ResponseHandler.sendWebSocketResponse(session, "Spot Forward processed successfully", HttpStatus.OK, responseEntity.getBody());
        
        broadcastMessage("Spot Forward processed successfully", spotForwardMessage);
    }

    private void broadcastMessage(String message, Object data) {
        sessions.forEach(webSocketSession -> {
            try {
                Object messageToSend = ResponseHandler.generateResponse(message, HttpStatus.OK, data);
                webSocketSession.sendMessage(new TextMessage(objectMapper.writeValueAsString(messageToSend)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public Set<WebSocketSession> getSessions() {
        return Collections.unmodifiableSet(sessions);
    }

    public List<UserSession> getUserSessions() {
        return userSessions;
    }

    public List<UserSessionForward> getUserSessionsForward() {
        return userSessionsForward;
    }
}
