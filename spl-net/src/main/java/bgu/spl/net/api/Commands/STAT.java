package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class STAT extends Command {

    // Fields
    private String userName;

    // Public Constructor
    public STAT(String userName) {
        this.op_code = 8;
        this.userName = userName;
    }
}
