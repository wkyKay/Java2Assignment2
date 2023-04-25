package cn.edu.sustech.cs209.chatting.client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sun.awt.windows.ThemeReader;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client extends Application {
    Socket mysocket;
    Scanner in;
    PrintWriter out;
    String data = null;
    boolean newData = true;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));

        mysocket = new Socket("localhost", 8888);
        in = new Scanner(mysocket.getInputStream());
        out = new PrintWriter(mysocket.getOutputStream());
        stage.setScene(new Scene(fxmlLoader.load()));
        Controller controller = fxmlLoader.getController();

        Thread t = new Thread(() -> {
            while (true) {
                if (in.hasNext()) {
                    String input = in.next();
                    if (input.split("#")[0].equals("NewMessage")) {
                        try {
                            controller.receiveMessage(input);
                        } catch (IOException | ClassNotFoundException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }else if(input.split("#")[0].equals("UpdateList")){
                        controller.updateList(input);

                    }
                    else {
                        setData(input);
                    }
                }
            }
        });
        t.start();
        controller.initialize(this);
        stage.setTitle("Chatting Client");
        stage.show();
        stage.setOnCloseRequest(event -> {
            //执行您要在关闭窗口时执行的代码
            out.println("Exit");
            out.flush();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            out.println(controller.username);
            out.flush();
            System.out.println(controller.username + " exits.");
        });
    }

    public void sendMessage(String s) {
        out.println(s);
        out.flush();
    }

    public synchronized void setData(String d){
        data = d;
        newData = true;
    }


}