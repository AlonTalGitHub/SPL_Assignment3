package bgu.spl.net.srv;

import bgu.spl.net.api.bidi.Connections;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    // Fields
    private ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionHandlerHashMap;

    // Public constructor
    public ConnectionsImpl(ConcurrentHashMap<Integer, ConnectionHandler<T>> connectionHandlerHashMap) {
        this.connectionHandlerHashMap = connectionHandlerHashMap; //TODO check if need concurrent
    }


    public void connect(ConnectionHandler<T> connectionHandler, int connectionId){
        connectionHandlerHashMap.put(connectionId, connectionHandler);
    }
    @Override
    public boolean send(int connectionId, T msg) {
        if (connectionHandlerHashMap.containsKey(connectionId)){
            connectionHandlerHashMap.get(connectionId).send(msg);
            return true;
        }

        return false;
    }

    @Override
    public void broadcast(T msg) {
        for (Integer connectionId : connectionHandlerHashMap.keySet()) {
            send(connectionId, msg);
        }

    }

    @Override
    public void disconnect(int connectionId) {
        connectionHandlerHashMap.remove(connectionId);
    }
}
