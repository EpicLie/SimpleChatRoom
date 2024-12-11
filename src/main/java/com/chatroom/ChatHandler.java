package com.chatroom;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;



@Slf4j
@Controller
public class ChatHandler extends TextWebSocketHandler {
    @Data
    public static class WhoAndWhat{
        String userName;
        String text;
        Integer isFirst = 0;
    }

//    private static WhoAndWhat whoAndWhat;

    // 存储所有连接的客户端
    // 采用写时复制的方法，实现了线程安全。但是对于写频繁的任务可能开销较大。
    private static final CopyOnWriteArrayList<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
//    private static final UpdateGuests updateGuests = new UpdateGuests();
    private static final WhoAndWhat updateUsers = new WhoAndWhat();
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        sessions.add(session); //新客户端接入时加入会话列表
        log.info(String.format("New connection established: %s", session.getId()));
        String ips = "\nOnline IPs:\n";
        for(WebSocketSession s:sessions){
            ips += "\t\t" +s.getRemoteAddress().getAddress().getHostAddress() + '\n';
        }
        log.info(ips);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String messageJson = message.getPayload();
        // 先对消息进行反序列化
        ObjectMapper objectMapper = new ObjectMapper();
        WhoAndWhat whoAndWhat = objectMapper.readValue(messageJson, WhoAndWhat.class);

        if(whoAndWhat.isFirst==1)
        {
            if(updateUsers.getUserName() == null)
                updateUsers.setUserName(whoAndWhat.getUserName());
            else
                updateUsers.setUserName(updateUsers.getUserName() + '\n' + whoAndWhat.getUserName());
            updateUsers.setIsFirst(1);
            log.info(String.format("Who has connected: %s", whoAndWhat.getUserName()));
            String updateUsersString = objectMapper.writeValueAsString(updateUsers);
            for(WebSocketSession s:sessions){
                if(s.isOpen()){
                    s.sendMessage(new TextMessage(updateUsersString));
                }
            }
        }
        else if(whoAndWhat.isFirst==0){
            // 处理接收到的消息，广播给所有已连接的客户端
            log.info(String.format("Who said: %s", whoAndWhat.getUserName()));
            log.info(String.format("Received message: %s", whoAndWhat.getText()));
            // 广播，除了当前会话
            for(WebSocketSession s:sessions){
                if(s.isOpen()){
                    s.sendMessage(new TextMessage(objectMapper.writeValueAsString(whoAndWhat)));
                }
            }
        }
        else if (whoAndWhat.isFirst==2) {
            if(updateUsers.getUserName().contains("\n"))
                updateUsers.setUserName(updateUsers.getUserName().replace(whoAndWhat.getUserName()+'\n',"" ));
            else
                updateUsers.setUserName(updateUsers.getUserName().replace(whoAndWhat.getUserName(),"" ));
            updateUsers.setIsFirst(1);
            log.info(String.format("Who has disconnected: %s", whoAndWhat.getUserName()));
            String updateUsersString = objectMapper.writeValueAsString(updateUsers);
            for(WebSocketSession s:sessions){
                if(s.isOpen()){
                    s.sendMessage(new TextMessage(updateUsersString));
                }
            }
        }



    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);  // 客户端断开连接时移除
        log.info(String.format("Connection closed: %s", session.getId()));
    }

}

