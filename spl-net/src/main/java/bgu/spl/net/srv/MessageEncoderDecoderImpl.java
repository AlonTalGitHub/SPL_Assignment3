package bgu.spl.net.srv;

import bgu.spl.net.api.Commands.*;
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
                    String passWord1 = popString(endindex1 + 1, len - endindex1 -1);

                    return new Register(userName1, passWord1);

                case 2: // Login
                    int start2 = 2;
                    int endindex2 = endIndex(start2);
                    String userName2 = popString(start2, endindex2 - start2);
                    String passWord2 = popString(endindex2 + 1, len - endindex2 -1);

                    return new Login(userName2, passWord2);

                case 3: // Logout
                    return new Logout();

                case 4: // Follow
                    int start4 = 5;
                    int follow = bytes[2];
                    int endindex4 = endIndex(start4);
                    int numOfUsers = bytesToShort(Arrays.copyOfRange(bytes, 3, 5));
                    List<String> userNameList = new ArrayList<>();

                    for(int i = 0; i < numOfUsers; i++){ // adding users to list
                        userNameList.add(popString(start4, endindex4 - start4));
                        start4 = endindex4 + 1;
                        endindex4 = endIndex(start4);
                    }

                    return new Follow(follow, numOfUsers, userNameList);

                case 5: // Post
                    int start5 = 2;
                    List<String> taggedUsers = taggedListFromText(start5);
                    String content = popString(start5, endMessageIndex(start5));

                    return new Post(content, taggedUsers);

                case 6: // PM
                    int start6 = 2;
                    String user = popString(start6, endIndex(start6) - start6);
                    start6 = endIndex(start6) + 1;
                    content = popString(start6, endMessageIndex(start6));

                    return new PM(user, content);

                case 7: // UserList


            }

        }

        pushByte(nextByte);
        return null; //not a line yet
    }

    @Override
    public byte[] encode(Command message) {
        return new byte[0];
    }


    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }

        bytes[len++] = nextByte;
    }

    private String popString(int start , int len) {
        //notice that we explicitly requesting that the string will be decoded from UTF-8
        //this is not actually required as it is the default encoding in java.
        String result = new String(bytes, start, len, StandardCharsets.UTF_8);
        return result;
    }

    private short bytesToShort(byte[] byteArr)
    {
        short result = (short)(((byteArr[0] & 0xff) << 8) % 246);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private int endIndex(int start){
        while(true){
            if(bytes[start] == 0 || bytes[start] == ' ')// TODO check if start > len
                return start;
            start++;
        }
    }

    private int endMessageIndex(int start){
        while(true){
            if(bytes[start] == 0)// TODO check if start > len
                return start - 2;
            start++;
        }
    }

    private List<String> taggedListFromText(int start){

        List<String> tagged = new ArrayList<String>();

        while(bytes[start] != 0){
            if(bytes[start] == '@'){
                start++;
                int endindex = endIndex(start);
                String user = popString(start, endindex - start);
                start = endindex;
                if(user != null)
                    tagged.add(user);
            }
            start++;
        }

        return tagged;
    }



    // Runner sample check
    public static void main(String[] args) {
        byte [] a = new byte[25];
        a[0] = 0;
        a[1] = 6;
        a[2] = 'R';
        a[3] = 'i';
        a[4] = 'c';
        a[5] = 'k';
        a[6] = 0;
        a[7] = 'g';
        a[8] = 'r';
        a[9] = 'e';
        a[10] = 'a';
        a[11] = 't';
        a[12] = ' ';
        a[13] = 'c';
        a[14] = 'o';
        a[15] = 'n';
        a[16] = 't';
        a[17] = 'e';
        a[18] = 'n';
        a[19] = 't';
        a[20] = ' ';
        a[21] = 'e';
        a[22] = 'n';
        a[23] = 'd';
        a[24] = 0;

        MessageEncoderDecoderImpl messageEncoderDecoder = new MessageEncoderDecoderImpl();
        for (byte b : a) {
            messageEncoderDecoder.decodeNextByte(b);
        }
        messageEncoderDecoder.decodeNextByte((byte) '\n');

    }
}
