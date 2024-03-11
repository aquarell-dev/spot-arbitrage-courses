package org.arbitrage.mexc;

import org.arbitrage.coins.Coin;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;

public class MexcOrderbook extends Orderbook {
    public MexcOrderbook(Coin coin) {
        super(coin, Exchange.MEXC);
    }
}
