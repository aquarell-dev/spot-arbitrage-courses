package org.arbitrage.coins;

import org.arbitrage.exchange.Exchange;

import java.util.HashMap;
import java.util.List;

public abstract class CoinsProcessor {
    protected final HashMap<Exchange, HashMap<String, Coin>> coins;

    public CoinsProcessor() {
        this.coins = new HashMap<>();
    }

    public abstract void process();

    public Coin getCoin(String coin, Exchange exchange) {
        return coins.get(exchange).get(coin);
    }

    public HashMap<Exchange, HashMap<String, Coin>> getCoins() {
        return coins;
    }

    public List<String> getCoins(Exchange exchange) {
        return coins.get(exchange).keySet().stream().toList();
    }

    public void addCoin(Coin coin, Exchange exchange) {
        HashMap<Exchange, HashMap<String, Coin>> coins = getCoins();

        coins.computeIfAbsent(exchange, k -> new HashMap<>());

        if (!coins.containsKey(exchange)) coins.put(exchange, new HashMap<>());

        coins.get(exchange).put(coin.coin, coin);
    }
}
