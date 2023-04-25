package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OnChatItem;

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
                try {
                    executeCommand(command);
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void executeCommand(String command) throws IOException, ClassNotFoundException, InterruptedException {
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

            case "NewChat":{
                OnChatItem newChat;
                while (true){
                    if(in.hasNext()){
                        newChat = (OnChatItem) Server.deserialize(in.next());
                        break;
                    }
                }
                int newId = Server.count;
                Server.count++;
                newChat.setId(newId);
                Server.groups.put(newId, newChat);

                out.println(newId);
                out.flush();
                break;
            }
            case "GetChat":{
                int chatId;
                while (true){
                    if(in.hasNext()){
                        chatId = Integer.parseInt(in.next());
                        break;
                    }
                }
                OnChatItem item = Server.groups.get(chatId);
                String data = Server.serialize(item);
                out.println(data);
                out.flush();
                break;
            }
            case "SendMessage":{
                System.out.println("execute SendMessage");
                int chatId;
                Message newMsg;
                while (true){
                    if(in.hasNext()){
                        chatId = Integer.parseInt(in.next());
                        break;
                    }
                }
                while (true){
                    if(in.hasNext()){
                        newMsg = (Message) Server.deserialize(in.next());
                        break;
                    }
                }
                //找到对应chatgroup
                OnChatItem item = Server.groups.get(chatId);
                item.chatMessage.add(newMsg);
                for(String user: item.chatPeople){
                    if (!user.equals(newMsg.getSentBy()) && Server.usernames.contains(user)){
                        Socket s = Server.users.get(user);
                        PrintWriter s_out = new PrintWriter(s.getOutputStream());
                        String Name;
                        if(item.groupName != null){
                            Name = item.groupName;
                        }else {
                            Name = newMsg.getSentBy();
                        }
                        s_out.println("NewMessage#" + Name + "#" + chatId );
                        s_out.flush();
                    }
                }

                break;
            }
            case "Exit":{
                String username;
                while (true){
                    if(in.hasNext()){
                        username = in.next();
                        break;
                    }
                }
                Server.removeUser(username);
            }
            default:
                break;


        }

    }


}
