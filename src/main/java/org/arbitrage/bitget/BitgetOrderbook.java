package org.arbitrage.bitget;

import org.arbitrage.coins.Coin;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;

import java.util.Map;

public class BitgetOrderbook extends Orderbook {
    public BitgetOrderbook(Coin coin) {
        super(coin, Exchange.BITGET);
    }
}
