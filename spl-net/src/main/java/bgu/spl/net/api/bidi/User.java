package bgu.spl.net.api.bidi;

import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

public class User {

    //------Private Fields------
    private String userName;
    private String password;
    private boolean loggedIn;
    private int connectionId;
    private HashSet<String> following = new HashSet<>();
    private HashSet<String> followers = new HashSet<>();
    private ConcurrentLinkedQueue<String[]> waitingPosts = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<String[]> waitingPM = new ConcurrentLinkedQueue<>();

    //------Public Constructor------
    public User(String userName, String password, boolean loggedIn, int connectionId) {
        this.userName = userName;
        this.password = password;
        this.loggedIn = loggedIn;
        this.connectionId = connectionId;
    }

    //------Public Methods------
    public String getUserName(){
        return this.userName;
    }

    public String getPassword(){
        return this.password;
    }

    public boolean isLoggedIn(){
        return this.loggedIn == true;
    }

    public int getConnectionId(){
        return this.connectionId;
    }

    public HashSet<String> getFollowing(){
        return this.following;
    }

    public HashSet<String> getFollowers(){
        return this.followers;
    }

    public ConcurrentLinkedQueue<String[]> getWaitingPosts(){
        return this.waitingPosts;
    }

    public ConcurrentLinkedQueue<String[]> getWaitingPM(){
        return this.waitingPM;
    }

    public void logIn(){
        this.loggedIn = true;
    }

    public void logOut(){
        this.loggedIn = false;
    }

    public void addWaitingPost(String post, String sender){
        String[] waiting = new String[2];
        waiting[0] = post;
        waiting[1] = sender;
        this.waitingPosts.add(waiting); }

    public void addWaitingPM(String pm, String sender){
        String[] waiting = new String[2];
        waiting[0] = pm;
        waiting[1] = sender;
        this.waitingPM.add(waiting);
    }

    public void updateConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }





}
