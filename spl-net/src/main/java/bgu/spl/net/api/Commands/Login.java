package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class Login extends Command {

    // Fields
    private String userName;
    private String passWord;

    // Public Constructor
    public Login(String userName, String passWord) {
        this.op_code = 2;
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
