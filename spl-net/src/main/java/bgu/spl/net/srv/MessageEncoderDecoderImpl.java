package bgu.spl.net.srv;

import bgu.spl.net.api.Commands.*;
import bgu.spl.net.api.Commands.ACKs.FollowACK;
import bgu.spl.net.api.Commands.ACKs.StatACK;
import bgu.spl.net.api.Commands.ACKs.UserListACK;
import bgu.spl.net.api.Commands.Error;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.Command;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Command> {

    //Fields
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;


    @Override
    public Command decodeNextByte(byte nextByte) {
        if (nextByte == '\n') {
            short op_code = bytesToShort(bytes);

            switch (op_code) {

                case 1: // Register
                    int start1 = 2;
                    int endindex1 = endIndex(start1);
                    String userName1 = popString(start1, endindex1 - start1);
                    String passWord1 = popString(endindex1 + 1, len - endindex1 -2);
                    len = 0;
                    return new Register(userName1, passWord1);

                case 2: // Login
                    int start2 = 2;
                    int endindex2 = endIndex(start2);
                    String userName2 = popString(start2, endindex2 - start2);
                    String passWord2 = popString(endindex2 + 1, len - endindex2 -2);
                    len = 0;
                    return new Login(userName2, passWord2);

                case 3: // Logout
                    len = 0;
                    return new Logout();

                case 4: // Follow
                    int start4 = 5;
                    int follow = bytes[2];
                    int endindex4 = endIndex(start4);
                    byte[] b = Arrays.copyOfRange(bytes, 3, 5);
                    int numOfUsers = bytesToShort(b);
                    List<String> userNameList = new ArrayList<>();

                    for(int i = 0; i < numOfUsers; i++){ // adding users to list
                        userNameList.add(popString(start4, endindex4 - start4));
                        start4 = endindex4 + 1;
                        endindex4 = endIndex(start4);
                    }
                    len = 0;
                    return new Follow(follow, numOfUsers, userNameList);

                case 5: // Post
                    int start5 = 2;
                    List<String> taggedUsers = taggedListFromText(start5);
                    String content = popString(start5, len - start5 -1);
                    len = 0;
                    return new Post(content, taggedUsers);

                case 6: // PM
                    int start6 = 2;
                    String user = popString(start6, endIndex(start6) - start6);
                    start6 = endIndex(start6) + 1;
                    content = popString(start6,   len - start6  -1);
                    len = 0;
                    return new PM(user, content);

                case 7: // UserList
                    len = 0;
                    return new UserList();

                case 8: // STAT
                    int start8 = 2;
                    String statuser = popString(start8, endIndex(start8) - start8);
                    len = 0;
                    return new Stat(statuser);


            }

        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    @Override
    public byte[] encode(Command message) {

        int intOpcode = message.getOp_code();
        short shortOpcode = (short) intOpcode;

        switch (intOpcode) {

            case 9: // Notification
                buildBytesArr(shortToBytes(shortOpcode));
                int kind = ((Notification) message).getKind();
                buildBytesArr(shortToBytes((short) kind));

                String postingUser = ((Notification) message).getUser();
                buildBytesArr(postingUser.getBytes());
                pushByte((byte) 0);

                String content = ((Notification) message).getContent();
                buildBytesArr(content.getBytes());
                pushByte((byte) 0);

                len = 0;
                return bytes;

            case 10: // ACKs
                int intOpcode2 = ((ACK) message).getrOpCode();

                switch (intOpcode2) {
                    case 4: // FollowACK
                        List<Short> opShorts = splitIntToShort(intOpcode);
                        for (short digit : opShorts){
                            pushByte((byte) digit);
                        }
                        buildBytesArr(shortToBytes((short) intOpcode2));
                        int numOfUsers = ((FollowACK) message).getNumOfUsers();
                        buildBytesArr(shortToBytes((short) numOfUsers));
                        List<String> userNameList = ((FollowACK) message).getUserNameList();

                        for(String name : userNameList){
                            buildBytesArr(name.getBytes());
                            pushByte((byte) 0);
                        }

                        len = 0;
                        return bytes;

                    case 7: // UserListACK
                        List<Short> opShorts1 = splitIntToShort(intOpcode);
                        for (short digit : opShorts1){
                            pushByte((byte) digit);
                        }
                        buildBytesArr(shortToBytes((short) intOpcode2));
                        int numOfUsers2 = ((UserListACK) message).getNumOfUsers();
                        buildBytesArr(shortToBytes((short) numOfUsers2));
                        List<String> userNameList2 = ((UserListACK) message).getUserNameList();

                        for(String name : userNameList2){
                            buildBytesArr(name.getBytes());
                            pushByte((byte) 0);
                        }

                        len = 0;
                        return bytes;

                    case 8: // StatACK
                        List<Short> opShorts2 = splitIntToShort(intOpcode);
                        for (short digit : opShorts2){
                            pushByte((byte) digit);
                        }
                        buildBytesArr(shortToBytes((short) intOpcode2));
                        int numOfPosts = ((StatACK) message).getNumOfPosts();
                        buildBytesArr(shortToBytes((short) numOfPosts));
                        int numOfFollowers = ((StatACK) message).getNumOfFollowers();
                        buildBytesArr(shortToBytes((short) numOfFollowers));
                        int numOfFollowing = ((StatACK) message).getNumOfFollowing();
                        buildBytesArr(shortToBytes((short) numOfFollowing));

                        len = 0;
                        return bytes;
                }

            case 11: // Error
                List<Short> opShorts = splitIntToShort(intOpcode);
                for (short digit : opShorts){
                    pushByte((byte) digit);
                }
                int errorOp = ((Error) message).getrOpCode();
                buildBytesArr(shortToBytes((short) errorOp));

                len = 0;
                return bytes;
        }
        return new byte[0];
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private void buildBytesArr(byte [] bytesArr) {
        for(byte b : bytesArr){
            pushByte(b);
        }
    }

    private String popString(int start , int len) {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, start, len, StandardCharsets.UTF_8);
        return result;
    }

    private short bytesToShort(byte[] byteArr)
    {
        return (short) (byteArr[0]*10 + byteArr[1]);
//        short result = (short)(((byteArr[0] & 0xff) << 8));
//        result += (short)(byteArr[1] & 0xff);
//        return result;
    }

    private byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    private List<Short> splitIntToShort(int num){
        short units = (short)(num % 10);
        short tens = (short)(num / 10);
        List<Short> shorts = new LinkedList<>();
        shorts.add(tens);
        shorts.add(units);


        return shorts;
    }

    private int endIndex(int start){
        while(true){
            if(bytes[start] == 0)// TODO check if start > len
                return start;
            start++;
        }
    }

    private int SpaceIndex(int start){
        while(true){
            if(bytes[start] == ' ' || bytes[start] == 0 || bytes[start] == '@')// TODO check if start > len
                return start;
            start++;
        }
    }

    private List<String> taggedListFromText(int start){

        List<String> tagged = new ArrayList<String>();

        while(bytes[start] != 0){
            if(bytes[start] == '@'){
                start++;
                int endindex = SpaceIndex(start);
                String user = popString(start, endindex - start);
                start = endindex;
                if(user != null && user.length() > 0)
                    tagged.add(user);
            }else
                start++;

        }

        return tagged;
    }



    // Runner sample check
    public static void main(String[] args) {
        List<String> userList = new LinkedList<String>();
        userList.add("user1");
        userList.add("user2");
        String postingUser = "alon";
        String content = "message content";
        UserListACK userListACK = new UserListACK(2,userList);

//        byte [] a = new byte[6];
//        a[0] = 0;
//        a[1] = 1;
//        a[2] = 'a';
//        a[3] = 0;
//        a[4] = 'b';
//        a[5] = 0;
//        a[7] = 'b';
//        a[8] = 0;
//        a[9] = 'c';
//        a[10] = 0;
//        a[11] = 'd';
//        a[12] = 0;
//        a[13] = 'e';
//        a[14] = 0;
//        a[15] = 'f';
//        a[16] = 0;
//        a[17] = 'g';
//        a[18] = 0;
//        a[19] = 'h';
//        a[20] = 0;
//        a[21] = 'i';
//        a[22] = 0;
//        a[23] = 'j';
//        a[24] = 0;


        MessageEncoderDecoderImpl messageEncoderDecoder = new MessageEncoderDecoderImpl();

        messageEncoderDecoder.encode(userListACK);
//        for (byte b : a) {
//            messageEncoderDecoder.decodeNextByte(b);
//        }
//        messageEncoderDecoder.decodeNextByte((byte) '\n');

    }
}
