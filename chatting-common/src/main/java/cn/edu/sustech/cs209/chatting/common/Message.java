package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {

    private Long timestamp;

    private String sentBy;

    private List<String> sendTo;

    private String data;

    public Message(Long timestamp, String sentBy, List<String> sendTo, String data) {
        this.timestamp = timestamp;
        this.sentBy = sentBy;
        this.sendTo = sendTo;
        this.data = data;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getSentBy() {
        return sentBy;
    }

    public List<String> getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }
}
