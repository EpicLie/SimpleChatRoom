package com.chatroom.client;

import com.chatroom.ChatHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class WebSocketJavaFXClient extends Application {
    private TextArea messageArea;
    private TextArea guestsArea;
    private TextField messageField;
    private WebSocketClient client;

//    public static void main(String[] args){
//        launch(args);
//    }
    @Data
    public static class UpdateGuests{
        CopyOnWriteArrayList<String> users;
        Boolean update;

    }

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.setTitle("WebSocket Client");
        GridPane gridPane = new GridPane();

        for (int i = 0; i < 10; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(10);  // 每一行占据 10% 的高度
            gridPane.getRowConstraints().add(rowConstraints);
        }

        for (int i = 0; i < 10; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(10);  // 每一行占据 10% 的高度
            gridPane.getColumnConstraints().add(columnConstraints);
        }

        // 多行文本框
        messageArea = new TextArea();
        guestsArea = new TextArea();
        messageArea.setEditable(false);
        guestsArea.setEditable(false);
        gridPane.add(messageArea, 0,0);
//        messageArea.setPrefSize(150,200);
        GridPane.setColumnSpan(messageArea, 5);
        GridPane.setRowSpan(messageArea, 7);

        gridPane.add(guestsArea, 6,0);
        GridPane.setColumnSpan(guestsArea, 4);
        GridPane.setRowSpan(guestsArea, 7);

        // 单行输入框
        messageField = new TextField();
//        messageField.setPrefWidth(200);
        Button enter = new Button("发送");
        VBox vBox = new VBox(20);
        vBox.getChildren().addAll(messageField, enter);
        vBox.setAlignment(Pos.CENTER);

        // 检测回车
        messageField.setOnAction(event -> {
//            String message = messageField.getText();
            ChatHandler.WhoAndWhat whoAndWhat = new ChatHandler.WhoAndWhat();
            whoAndWhat.setText(messageField.getText());
            whoAndWhat.setUserName(LoginPage.user.getUserName());
            ObjectMapper objectMapper = new ObjectMapper();
            String message = null;
            try {
                message = objectMapper.writeValueAsString(whoAndWhat);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if(client!=null && client.isOpen()){
                client.send(message);
//                messageArea.appendText(String.format("%s: ",LoginPage.user.userName) + message + "\n");
            }
            messageField.clear();
        });

        // 检测点击确认
        enter.setOnAction(event -> {
            ChatHandler.WhoAndWhat whoAndWhat = new ChatHandler.WhoAndWhat();
            whoAndWhat.setText(messageField.getText());
            whoAndWhat.setUserName(LoginPage.user.getUserName());
            ObjectMapper objectMapper = new ObjectMapper();
            String message = null;
            try {
                message = objectMapper.writeValueAsString(whoAndWhat);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            if(client!=null && client.isOpen()){
                client.send(message);
            }
            messageField.clear();
        });


        gridPane.add(vBox,2,7);
        GridPane.setRowSpan(vBox,2);
        GridPane.setColumnSpan(vBox, 5);
//        gridPane.setHgap(40);  // 控件之间的水平间距
        gridPane.setVgap(40);  // 控件之间的垂直间距
//        gridPane.setAlignment(Pos.CENTER);  // GridPane 对齐方式
        Scene scene = new Scene(gridPane, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.show();

        // 初始化websocket client
        try{
            // 使用匿名内部类来实例化抽象类对象client
            client = new WebSocketClient(new URI("ws://localhost:8080/chat")) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    // 把用户名传过去备份，同时服务器进行广播，本地同步更新guestsArea
                    LoginPage.user.setIsFirst(true);
                    ObjectMapper objectMapper = new ObjectMapper();
                    String message = null;
                    try {
                         message = objectMapper.writeValueAsString(LoginPage.user);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    send(message);
                    // Platform.runLater() 会将代码放入主线程的事件队列，当主线程空闲时，就会执行这些代码
                    Platform.runLater(() -> {
                        messageArea.appendText("Connected to server\n");
                    });
                    LoginPage.user.setIsFirst(false);
                }
                @Override
                public void onMessage(String message) {
                    log.info("received message.");
                    ObjectMapper objectMapper = new ObjectMapper();
                    User whoAndWhat = null;
                    try {
                        whoAndWhat = objectMapper.readValue(message, User.class);
//                        log.info(String.format("%s: %s",whoAndWhat.getUserName(),whoAndWhat.getText()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                    if(whoAndWhat.getIsFirst())
                    {
                        User finalWhoAndWhat1 = whoAndWhat;
                        Platform.runLater(() -> {
                            guestsArea.clear();
                            guestsArea.appendText(finalWhoAndWhat1.getUserName());
                        });
                    }
                    else{
                        User finalWhoAndWhat = whoAndWhat;
                        Platform.runLater(() -> {
                            messageArea.appendText(String.format("%s: ", finalWhoAndWhat.getUserName()) + finalWhoAndWhat.getText() + "\n");
                        });
                    }

                }

                @Override
                public void onClose(int i, String s, boolean b) {
                    Platform.runLater(() -> {
                        messageArea.appendText("Connection closed\n");
                    });
                }

                @Override
                public void onError(Exception e) {
                    Platform.runLater(() -> {
                        messageArea.appendText("Error: " + e.getMessage() + "\n");
                    });
                }
            };
            client.connect();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}

