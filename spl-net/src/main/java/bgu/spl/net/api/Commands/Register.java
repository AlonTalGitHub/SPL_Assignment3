package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class Register extends Command {

    // Fields
    private int op_code;
    private String userName;
    private String passWord;

    // Public Constructor
    public Register(String userName, String passWord) {
        this.op_code = 1;
        this.userName = userName;
        this.passWord = passWord;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassWord() {
        return passWord;
    }
}
