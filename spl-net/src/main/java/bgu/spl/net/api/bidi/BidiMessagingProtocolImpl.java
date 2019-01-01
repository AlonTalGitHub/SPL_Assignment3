package bgu.spl.net.api.bidi;

import bgu.spl.net.api.Commands.*;
import bgu.spl.net.api.Commands.Command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

public class BidiMessagingProtocolImpl<T> implements BidiMessagingProtocol<T> {

    //------Private Fields------
    private int connectionId;
    private Connections<T> connections;
    private ConcurrentHashMap<String, User> registeredClients; //Mapped by their usernames TODO: concurrent?
    private LinkedList<User> registeredClientsByOrder; //In order to know the order the user registered
    /*
    All posts and PMs are saved into a data structure.
    The data structure is mapped by the kind: either post or PM.
    Each kind is mapped by the user names.
    Each user name contains a list of all his/her posts or PMs.
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<String, List<String>>> postsAndPMs;
    private final Object registerLock;
    private boolean shouldTerminate;


    //------Public Constructors------
    public BidiMessagingProtocolImpl(ConcurrentHashMap<String, User> registeredClients, LinkedList<User> registeredClientsByOrder) {
        this.registeredClients = registeredClients;
        this.registeredClientsByOrder = registeredClientsByOrder;
        this.postsAndPMs = new ConcurrentHashMap<>();
        postsAndPMs.put("posts", new ConcurrentHashMap<>());
        postsAndPMs.put("PMs", new ConcurrentHashMap<>());
        this.registerLock = new Object();
        this.shouldTerminate = false;
    }

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    //------Public Methods------
    @Override
    public void process(T message) {

        //TODO: synchronization ONLY IF NEEDED

/*
        int opCode = ((Command) message).getOpCode(); //TODO: Possibility: doing the command as an interface with getOpCode function

        switch (opCode) {
            case 1:
                registerProcess((Register) message);
            break;
            case 2:
                loginProcess((Login) message);
                break;
            case 3:
                logoutProcess();
                break;
            case 4:
                followUnfollowProcess((Follow) message);
                break;
            case 5:
                postProcess((Post) message);
                break;
            case 6:
                pmProcess((PM) message);
                break;
            case 7:
                userListProcess();
                break;
            case 8:
                statProcess((Stat) message);
                break;
        }

*/

        if (message instanceof Register) {
            registerProcess((Register) message);
        } else if (message instanceof Login) {
            loginProcess((Login) message);
        } else if (message instanceof Logout) {
            logoutProcess();
        } else if (message instanceof Follow) {
            followUnfollowProcess((Follow) message);
        } else if (message instanceof Post) {
            postProcess((Post) message);
        } else if (message instanceof PM) {
            pmProcess((PM) message);
        } else if (message instanceof UserList) {
            userListProcess();
        } else if (message instanceof Stat) {
            statProcess((Stat) message);
        }

    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    //------Private Methods------
    private void registerProcess(Register message) {

        int opCode = 1;
        String userName = message.getUserName();
        String password = message.getPassWord();

        synchronized (registerLock) { //Avoiding 2 clients registering with the same username

            if (registeredClients.containsKey(userName)) { //Failing To Register
                sendError(opCode);
            } else { //Successfully Registered
                User user = new User(userName, password, false, connectionId); //The connection id changes in every connection
                registeredClients.put(userName, user);
                registeredClientsByOrder.add(user);
                sendACK(opCode);
            }

        }
    }

    private void loginProcess(Login message) {

        int opCode = 2;
        String userName = message.getUserName();
        String password = message.getPassWord();

        if (registeredClients.containsKey(userName)) {

            //if the password doesn't match or the user is already logged in
            User user = registeredClients.get(userName);
            User clientUser = iteration(connectionId);
            if (user.getPassword().compareTo(password) != 0 || user.isLoggedIn() || clientUser.isLoggedIn()) { //Either: the password doesn't match\the username is already logged in with another or the same client\the client is logged in with another or the same user
                sendError(opCode);
            } else {
                //Updating the client status to be logged in and updating the new connectionId
                registeredClients.get(userName).updateConnectionId(connectionId);
                registeredClients.get(userName).logIn();
                sendACK(opCode);

                //Sending all the waiting posts and messages to the user
                while (!registeredClients.get(userName).getWaitingPosts().isEmpty()) {
                    String waitingPost = registeredClients.get(userName).getWaitingPosts().poll();
                    sendACK(opCode, waitingPost);
                }
                while (!registeredClients.get(userName).getWaitingPM().isEmpty()) {
                    String waitingPM = registeredClients.get(userName).getWaitingPM().poll();
                    sendACK(opCode, waitingPM);
                }

            }
        }
    }

    private void logoutProcess() {

        int opCode = 3;
        boolean signed = false;
        Iterator iter = registeredClients.values().iterator();
        while (iter.hasNext()) { //Iterates through the users, checking if there is at least one which is logged in
            User user = (User) iter.next();
            if (user.getConnectionId() == connectionId) { //If one of the current client's users is logged in
                if (user.isLoggedIn()) {
                    signed = true;
                }
            }
        }

        if (!signed) {
            sendError(opCode);
        } else {
            User user = iteration(connectionId); //Finding who is the client that sent a command
            sendACK(opCode);
            registeredClients.get(user.getUserName()).logOut();
            shouldTerminate = true;
        }
    }

    private void followUnfollowProcess(Follow message) {

        int opCode = 4;
        List<String> unOrFollowing = message.getUserNameList();
        int numOfFollowing = message.getNumFoUsers();
        int followOrUn = message.getFollow();
        List<String> successfulUnOrFol = new LinkedList<>();

        User follower = iteration(connectionId); //Finding who is the client that sent a command
        if (!follower.isLoggedIn()) { //User is not logged in
            sendError(opCode);
        } else {

            if (followOrUn == 0) { //FOLLOW
                for (String toUnOrFol : unOrFollowing) {
                    if (!follower.getFollowing().contains(toUnOrFol)) {
                        follower.getFollowing().add(toUnOrFol);

                        //Here we add the follower that started to follow x to x's followers list
                        registeredClients.get(toUnOrFol).getFollowers().add(follower.getUserName());
                        successfulUnOrFol.add(toUnOrFol);
                    }
                }
            } else if (followOrUn == 1) { //UNFOLLOW

                for (String toUnOrFol : unOrFollowing) {
                    if (follower.getFollowing().contains(toUnOrFol)) {
                        follower.getFollowing().remove(toUnOrFol);

                        //Here we remove the user that decided to unfollow x from x's followers list
                        registeredClients.get(toUnOrFol).getFollowers().remove(follower.getUserName());
                        successfulUnOrFol.add(toUnOrFol);
                    }
                }
            }

            if (successfulUnOrFol.isEmpty()) { //No successful (un)follows
                sendError(opCode);
            } else { //At least one successful (un)follow
                String successes = "" + successfulUnOrFol.size();
                for (String successful : successfulUnOrFol) {
                    successes += " " + successful;
                }
                sendACK(opCode, successes);
            }
        }

    }

    private void postProcess(Post message) {

        int opCode = 5;
        List<String> tagged = message.getTaggedUsersList();
        String content = message.getContent(); //TODO: making sure the content is not separated from the tagged list.

        User poster = iteration(connectionId); //Finding who is the client that sent a command
        if (!poster.isLoggedIn()) {
            sendError(opCode);
        } else {

            //Saving the posts in a data structure
            if (!postsAndPMs.get("posts").containsKey(poster.getUserName())) {
                postsAndPMs.get("posts").put(poster.getUserName(), new LinkedList<>());
            }
            postsAndPMs.get("posts").get(poster.getUserName()).add(content);

            //Sending the post to all of the followers
            Iterator iterFol = poster.getFollowers().iterator();
            while (iterFol.hasNext()) {
                String follower = (String) iterFol.next();
                User followerUser = registeredClients.get(follower);
                if (!tagged.contains(follower)) { //Only if the follower is not tagged in the post. If he/she is tagged they will receive the post anyway.
                    if (followerUser.isLoggedIn()) {
                        sendNotification(followerUser.getConnectionId(), opCode, "Public", poster.getUserName(), content);
                    } else {
                        registeredClients.get(follower).addWaitingPost(content);
                    }
                }
            }

            //Sending the post to all of the tagged users
            for (String taggedUser : tagged) {
                taggedUser = taggedUser.substring(1); //Removing the @ so we can find the user
                User user = registeredClients.get(taggedUser);
                if (user.isLoggedIn()) {
                    sendNotification(user.getConnectionId(), opCode, "Public", poster.getUserName(), content);
                } else {
                    registeredClients.get(taggedUser).addWaitingPost(content);
                }
            }
        }
    }

    private void pmProcess(PM message) {

        int opCode = 6;
        String recipient = message.getUserName();
        String content = message.getContent();

        User sender = iteration(connectionId); //Finding who is the client that sent a command
        if (!sender.isLoggedIn() || !registeredClients.containsKey(recipient)) { //Either the sender is not logged in or the recipient is not registered
            sendError(opCode);
        } else {

            //Saving the PMs in a data structure
            if (!postsAndPMs.get("PMs").containsKey(sender.getUserName())) {
                postsAndPMs.get("PMs").put(sender.getUserName(), new LinkedList<>());
            }
            postsAndPMs.get("PMs").get(sender.getUserName()).add(content);

            User recipientUser = registeredClients.get(recipient);
            if (recipientUser.isLoggedIn()) {
                sendNotification(recipientUser.getConnectionId(), opCode, "PM", sender.getUserName(), content);
            } else {
                registeredClients.get(recipient).addWaitingPM(content);
            }
        }
    }

    private void userListProcess() {

        int opCode = 7;
        User requester = iteration(connectionId); //Finding who is the client that sent a command
        if (!requester.isLoggedIn()) {
            sendError(opCode);
        } else {
            String userList = "";
            userList += registeredClientsByOrder.size();
            for (int i = registeredClientsByOrder.size() - 1; i >= 0; i--) { //The registered clients from the first to the last because linked list is LIFO
                User user = registeredClientsByOrder.get(i);
                userList += " " + user.getUserName();
            }

            sendACK(opCode, userList);
        }

    }

    private void statProcess(Stat message) {

        int opCode = 8;
        String userName = message.getUserName();

        User requester = iteration(connectionId); //Finding who is the client that sent a command
        if (!requester.isLoggedIn() || !registeredClients.containsKey(userName)) { //Either the requester is not logged in or the requested is not registered
            sendError(opCode);
        } else {
            User user = registeredClients.get(userName);
            String stat = "";
            int numOfPosts = postsAndPMs.get("posts").get(userName).size(); //How many posts the requested user posted
            int numOfFollowers = user.getFollowers().size();
            int numOfFollowing = user.getFollowing().size();
            stat += numOfPosts + " " + numOfFollowers + " " + numOfFollowing;
            sendACK(opCode, stat);
        }
    }


    /**
     * Sending an ERROR with the corresponding opcode
     *
     * @param opCode the opCode of the current ERROR
     */
    private void sendError(int opCode) {
        Error error = new Error(opCode);
        T errorMessage = (T) error.createMessage();
        connections.send(connectionId, errorMessage);
    }

    /**
     * Sending an ACK with the corresponding opcode
     *
     * @param opCode the opCode of the current ACK
     */
    private void sendACK(int opCode) {
        ACK ack = new ACK(opCode);
        T ackMessage = (T) ack.createMessage();
        connections.send(connectionId, ackMessage);
    }

    /**
     * Sending an ACK with the corresponding opcode, and optional message
     *
     * @param opCode   the opCode of the current ACK
     * @param optional an optional content added to the ACK
     */
    private void sendACK(int opCode, String optional) {
        ACK ack = new ACK(opCode, optional);
        T ackMessage = (T) ack.createMessage();
        connections.send(connectionId, ackMessage);
    }

    /**
     * Sending a NOTIFICATION with the corresponding opcode, and either it is private or public, who sent the post/message, and the content
     *
     * @param recipientConnectionId to whom sending the NOTIFICATION
     * @param opCode                the opCode of the current NOTIFICATION
     * @param kind                  is it a post or a PM
     * @param user                  who sent the post/PM
     * @param content               the content of the post/PM
     */

    private void sendNotification(int recipientConnectionId, int opCode, String kind, String user, String content) {
        Notification notification = new Notification(opCode, kind, user, content);
        T notificationMessage = (T) notification.createMessage();
        connections.send(recipientConnectionId, notificationMessage);
    }

    /**
     * Finding the @toFind in the registered users list
     *
     * @param toFind the user by connection that needs to be found
     * @return the user that we found or null if he/she wasn't found
     */
    private User iteration(Integer toFind) {

        //TODO: making sure each client receives a new connectionID
        boolean found = false;
        Iterator iterThroughRegistered = registeredClients.values().iterator();
        User user = (User) iterThroughRegistered.next();
        if (user.getConnectionId() == toFind) {
            found = true;
        }
        while (iterThroughRegistered.hasNext() && !found) {
            user = (User) iterThroughRegistered.next();
            if (user.getConnectionId() == toFind) {
                found = true;
            }
        }

        if (!found) {
            return null;
        }

        return user;
    }

}
