package com.chatroom.client;

import org.apache.ibatis.annotations.Insert;
//import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.sql.*;

//@Mapper
public class UserMapper {
//    @Select("select * from users where userName=#{userName}")
    void getUser(String userName) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatroom", "root", "lh15987.");

        Statement statement = connection.createStatement();
        String sql = String.format("select * from users where userName='%s'", userName);
        ResultSet resultSet = statement.executeQuery(sql);
        resultSet.next();
        LoginPage.user.setUserName(resultSet.getString("userName")) ;
        LoginPage.user.setText(resultSet.getString("password")); ;
    }

//    @Insert("insert into users (userName, password) values()")
    void register(String userName, String password) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/chatroom", "root", "lh15987.");
        Statement statement = connection.createStatement();
        String sql = String.format("insert into users (userName, password) values ('%s', '%s')", userName, password);
        System.out.println(sql);
        statement.execute(sql);
        LoginPage.user.setUserName(userName);
        LoginPage.user.setText(password);
    }
}
