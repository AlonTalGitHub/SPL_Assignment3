package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class Notification extends Command {

    //------Private Fields------
    private int rOpCode;
    private int kind;
    private String user;
    private String content;

    //------Public Constructor------
    public Notification(int rOpCode, int kind, String user, String content) {
        this.op_code = 9; //The opCode of NOTIFICATION
        this.rOpCode = rOpCode; //The opCode of the handled message
        this.rOpCode = rOpCode;
        this.kind = kind;
        this.user = user;
        this.content = content;
    }

    //------Public Methods------
    public int getrOpCode() { return rOpCode; }

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



