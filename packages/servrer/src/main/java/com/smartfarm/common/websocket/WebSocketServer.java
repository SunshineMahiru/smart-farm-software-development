package com.smartfarm.common.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ServerEndpoint("/ws/{userId}")
public class WebSocketServer {

    // 线程安全的哈希表，用于存放每个客户端对应的 WebSocketServer 对象
    private static final ConcurrentHashMap<String, Session> SESSION_POOL = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        SESSION_POOL.put(userId, session);
        log.info("【WebSocket】有新连接加入！用户ID：{}，当前在线人数：{}", userId, SESSION_POOL.size());
    }

    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        SESSION_POOL.remove(userId);
        log.info("【WebSocket】连接断开！用户ID：{}，当前在线人数：{}", userId, SESSION_POOL.size());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【WebSocket】发生错误", error);
    }

    // ---------------- 提供给成员 2 和成员 3 调用的公有方法 ---------------- //

    /**
     * 发送消息给指定用户
     */
    public void sendToUser(String userId, String message) {
        Session session = SESSION_POOL.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.error("【WebSocket】发送消息失败，用户ID：{}", userId, e);
            }
        }
    }

    /**
     * 广播消息给所有人 (常用于大屏 AI 预警)
     */
    public void broadcastAll(String message) {
        for (Session session : SESSION_POOL.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("【WebSocket】群发消息失败", e);
                }
            }
        }
    }
}