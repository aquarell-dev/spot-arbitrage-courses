package org.arbitrage.client;

import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;

import java.util.List;
import java.util.Map;

public interface Client extends Runnable {
    List<String> getCoins();

    Exchange getExchange();

    Map<String, Orderbook> getOrderbooks();

    String tickerToCoin(String ticker);

    String coinToTicker(String coin);
}
