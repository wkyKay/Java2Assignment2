package cn.edu.sustech.cs209.chatting.common;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {

    private Long timestamp;

    private String sentBy;

    private int sendTo;

    private String data;

    public Message(Long timestamp, String sentBy, int sentTo, String data) {
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

    public int getSendTo() {
        return sendTo;
    }

    public String getData() {
        return data;
    }
}
