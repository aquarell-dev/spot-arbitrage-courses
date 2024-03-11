package org.arbitrage.exchange;

import java.net.URI;

public enum Exchange {
    GATE("Gate.io", "wss://api.gateio.ws/ws/v4/", 0.002),
    BITGET ("Bitget", "wss://ws.bitget.com/v2/ws/public", 0.001),
    MEXC ("MEXC Global", "https://api.mexc.com/api/v3/", 0), // wss://wbs.mexc.com/ws
    HTX("HTX", "wss://api.huobi.pro/ws", 0.002),
    KUCOIN("KuCoin", "wss://ws-api-spot.kucoin.com/", 0.002);

    public final String exchange;
    public final URI baseUri;
    public final double takerFee;

    private Exchange(String exchange, String baseUri, double takerFee) {
        this.exchange = exchange;
        this.baseUri = URI.create(baseUri);
        this.takerFee = takerFee;
    }

    public static Exchange getByCaption(String caption) {
        for (Exchange exchange : values()) {
            if (exchange.exchange.equals(caption)) {
                return exchange;
            }
        }
        throw new IllegalArgumentException("No Exchange with caption: " + caption);
    }
}
