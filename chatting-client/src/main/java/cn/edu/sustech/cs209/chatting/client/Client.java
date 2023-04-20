package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client extends Application {
    Scanner in;
    PrintWriter out;
    Controller controller;
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        stage.setScene(new Scene(fxmlLoader.load()));
        stage.setTitle("Chatting Client");
        stage.show();
        controller = fxmlLoader.getController();
        in = controller.getIn();
        out = controller.getOut();
        while (true) {
            if (in.hasNext()) {
                String data = in.next();
                if (data.equals("Exit"))
                    return;
                controller.GetMessage(data);
            }
        }
    }
}
