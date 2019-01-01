package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class Stat extends Command {

    // Fields
    private String userName;

    // Public Constructor
    public Stat(String userName) {
        this.op_code = 8;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}
