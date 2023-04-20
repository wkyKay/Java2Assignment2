package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class Controller implements Initializable {

    @FXML
    ListView<Message> chatContentList;
    @FXML
    ListView<String> chatList;
    @FXML
    TextArea inputArea;
    Socket client;
    Scanner in;
    PrintWriter out;
    String username;
    List<String> onPrivateChat = new ArrayList<>();
    List<List<String>> onGroupChat = new ArrayList<>();

    Map<String,List<Message>> onPrivateChatMsg = new HashMap<>();
    Map<List<String>,List<Message>> onGroupChatMsg = new HashMap<>();
    Map<String, List<String>> groupNameUsers = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        InitializeConnection();
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Login");
        dialog.setHeaderText(null);
        dialog.setContentText("Username:");

        Optional<String> input = dialog.showAndWait();
        if (input.isPresent() && !input.get().isEmpty()) {
            /*
               TODO: Check if there is a user with the same name among the currently logged-in users,
                     if so, ask the user to change the username
             */
            username = input.get();
            out.println(username);
            out.flush();
            String result = null;
            while (true){
                if(in.hasNext()){
                    result = in.next();
                    if(result.equals("Occupied")){
                        System.out.println("Occupied username: " + username +", please try another");
                        Platform.exit();
                    }else {
                        System.out.println(username + " Connected to the Server.");
                    }
                    break;
                }
            }

        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }

        out.println(username);
        out.flush();
        chatContentList.setCellFactory(new MessageCellFactory());
    }
    public void InitializeConnection()  {
        Socket s = null;
        try {
            s = new Socket("localhost",8888);
            in = new Scanner(s.getInputStream());
            out = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Scanner getIn(){
        return in;
    }
    public PrintWriter getOut(){
        return out;
    }

    @FXML
    public void createPrivateChat() throws IOException, ClassNotFoundException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        out.println("UserList");
        out.flush();
        String[] userlist;
        while (true){
            if(in.hasNext()){
                userlist = (String[]) deserialize(in.next());
                break;
            }
        }
        userSel.getItems().addAll(userlist);

        Button okBtn = new Button("OK");
        okBtn.setOnAction(e -> {
            user.set(userSel.getSelectionModel().getSelectedItem());
            stage.close();
        });

        HBox box = new HBox(30);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        // TODO: if the current user already chatted with the selected user, just open the chat with that user
        // TODO: otherwise, create a new chat item in the left panel, the title should be the selected user's name
        String selected = String.valueOf(user);
        if(onPrivateChat.contains(selected)){
            chatContentList.getItems().clear();
            chatContentList.getItems().addAll(onPrivateChatMsg.get(selected));
        }else {
            onPrivateChat.add(selected);
            chatContentList.getItems().clear();
            onPrivateChatMsg.put(selected, new ArrayList<>());
            chatList.getItems().addAll(selected);
        }
    }

    /**
     * A new dialog should contain a multi-select list, showing all user's name.
     * You can select several users that will be joined in the group chat, including yourself.
     * <p>
     * The naming rule for group chats is similar to WeChat:
     * If there are > 3 users: display the first three usernames, sorted in lexicographic order, then use ellipsis with the number of users, for example:
     * UserA, UserB, UserC... (10)
     * If there are <= 3 users: do not display the ellipsis, for example:
     * UserA, UserB (2)
     */
    @FXML
    public void createGroupChat() throws IOException, ClassNotFoundException {
        List<String> users = new ArrayList<>();
        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        out.println("UserList");
        out.flush();
        String[] userlist;
        while (true){
            if(in.hasNext()){
                userlist = (String[]) deserialize(in.next());
                break;
            }
        }
        userSel.getItems().addAll(userlist);
        Button okBtn = new Button("OK");
        Button finishBtn = new Button("finish");
        okBtn.setOnAction(e -> {
            users.add(userSel.getSelectionModel().getSelectedItem());

        });
        finishBtn.setOnAction(e -> {
            stage.close();
        });

        HBox box = new HBox(30);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.getChildren().addAll(userSel, okBtn, finishBtn);
        stage.setScene(new Scene(box));
        stage.showAndWait();

        if(onGroupChat.contains(users)){
            chatContentList.getItems().clear();
            chatContentList.getItems().addAll(onGroupChatMsg.get(users));
        }else {
            onGroupChat.add(users);
            chatContentList.getItems().clear();
            onGroupChatMsg.put(users, new ArrayList<>());
            StringBuilder groupName = new StringBuilder();
            for(int i = 0; i < 3 && i < users.size(); i++){
                groupName.append(users.get(i)).append(",");
            }
            groupName.append("...(").append(users.size()+1).append(")");
            chatList.getItems().addAll(String.valueOf(groupName));
            groupNameUsers.put(String.valueOf(groupName), users);
        }
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() {
        // TODO
        String data = inputArea.toString();
        String selectedItem = chatList.getSelectionModel().getSelectedItem();
        List<String>sendto = new ArrayList<>();

        if(onPrivateChat.contains(selectedItem)){
            sendto.add(selectedItem);
        }else if(onGroupChat.contains(groupNameUsers.get(selectedItem))){
            sendto = groupNameUsers.get(selectedItem);
        }

        Message message = new Message(0L, username, sendto, data);
        out.println("SendMessage");
        out.flush();
        out.println(serialize(message));
        out.flush();
    }

    /**
     * You may change the cell factory if you changed the design of {@code Message} model.
     * Hint: you may also define a cell factory for the chats displayed in the left panel, or simply override the toString method.
     */
    private class MessageCellFactory implements Callback<ListView<Message>, ListCell<Message>> {
        @Override
        public ListCell<Message> call(ListView<Message> param) {
            return new ListCell<Message>() {

                @Override
                public void updateItem(Message msg, boolean empty) {
                    super.updateItem(msg, empty);
                    if (empty || Objects.isNull(msg)) {
                        setText(null);
                        setGraphic(null);
                        return;
                    }

                    HBox wrapper = new HBox();
                    Label nameLabel = new Label(msg.getSentBy());
                    Label msgLabel = new Label(msg.getData());

                    nameLabel.setPrefSize(50, 20);
                    nameLabel.setWrapText(true);
                    nameLabel.setStyle("-fx-border-color: black; -fx-border-width: 1px;");

                    if (username.equals(msg.getSentBy())) {
                        wrapper.setAlignment(Pos.TOP_RIGHT);
                        wrapper.getChildren().addAll(msgLabel, nameLabel);
                        msgLabel.setPadding(new Insets(0, 20, 0, 0));
                    } else {
                        wrapper.setAlignment(Pos.TOP_LEFT);
                        wrapper.getChildren().addAll(nameLabel, msgLabel);
                        msgLabel.setPadding(new Insets(0, 0, 0, 20));
                    }

                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                    setGraphic(wrapper);
                }
            };
        }
    }

    public String serialize(Serializable o)  {
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

    public Object deserialize(String s)
            throws IOException, ClassNotFoundException {

        byte[] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(data));
        Object o = ois.readObject();
        ois.close();
        return o;
    }

    public String GetMessage(String data){
        return data;
    }

    public void ExecuteCommand(String command){

    }
}
