package bgu.spl.net.api.bidi.Responses.ACKs;

import bgu.spl.net.api.bidi.Responses.ACK;

public class StatACK extends ACK {

    //------Private Fields------
    private int numOfPosts;
    private int numOfFollowers;
    private int numOfFollowing;

    //------Public Constructors------
    public StatACK(int numOfPosts, int numOfFollowers, int numOfFollowing) {
        this.rOpCode = 8;
        this.numOfPosts = numOfPosts;
        this.numOfFollowers = numOfFollowers;
        this.numOfFollowing = numOfFollowing;
    }

    //------Public Methods------
    public int getNumOfPosts() { return this.numOfPosts; }

    public int getNumOfFollowers() { return this.numOfFollowers; }

    public int getNumOfFollowing() { return this.numOfFollowing; }


}
