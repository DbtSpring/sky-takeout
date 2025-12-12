package com.sky.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {
    private static Map<String, Session> sessionMap = new HashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid){
        log.info("建立WebSocket连接，session：{},sid:{}",session,sid);
        sessionMap.put(sid, session);
        System.out.println("sessionMap:" + sessionMap);
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid){
        log.info("关闭WebSocket连接，sid:{}",sid);
        sessionMap.remove(sid);
        System.out.println("sessionMap:" + sessionMap);
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid){
        log.info("来自sid：{}的信息：{}",sid, message);
        System.out.println("sessionMap:" + sessionMap);

    }

    public void sendToAllClient(String message){
        log.info("群发，message：{}",message);
        System.out.println("sessionMap:" + sessionMap);
        Collection<Session> sessions = sessionMap.values();
        for(Session session: sessions){
            try{
                session.getBasicRemote().sendText(message);
            }catch (Exception exception){
                exception.printStackTrace();
            }
        }

    }



}
