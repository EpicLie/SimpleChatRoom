package com.chatroom;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.chatroom.ChatHandler;

@Configuration
@EnableWebSocket // 启用websocket功能
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 注册 WebSocket 处理器  即把特定的url和特定的处理器连接起来
        registry.addHandler(new ChatHandler(), "/chat").setAllowedOrigins("*");//允许跨域请求
    }

}
