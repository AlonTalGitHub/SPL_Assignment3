package bgu.spl.net.srv;

import bgu.spl.net.api.Commands.Login;
import bgu.spl.net.api.Commands.Register;
import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.bidi.Command;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private int endIndex(int start){
        while (true){
            if(bytes[start] == 0)// TODO check if start > len
                return start ;
            start++;
        }
    }

    // Runner sample check
    public static void main(String[] args) {
        byte [] a = new byte[10];
        a[0] = 0;
        a[1] = 1;
        a[2] = 'a';
        a[3] = 'b';
        a[4] = 'c';
        a[5] = 0;
        a[6] = 'd';
        a[7] = 'e';
        a[8] = 'f';
        a[9] = 0;
        MessageEncoderDecoderImpl messageEncoderDecoder = new MessageEncoderDecoderImpl();
        for (byte b : a) {
            messageEncoderDecoder.decodeNextByte(b);
        }
        messageEncoderDecoder.decodeNextByte((byte) '\n');

    }
}
