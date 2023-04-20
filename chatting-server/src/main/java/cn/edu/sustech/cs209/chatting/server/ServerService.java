package cn.edu.sustech.cs209.chatting.server;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ServerService implements Runnable {
    Socket mysocket;
    Scanner in;
    PrintWriter out;
    String username;

    public ServerService(Socket s, String name) throws IOException {
        mysocket = s;
        username = name;
        in = new Scanner(s.getInputStream());
        out = new PrintWriter(s.getOutputStream());
    }

    @Override
    public void run() {
        while (true) {
            if (in.hasNext()) {
                String command = in.next();
                if (command.equals("Exit"))
                    return;

                executeCommand(command);
            }
        }
    }

    public void executeCommand(String command) {
        switch (command) {
            case "UserList":
                List<String>userlist = new ArrayList<>();
                for(String n: Server.usernames){
                    if(!n.equals(username)){
                        userlist.add(n);
                    }
                }
                String[] rl = new String[userlist.size()];
                userlist.toArray(rl);
                out.println(Server.serialize(rl));
                out.flush();
                break;
            case "SendMessage":

        }

    }


}
