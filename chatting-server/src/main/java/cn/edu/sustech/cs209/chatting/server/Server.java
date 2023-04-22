package cn.edu.sustech.cs209.chatting.server;

import cn.edu.sustech.cs209.chatting.common.OnChatItem;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    static List<String> usernames = new ArrayList<>();
    static Map<String, Socket> users = new HashMap<>();
    static Map<Integer, OnChatItem> groups = new HashMap<>();
    static int count = 0;


    public static void main(String[] args) throws IOException {
        System.out.println("Starting server");
        ServerSocket serverSocket = new ServerSocket(8888);
        System.out.println("Waiting for clients to connect");

        while (true){
            Socket user = serverSocket.accept();
            Scanner in = new Scanner(user.getInputStream());
            PrintWriter out = new PrintWriter(user.getOutputStream());
            String name;
            while (true){
                if(in.hasNext()){
                    name = in.next();
                    break;
                }
            }

            if (Server.usernames.contains(name)) {
                out.println("Occupied");
                out.flush();
                System.out.println("Client fail: " + name);
            } else {
                out.println("Connected");
                out.flush();
                Server.addUser(user, name);
                System.out.println("Client connected: " + name);
            }

            ServerService service = new ServerService(user, name);
            Thread t = new Thread(service);
            t.start();
        }
    }
    public synchronized static void addUser(Socket user, String name){
        users.put(name,user);
        usernames.add(name);
        notifyUsers();
    }

    public synchronized static void removeUser(String name){
        users.remove(name);
        usernames.remove(name);
        notifyUsers();
    }

    public static void notifyUsers()  {
        for(String n: usernames){
            List<String> returnList = new ArrayList<>();
            for(String m:usernames){
                if(m.equals(n)){
                    continue;
                }
                returnList.add(m);
            }
            String[]rl = new String[returnList.size()];
            returnList.toArray(rl);
            PrintWriter out = null;
            try {
                out = new PrintWriter(users.get(n).getOutputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            out.println("UpdateList");
            out.println(serialize(rl));
        }
    }

    public static String serialize(Serializable o)  {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    public static Object deserialize(String s)
            throws IOException, ClassNotFoundException {

        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }
}
