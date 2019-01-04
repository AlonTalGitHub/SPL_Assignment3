package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class Notification extends Command {

    //------Private Fields------
    private int kind;
    private String user;
    private String content;

    //------Public Constructor------
    public Notification(int kind, String user, String content) {
        this.op_code = 9; // The opCode of NOTIFICATION
        this.kind = kind; // The kind of the handled message
        this.user = user;
        this.content = content;
    }

    //------Public Methods------
    public int getKind(){
        return kind;
    }

    public String getUser(){
        return user;
    }

    public String getContent(){
        return content;
    }
}



