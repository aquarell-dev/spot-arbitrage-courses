package org.arbitrage.server;

public enum Channel {
    ARBITRAGE("arbitrage");

    public final String channel;

    private Channel(String channel) {
        this.channel = channel;
    }
}
