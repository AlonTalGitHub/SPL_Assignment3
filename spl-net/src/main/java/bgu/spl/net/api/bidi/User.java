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
    private ConcurrentLinkedQueue<String> waitingPosts = new ConcurrentLinkedQueue<>(); //TODO: concurrentLinkedQueue?
    private ConcurrentLinkedQueue<String> waitingPM = new ConcurrentLinkedQueue<>();

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

    public ConcurrentLinkedQueue<String> getWaitingPosts(){
        return this.waitingPosts;
    }

    public ConcurrentLinkedQueue<String> getWaitingPM(){
        return this.waitingPM;
    }

    public void logIn(){
        this.loggedIn = true;
    }

    public void logOut(){
        this.loggedIn = false;
    }

    public void addWaitingPost(String post){ this.waitingPosts.add(post); }

    public void addWaitingPM(String pm){
        this.waitingPM.add(pm);
    }

    public void updateConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }





}
