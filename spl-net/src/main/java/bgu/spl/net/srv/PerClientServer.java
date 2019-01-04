package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.User;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class PerClientServer<T> {


    ConcurrentHashMap<String, User> hash1 = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> hash2 = new ConcurrentHashMap<>();
    LinkedList<User> list = new LinkedList<>();

    Supplier<BidiMessagingProtocol<T>> supp = () -> new BidiMessagingProtocolImpl<>(hash1, hash2, list);

    public Supplier<BidiMessagingProtocol<T>> getSupp() {
        return supp;
    }

    public static void main(String[] args) {

        /*
        PerClientServer server = new PerClientServer();

        Server.threadPerClient(
                7777, //port
                server.getSupp(), //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        ).serve();

    }


    */

}
