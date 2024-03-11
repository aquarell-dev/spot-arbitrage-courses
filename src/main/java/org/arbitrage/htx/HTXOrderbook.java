package org.arbitrage.htx;

import org.arbitrage.coins.Coin;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;

import java.util.Map;

public class HTXOrderbook extends Orderbook {
    public HTXOrderbook(Coin coin) {
        super(coin, Exchange.HTX);
    }
}
