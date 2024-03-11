package org.arbitrage.kucoin;

import org.arbitrage.coins.Coin;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;

import java.util.Map;

public class KucoinOrderbook extends Orderbook {
    public KucoinOrderbook(Coin coin) {
        super(coin, Exchange.KUCOIN);
    }
}
