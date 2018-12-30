package bgu.spl.net.api.Commands;

import bgu.spl.net.api.bidi.Command;

import java.util.List;

public class Follow extends Command {

    // Fields
    private int op_code;
    private int follow;
    private int numFoUsers;
    private List<String> userNameList;

    // Public Constructor
    public Follow(int follow, int numFoUsers, List<String> userNameList) {
        this.op_code = 4;
        this.follow = follow;
        this.numFoUsers = numFoUsers;
        this.userNameList = userNameList;
    }

    public int getFollow() {
        return follow;
    }

    public int getNumFoUsers() {
        return numFoUsers;
    }

    public List<String> getUserNameList() {
        return userNameList;
    }
}
