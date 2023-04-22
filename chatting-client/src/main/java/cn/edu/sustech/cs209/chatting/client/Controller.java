package cn.edu.sustech.cs209.chatting.client;

import cn.edu.sustech.cs209.chatting.common.Message;
import cn.edu.sustech.cs209.chatting.common.OnChatItem;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Controller {
    public Label currentUsername;
    public Label currentOnlineCnt;
    Client client;
    @FXML
    ListView<Message> chatContentList;
    @FXML
    ListView<String> chatList;
    @FXML
    TextArea inputArea;
    String username;
    Map<String, Integer> nameToId = new HashMap<>();


    public void initialize(Client client) throws IOException {
        this.client = client;
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
            client.sendMessage(username);
            String result = getData();
            if (result.equals("Occupied")) {
                System.out.println("Occupied username: " + username + ", please try another");
                Platform.exit();
            } else {
                currentUsername.setText(username);
                System.out.println(username + " Connected to the Server.");
            }

        } else {
            System.out.println("Invalid username " + input + ", exiting");
            Platform.exit();
        }
        chatList.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {

            if (newValue != null) {
                Platform.runLater(() -> {
                    try {
                        openChatItem(newValue);
                    } catch (IOException | ClassNotFoundException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
        chatContentList.setCellFactory(new MessageCellFactory());
    }


    @FXML
    public void createPrivateChat() throws IOException, ClassNotFoundException, InterruptedException {
        AtomicReference<String> user = new AtomicReference<>();

        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        client.sendMessage("UserList");
        String[] userlist;

        userlist = (String[]) deserialize(getData());
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

        if (nameToId.get(selected) != null) {
            //清空聊天面板
            chatContentList.getItems().clear();
            //获取聊天记录
            client.sendMessage("GetChat");
            client.sendMessage(nameToId.get(selected).toString());
            OnChatItem current = (OnChatItem) deserialize(getData());
            chatContentList.getItems().addAll(current.chatMessage);
        } else {
            //创建聊天
            client.sendMessage("NewChat");
            client.sendMessage(serialize(new OnChatItem(selected, this.username)));
            //获取id并保存
            System.out.println("waiting id");
            Thread.sleep(100);
            int givenId = Integer.parseInt(getData());
            System.out.println(givenId);

            nameToId.put(selected, givenId);
            //清空聊天面板
            chatList.getItems().addAll(selected);
            chatContentList.getItems().clear();
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
    public void createGroupChat() throws IOException, ClassNotFoundException, InterruptedException {
        List<String> users = new ArrayList<>();
        Stage stage = new Stage();
        ComboBox<String> userSel = new ComboBox<>();

        // FIXME: get the user list from server, the current user's name should be filtered out
        client.sendMessage("UserList");
        String[] userlist;
        while (true) {
            if (client.data != null) {
                userlist = (String[]) deserialize(client.data);
                client.data = null;
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

        StringBuilder groupName = new StringBuilder();
        for (int i = 0; i < 3 && i < users.size(); i++) {
            groupName.append(users.get(i)).append(",");
        }
        groupName.append("...(").append(users.size() + 1).append(")");
        String selected = groupName.toString();

        if (nameToId.get(selected) != null) {
            //清空聊天面板
            chatContentList.getItems().clear();
            //获取聊天记录
            client.sendMessage("GetChat");
            client.sendMessage(nameToId.get(selected).toString());
            OnChatItem current = (OnChatItem) deserialize(getData());
            chatContentList.getItems().addAll(current.chatMessage);
        } else {
            //创建聊天
            client.sendMessage("NewChat");
            client.sendMessage(serialize(new OnChatItem(users, this.username, selected)));
            //获取id并保存
            System.out.println("waiting id");
            Thread.sleep(100);
            int givenId = Integer.parseInt(getData());
            System.out.println(givenId);

            nameToId.put(selected, givenId);
            //清空聊天面板
            chatList.getItems().addAll(selected);
            chatContentList.getItems().clear();
        }
    }

    /**
     * Sends the message to the <b>currently selected</b> chat.
     * <p>
     * Blank messages are not allowed.
     * After sending the message, you should clear the text input field.
     */
    @FXML
    public void doSendMessage() throws IOException, ClassNotFoundException, InterruptedException {
        // TODO
        String data = inputArea.getText();
        if (data.equals("")) {
            return;
        }
        String selectedItem = chatList.getSelectionModel().getSelectedItem();
        int chatId = nameToId.get(selectedItem);
        Message newMsg = new Message(0L, username, chatId, data);
        //清空聊天面板
        chatContentList.getItems().clear();
        //获取聊天记录
        client.sendMessage("GetChat");
        Thread.sleep(100);
        client.sendMessage(String.valueOf(chatId));
        Thread.sleep(100);
        OnChatItem current = (OnChatItem) deserialize(getData());
        chatContentList.getItems().addAll(current.chatMessage);
        //发送消息
        client.sendMessage("SendMessage");
        Thread.sleep(100);
        client.sendMessage(String.valueOf(chatId));

        Thread.sleep(100);
        client.sendMessage(serialize(newMsg));
        //更新聊天面板：添加我说的话
        chatContentList.getItems().addAll(newMsg);
        inputArea.setText("");

    }


    public void receiveMessage(String input) throws IOException, ClassNotFoundException, InterruptedException {

        String sendBy = input.split("#")[1];
        String id = input.split("#")[2];
//        Stage stage = new Stage();
//        HBox reminder = new HBox(30);
//        reminder.setAccessibleText("Message from " + sendBy);
//        Button okBtn = new Button("OK");
//        okBtn.setOnAction(e -> {
//            stage.close();
//        });
//
//        HBox box = new HBox(40);
//        box.setAlignment(Pos.TOP_CENTER);
//        box.setPadding(new Insets(20, 20, 20, 20));
//        box.getChildren().addAll(reminder,okBtn);
//        stage.setScene(new Scene(box));
//        stage.showAndWait();
        //未曾通信
        if (!nameToId.containsKey(sendBy)) {
            nameToId.put(sendBy, Integer.valueOf(id));
            chatList.getItems().addAll(sendBy);
        }
    }


    public String getData() {
        String result = null;
        while (true) {
            if (client.data != null) {
                result = client.data;
                client.data = null;
                break;
            }
        }
        return result;
    }

    public void openChatItem(String newValue) throws IOException, ClassNotFoundException, InterruptedException {

        int chatId;
        if (nameToId.get(newValue) != null) {
            chatId = nameToId.get(newValue);
            //清空聊天面板
            chatContentList.getItems().clear();
            //获取聊天记录
            client.sendMessage("GetChat");
            Thread.sleep(100);
            client.sendMessage(String.valueOf(chatId));
            Thread.sleep(100);
            OnChatItem current = (OnChatItem) deserialize(getData());
            if (current.chatMessage != null && current.chatMessage.size() != 0)
                chatContentList.getItems().addAll(current.chatMessage);
        }

    }

    @FXML
    public void getMessage() throws InterruptedException, IOException, ClassNotFoundException {
        String selectedItem = chatList.getSelectionModel().getSelectedItem();
        int chatId = nameToId.get(selectedItem);
        //清空聊天面板
        chatContentList.getItems().clear();
        //获取聊天记录
        client.sendMessage("GetChat");
        Thread.sleep(100);
        client.sendMessage(String.valueOf(chatId));
        Thread.sleep(100);
        OnChatItem current = (OnChatItem) deserialize(getData());
        chatContentList.getItems().addAll(current.chatMessage);
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

    public String serialize(Serializable o) {
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


}
