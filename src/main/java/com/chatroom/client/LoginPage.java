package com.chatroom.client;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;
//@Component
@Data
class User {
    String userName;
    String text;
//    String image;
    Integer isFirst;
}

//@Service
public class LoginPage extends Application {
//    @Autowired
    private final UserMapper userMapper = new UserMapper();
    public static User user;
    @Override
    public void start(Stage primaryStage) throws Exception {
        user = new User();
        // 创建主页按钮
        Button btnGoToChatRoom = new Button("登录");
        Button register = new Button("注册");
        Label userName = new Label("账号: ");
        TextField usernameField = new TextField();
        Label password = new Label("密码: ");
        PasswordField passwordField = new PasswordField();

        HBox btn = new HBox(20);
        btn.getChildren().addAll(btnGoToChatRoom, register);
        btn.setAlignment(Pos.CENTER);

        HBox name = new HBox(20);
        name.getChildren().addAll(userName, usernameField);
        name.setAlignment(Pos.CENTER);

        HBox psw = new HBox(20);
        psw.getChildren().addAll(password, passwordField);
        psw.setAlignment(Pos.CENTER);

        VBox vBox = new VBox(20);
        vBox.getChildren().addAll(name, psw, btn);
        vBox.setAlignment(Pos.CENTER);

//        StackPane stackPane = new StackPane(vBox);

        Scene scene = new Scene(vBox, 300, 250);
        primaryStage.setResizable(false);
        primaryStage.setTitle("登录/注册");
        primaryStage.setScene(scene);
        primaryStage.show();

        // 点击按钮时，若登录成功，跳转聊天室
        btnGoToChatRoom.setOnAction(event -> {
            String userNameInput = usernameField.getText();
            String passwordInput = passwordField.getText();
            try {
                userMapper.getUser(userNameInput);
            } catch (Exception e) {
//                throw new RuntimeException(e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("警告");
                alert.setContentText("账号或密码错误");
                alert.showAndWait();
            }
            if (Objects.equals(user.getText(), passwordInput)) {
                WebSocketJavaFXClient chatRoomPage = new WebSocketJavaFXClient();
                try {
                    chatRoomPage.start(primaryStage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else{
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("警告");
                alert.setContentText("账号或密码错误");
                alert.showAndWait();
            }
        });

        register.setOnAction(event -> {
            RegistryPage registryPage = new RegistryPage();
            registryPage.start(primaryStage);
        });

    }

    public static void main(String[] args) {
        launch(args);
    }

}
