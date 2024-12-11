package com.chatroom.client;

import com.chatroom.WebSocketConfig;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

//@Service
@Slf4j
public class RegistryPage extends Application {

//    @Autowired
    private final UserMapper userMapper = new UserMapper();
    @Override
    public void start(Stage primaryStage){
        Label userName = new Label("昵称: ");
        Label password = new Label("密码: ");
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button register = new Button("注册");
        Button back = new Button("返回");

        HBox name = new HBox(20);
        name.getChildren().addAll(userName,usernameField);
        name.setAlignment(Pos.CENTER);
        HBox psw = new HBox(20);
        psw.setAlignment(Pos.CENTER);
        psw.getChildren().addAll(password, passwordField);

        HBox btn = new HBox(20);
        btn.getChildren().addAll(register, back);
        btn.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(20);
        vBox.getChildren().addAll(name,psw,btn);
        vBox.setAlignment(Pos.CENTER);

//        BorderPane borderPane = new BorderPane();
//        borderPane.setCenter(vBox);

        Scene scene = new Scene(vBox, 300,250);
        primaryStage.setResizable(false);
        primaryStage.setTitle("注册");
        primaryStage.setScene(scene);
        primaryStage.show();
        // 注册
        register.setOnAction(event -> {
            try {
                log.info(String.format("注册用户名: %s",usernameField.getText()));
                userMapper.register(usernameField.getText(),passwordField.getText());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            LoginPage.user.setUserName(usernameField.getText());
            LoginPage.user.setText(passwordField.getText());
            WebSocketJavaFXClient chatRoomPage = new WebSocketJavaFXClient();
            try {
                chatRoomPage.start(primaryStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        // 返回
        back.setOnAction(event -> {
            LoginPage loginPage = new LoginPage();
            try {
                loginPage.start(primaryStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

}
