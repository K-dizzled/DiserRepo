package com.diserauto.diserappbeta10.models;

import java.util.Comparator;

public class ModelChatlist {
    String id, lastMessage, sender, timestamp, name, email;
    Boolean isSeen;

    public ModelChatlist() {
    }


    public ModelChatlist(String id, String lastMessage, String sender, String timestamp, String name, String email, Boolean isSeen) {
        this.id = id;
        this.lastMessage = lastMessage;
        this.sender = sender;
        this.timestamp = timestamp;
        this.name = name;
        this.email = email;
        this.isSeen = isSeen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getSeen() {
        return isSeen;
    }

    public void setSeen(Boolean seen) {
        isSeen = seen;
    }

    public static Comparator<ModelChatlist> ByDate = new Comparator<ModelChatlist>(){
        @Override
        public int compare(ModelChatlist o1, ModelChatlist o2) {
            //compare by descending
            return - Long.compare(Long.parseLong(o1.timestamp),
                    Long.parseLong(o2.timestamp));
        }
    };
}
