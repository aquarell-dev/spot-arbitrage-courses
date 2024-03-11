package org.arbitrage.orderbook;

public class NotEnoughVolume extends RuntimeException {
    public NotEnoughVolume(String message) {
        super(message);
    }
}
