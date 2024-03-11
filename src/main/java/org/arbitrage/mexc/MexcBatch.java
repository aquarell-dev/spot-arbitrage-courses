package org.arbitrage.mexc;

import org.arbitrage.client.SocketExchangeClient;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.exchange.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class MexcBatch extends SocketExchangeClient {
    private final String DEPTH_CHANNEL = "spot@public.limit.depth.v3.api@";
    private final String DEPTH = "@20";

    public MexcBatch(CoinsProcessor processor, List<String> coins) {
        super(Exchange.MEXC, processor, coins, 10);
    }

    @Override
    protected void onMessage(String message) {
//        System.out.println(message);
    }

    @Override
    protected String getPingMessage() {
        return "{\"method\":\"PING\"}";
    }

    @Override
    protected String createSubscriptionMessage(List<String> coins) {
        JSONObject message = new JSONObject();

        List<String> stringCoins = coins.stream().map(coin -> DEPTH_CHANNEL + coinToTicker(coin) + DEPTH).toList();

        message.put("method", "SUBSCRIPTION");
        message.put("params", new JSONArray(stringCoins));

        return message.toString();
     }

    @Override
    public String tickerToCoin(String ticker) {
        return ticker.replace("USDT", "");
    }

    @Override
    public String coinToTicker(String coin) {
        return coin + "USDT";
    }
}
