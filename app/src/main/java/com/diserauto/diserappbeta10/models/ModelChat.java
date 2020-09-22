package com.diserauto.diserappbeta10.models;

public class ModelChat {
    String message, receiver, sender, timestamp;
    boolean isSeen;
    //Integer index;

    public ModelChat() {

    }

    public ModelChat(String message, String receiver, String sender, String timestamp, boolean isSeen) {
        this.message = message;
        this.receiver = receiver;
        this.sender = sender;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        //this.index = index;
    }
/*
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }
    */

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean getIsSeen() {
        return isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }
}
