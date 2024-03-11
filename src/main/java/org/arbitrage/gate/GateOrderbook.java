package org.arbitrage.gate;

import org.arbitrage.coins.Coin;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GateOrderbook extends Orderbook {
    public GateOrderbook(Coin coin) {
        super(coin, Exchange.GATE);
    }
}
