package bgu.spl.net.api.bidi;

import bgu.spl.net.api.Commands.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

public class BGSProtocol<T> implements BidiMessagingProtocol<T> {

    //------Private Fields------
    private int connectionId;
    private Connections<T> connections;
    private HashMap<String, User> registeredClients; //Mapped by their usernames


    //------Public Constructors------
    public BGSProtocol(HashMap<String, User> registeredClients) {
        this.registeredClients = registeredClients;
    }

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    //------Public Methods------
    @Override
    public void process(T message) {

        if (message instanceof Register) {
            registerProcess((Register) message);
        } else if (message instanceof Login) {
            loginProcess((Login) message);
        } else if (message instanceof Logout) {
            logoutProcess((Logout) message);
        } else if (message instanceof Follow) {
            followUnfollowProcess((Follow) message);
        } else if (message instanceof Post) {
            postProcess((Post) message);
        } else if (message instanceof PM) {
            pmProcess((PM) message);
        } else if (message instanceof UserList) {
            userListProcess((UserList) message);
        } else if (message instanceof Stats) {
            statsProcess((Stats) message);
        }

    }

    @Override
    public boolean shouldTerminate() {
        //TODO: finding what terminates the process
        User user = iteration(connectionId);
        return !registeredClients.get(user.getUserName()).isLoggedIn();
    }

    //------Private Methods------
    private void registerProcess(Register message) {

        int opCode = 1;
        String userName = message.getUserName();
        String password = message.getPassWord();

        if (registeredClients.containsKey(userName)) { //Failing To Register
            sendError(opCode); //TODO: T?
        } else { //Successfully Registered
            User user = new User(userName, password, false, connectionId); //The connection id changes in every connection
            registeredClients.put(userName, user);
            sendACK(opCode);
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
            if (user.getPassword().compareTo(password) != 0 || user.isLoggedIn() || clientUser.isLoggedIn()) { //Either: the password doesn't match\the username is already logged in with another\same client\the client is logged in with another\same user
                sendError(opCode);
            } else {
                //Updating the client status to be logged in and updating the new connectionId
                registeredClients.get(userName).updateConnectionId(connectionId);
                registeredClients.get(userName).logIn();
                sendACK(opCode);

                //Sending all the waiting posts and messages to the user
                for (int i = 0; i < user.getWaitingPosts().size(); i++) {
                    T waitingPost = (T) registeredClients.get(userName).getWaitingPosts().poll();
                    connections.send(connectionId, waitingPost);
                }
                for (int i = 0; i < user.getWaitingPM().size(); i++) {
                    T waitingPM = (T) registeredClients.get(userName).getWaitingPM().poll();
                    connections.send(connectionId, waitingPM);
                }

            }
        }
    }

    private void logoutProcess(Logout message) {

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
                for (int i = 0; i < numOfFollowing; i++) {
                    String toUnOrFol = unOrFollowing.get(i);
                    if (!follower.getFollowing().contains(toUnOrFol)) {
                        follower.getFollowing().add(unOrFollowing.get(i));

                        //Here we add the follower that started to follow x to x's follower list
                        registeredClients.get(toUnOrFol).getFollowers().add(follower.getUserName());
                        successfulUnOrFol.add(toUnOrFol);
                    }
                }
            } else if (followOrUn == 1) { //UNFOLLOW

                for (int i = 0; i < unOrFollowing.size(); i++) {
                    String toUnOrFol = unOrFollowing.get(i);
                    if (follower.getFollowing().contains(toUnOrFol)) {
                        follower.getFollowing().remove(unOrFollowing.get(i));

                        //Here we remove the user that decided to unfollow x from x's follower list
                        registeredClients.get(toUnOrFol).getFollowers().remove(follower.getUserName());
                        successfulUnOrFol.add(toUnOrFol);
                    }
                }
            }

            if (successfulUnOrFol.isEmpty()) { //No successful (un)follows
                sendError(opCode);
            } else { //At least one successful (un)follow
                String successful = "" + successfulUnOrFol.size();
                for (int i = 0; i < successfulUnOrFol.size(); i++) {
                    successful += " " + successfulUnOrFol.get(i);
                }
                sendACK(opCode, successful);
            }
        }

    }

    private void postProcess(Post message) {

        int opCode = 5;
        List<String> tagged = message.getTaggedUsersList();
        String content = message.getContent();

        User poster = iteration(connectionId); //Finding who is the client that sent a command
        if (!poster.isLoggedIn()) {
            sendError(opCode);
        } else {

            poster.addPersonalPost(content);

            //Sending the post to all of the followers
            Iterator iterFol = poster.getFollowers().iterator();
            while (iterFol.hasNext()) {
                String follower = (String) iterFol.next();
                User followerUser = registeredClients.get(follower);
                if (followerUser.isLoggedIn()) {
                    sendNotification(followerUser.getConnectionId(), opCode, "Public", poster.getUserName(), content);
                } else {
                    registeredClients.get(follower).addWaitingPost(content);
                }
            }

            //Sending the post to all of the tagged users
            for (int i = 0; i < tagged.size(); i++) {
                String taggedUser = (String) tagged.get(i);
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
        T contentToSend = (T) content;

        User sender = iteration(connectionId); //Finding who is the client that sent a command
        if (!sender.isLoggedIn() || !registeredClients.containsKey(recipient)) { //Either the sender is not logged in or the recipient is not registered
            sendError(opCode);
        } else {
            User recipientUser = registeredClients.get(recipient);
            if (recipientUser.isLoggedIn()) {
                sendNotification(recipientUser.getConnectionId(), opCode, "PM", sender.getUserName(), content);
                connections.send(recipientUser.getConnectionId(), contentToSend);
            } else {
                registeredClients.get(recipient).addWaitingPM(content);
            }
        }
    }

    private void userListProcess(UserList message) {

        int opCode = 7;

        User requester = iteration(connectionId); //Finding who is the client that sent a command
        if (!requester.isLoggedIn()) {
            sendError(opCode);
        } else {
            String userList = "";
            userList += registeredClients.size();
            Iterator userListIter = registeredClients.values().iterator(); //Finding each registered user TODO: not ordered by time registered!!! Either linkedlist is needed or a counter increased by 1
            while (userListIter.hasNext()) {
                User user = (User) userListIter.next();
                userList += " " + user.getUserName();
            }
            sendACK(opCode, userList);
        }

    }

    private void statsProcess(Stats message) {

        int opCode = 8;
        String userName = message.getUserName();


        User requester = iteration(connectionId); //Finding who is the client that sent a command
        if (!requester.isLoggedIn() || !registeredClients.containsKey(userName)) { //Either the requester is not logged in or the requested is not registered
            sendError(opCode);
        } else {
            User user = registeredClients.get(userName);
            String stat = "";
            int numOfPosts = user.getPersonalPosts().size();
            int numOfFollowers = user.getFollowers().size();
            int numOfFollowing = user.getFollowing().size();
            stat += numOfPosts + " " + numOfFollowers + " " + numOfFollowing;
            sendACK(opCode, stat);
        }
    }


    /**
     * Sending an ERROR with the corresponding opcode
     *
     * @param opCode
     */
    private void sendError(int opCode) {
        Error error = new Error(opCode);
        T errorMessage = (T) error.createMessage();
        connections.send(connectionId, errorMessage);
    }

    /**
     * Sending an ACK with the corresponding opcode
     *
     * @param opCode
     */
    private void sendACK(int opCode) {
        ACK ack = new ACK(opCode);
        T ackMessage = (T) ack.createMessage();
        connections.send(connectionId, ackMessage);
    }

    /**
     * Sending an ACK with the corresponding opcode, and optional message
     *
     * @param opCode
     * @param optional
     */
    private void sendACK(int opCode, String optional) {
        ACK ack = new ACK(opCode, optional);
        T ackMessage = (T) ack.createMessage();
        connections.send(connectionId, ackMessage);
    }

    /**
     * Sending a NOTIFICATION with the corresponding opcode, and either it is private or public, who sent the post/message, and the content
     *
     * @param recipientConnectionId
     * @param opCode
     * @param kind
     * @param user
     * @param content
     */

    private void sendNotification(int recipientConnectionId, int opCode, String kind, String user, String content) {
        Notification notification = new Notification(opCode, kind, user, content);
        T notificationMessage = (T) notification.createMessage();
        connections.send(recipientConnectionId, notificationMessage);
    }

    /**
     * Finding the @toFind in the registered users list
     *
     * @param toFind
     * @return the user that we found or null if he/she wasn't found
     */
    private User iteration(Integer toFind) {

        boolean found = false;
        Iterator iterThroughRegistered = registeredClients.values().iterator();
        User user = (User) iterThroughRegistered.next();
        if (user.getConnectionId() == connectionId) {
            found = true;
        }
        while (iterThroughRegistered.hasNext() && !found) {
            user = (User) iterThroughRegistered.next();
            if (user.getConnectionId() == connectionId) {
                found = true;
            }
        }

        if (!found) {
            return null;
        }

        return user;
    }

}
