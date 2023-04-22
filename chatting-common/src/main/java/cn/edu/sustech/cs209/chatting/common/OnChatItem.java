package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OnChatItem implements Serializable {

    public Integer id;
    public String groupName;
    public List<String> chatPeople;
    public List<Message> chatMessage;

    public OnChatItem(List<String> chatPeople, String me, String groupName) {
        this.groupName = groupName;
        this.chatPeople = chatPeople;
        chatPeople.add(me);
        this.chatMessage = new ArrayList<>();

    }

    public OnChatItem(String person, String me){
        groupName = null;
        this.chatPeople = new ArrayList<>();
        chatPeople.add(me);
        chatPeople.add(person);
        this.chatMessage = new ArrayList<>();
    }

    public void setId(Integer i){
        id = i;
    }

    public void updateMessage(Message m){
        chatMessage.add(m);
    }

}
