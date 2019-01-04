package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.BidiMessagingProtocolImpl;
import bgu.spl.net.api.bidi.User;


import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


public class ReactorServer<T> {


    ConcurrentHashMap<String, User> hash1 = new ConcurrentHashMap<>();
    ConcurrentHashMap<Integer, String> hash2 = new ConcurrentHashMap<>();
    LinkedList<User> list = new LinkedList<>();

    Supplier<BidiMessagingProtocol<T>> supp = () -> new BidiMessagingProtocolImpl<>(hash1, hash2, list);

    public Supplier<BidiMessagingProtocol<T>> getSupp() {
        return supp;
    }

    public static void main(String[] args) {
        ReactorServer server = new ReactorServer();

        Reactor reactor = new Reactor(Runtime.getRuntime().availableProcessors(),
                7777, //port
                server.getSupp(), //protocol factory
                MessageEncoderDecoderImpl::new //message encoder decoder factory
        );

        reactor.serve();

        try {
            reactor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
