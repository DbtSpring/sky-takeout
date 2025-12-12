package com.sky.task;

import com.sky.websocket.WebSocketServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * WebSocket入门案例的后端代码
 */
@Component
public class WebSocketTask {
    @Autowired
    private WebSocketServer webSocketServer;

//    @Scheduled(cron="0/5 * * * * ?")
    public void sendMessageToAllClient(){
        webSocketServer.sendToAllClient("服务端消息：" + DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
    }
}
