package bgu.spl.net.impl.echo;

import bgu.spl.net.api.bidi.BidiMessagingProtocol;
import bgu.spl.net.api.bidi.Connections;
import bgu.spl.net.srv.ConnectionsImpl;

import java.time.LocalDateTime;

public class EchoProtocol implements BidiMessagingProtocol<String> { // todo

    private boolean shouldTerminate = false;
    private Connections<String> connections; // todo
    private int connectionId; // todo

    @Override
    public void start(int connectionId, Connections<String> connections) { // todo
        this.connectionId =connectionId;
        this.connections = connections;
    }

    @Override
    public void process(String msg) {
        shouldTerminate = "bye".equals(msg);
        System.out.println("[" + LocalDateTime.now() + "]: " + msg);
        connections.broadcast(createEcho(msg)); // todo
    }

    private String createEcho(String message) {
        String echoPart = message.substring(Math.max(message.length() - 2, 0), message.length());
        return message + " .. " + echoPart + " .. " + echoPart + " ..";
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
