package bgu.spl.net.api.Commands.ACKs;

import bgu.spl.net.api.Commands.ACK;

import java.util.List;

public class UserListACK extends ACK {

    //------Private Fields------
    private int numOfUsers;
    private List<String> userNameList;

    //------Public Constructors------
    public UserListACK(int numOfUsers, List<String> userNameList) {
        this.rOpCode = 7;
        this.numOfUsers = numOfUsers;
        this.userNameList = userNameList;
    }

    //------Public Methods------
    public int getNumOfUsers() { return this.numOfUsers; }

    public List<String> getUserNameList() { return this.userNameList;}
}
