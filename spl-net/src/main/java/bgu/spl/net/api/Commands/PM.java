package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

public class PM extends Command {

    // Fields
    private String userName;
    private String content;

    // Public Constructor
    public PM(String userName, String content) {
        this.op_code = 6;
        this.userName = userName;
        this.content = content;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }
}
