package org.arbitrage.coins;

import org.arbitrage.exchange.Exchange;
import org.json.JSONObject;

public class JSONCoinsProcessor extends CoinsProcessor {
    private final JSONObject payload;

    public JSONCoinsProcessor(JSONObject payload) {
        super();
        this.payload = payload;
        this.process();
    }

    @Override
    public void process() {
        for (Exchange exchange : Exchange.values()) {
            if (!payload.has(exchange.exchange)) continue;

            JSONObject exchangeCoins = payload.getJSONObject(exchange.exchange);

            exchangeCoins.keySet()
                    .forEach(key -> {
                        JSONObject jsonCoin = exchangeCoins.getJSONObject(key);

                        Coin coin = new Coin(jsonCoin);

                        if (!coin.networks.isEmpty()) addCoin(coin, exchange);
                    });
        }
    }
}
