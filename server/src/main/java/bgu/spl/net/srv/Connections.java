package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    String send(int connectionId, T msg);

    String send(String channel, T msg);

    void disconnect(int connectionId);
}
