package org.arbitrage.htx;

import org.arbitrage.client.SocketExchangeClient;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

public class HTXClient extends SocketExchangeClient {
    public HTXClient(CoinsProcessor processor) {
        super(Exchange.HTX, processor, 1);
    }

    @Override
    public void onMessage(String s) {
        JSONObject message = new JSONObject(s);

        if (message.has("ping")) {
            Long pingTime = message.getLong("ping");
            this.client.send(String.format("{\"pong\": %d}", pingTime));
            return;
        }

        if (message.has("status") && message.has("subbed")) {
            if (!message.getString("status")
                    .equals("ok")) return;

            String coin = tickerToCoin(message.getString("subbed")
                    .split("\\.")[1]);

            addCoinToOrderbook(coin);

            return;
        }

        if (!message.has("tick")) return;
        if (!message.has("ch")) return;

        String coin = tickerToCoin(message.getString("ch")
                .split("\\.")[1]);

        Orderbook orderbook = getOrderbooks().get(coin);

        JSONObject tick = message.getJSONObject("tick");

        JSONArray bids = tick.getJSONArray("bids");
        JSONArray asks = tick.getJSONArray("asks");

        orderbook.updateAsks(asks);
        orderbook.updateBids(bids);
    }

    @Override
    protected String createSubscriptionMessage(List<String> coins) {
        String coin = coins.getFirst();

        JSONObject message = new JSONObject();

        Random random = new Random();

        int max = 10000;
        int min = 1;

        // Generate a random integer within the specified range
        int randomInt = random.nextInt(max - min + 1) + min;

        message.put("id", String.format("id%s", randomInt));
        message.put("sub", String.format("market.%s.depth.step0", coinToTicker(coin)));

        return message.toString();
    }

    @Override
    protected String getPingMessage() {
        return "";
    }

    @Override
    public String coinToTicker(String coin) {
        return coin.toLowerCase() + "usdt";
    }

    @Override
    public String tickerToCoin(String ticker) {
        return ticker.replace("usdt", "")
                .toUpperCase();
    }
}
