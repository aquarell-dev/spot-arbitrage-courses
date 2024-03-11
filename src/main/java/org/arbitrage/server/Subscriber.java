package org.arbitrage.server;

import org.java_websocket.WebSocket;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Subscriber {
    public final WebSocket connection;
    private final Set<String> channels = new HashSet<>();

    public Subscriber(WebSocket connection) {
        this.connection = connection;
    }

    public Set<String> getChannels() {
        return channels;
    }

    public void subscribe(String channel) {
        channels.add(channel);
    }

    public boolean isSubscriber(String wantedChannel) {
        return channels.stream().anyMatch(channel -> channel.equals(wantedChannel));
    }

    public void unsubscribe(String channel) {
        channels.remove(channel);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscriber that = (Subscriber) o;
        return Objects.equals(connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connection);
    }
}
