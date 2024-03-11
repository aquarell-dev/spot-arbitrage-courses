package org.arbitrage.gate;

import org.arbitrage.client.SocketExchangeClient;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class GateClient extends SocketExchangeClient {
    private final String ORDERBOOK_CHANNEL = "spot.order_book";
    private final String PING_CHANNEL = "spot.pong";
    private final String ORDERBOOK_EVENT = "update";

    public GateClient(CoinsProcessor processor) {
        super(Exchange.GATE, processor, 10);
    }

    @Override
    public void onMessage(String message) {
        JSONObject jsonMessage = new JSONObject(message);

        if (!jsonMessage.has("channel") || !jsonMessage.has("event")) return;

        String channel = jsonMessage.getString("channel");
        String event = jsonMessage.getString("event");

        if (channel.equals(PING_CHANNEL)) return;

        if (!channel.equals(ORDERBOOK_CHANNEL)) return;
        if (!event.equals(ORDERBOOK_EVENT)) return;

        JSONObject result = jsonMessage.getJSONObject("result");

        String ticker = result.getString("s");
        String coin = tickerToCoin(ticker);

        if (getOrderbooks().get(coin) == null) {
            addCoinToOrderbook(coin);
        }

        Orderbook orderbook = getOrderbooks().get(coin);

        orderbook.updateBids(result.getJSONArray("bids"));
        orderbook.updateAsks(result.getJSONArray("asks"));
    }

    @Override
    public String getPingMessage() {
        JSONObject ping = new JSONObject();

        ping.put("time", System.currentTimeMillis() / 1000);
        ping.put("channel", "spot.ping");

        return ping.toString();
    }

    @Override
    protected String createSubscriptionMessage(List<String> coins) {
        var coinsMessage = coins.stream().map(coin -> List.of(coinToTicker(coin), "20", "100ms")).toList();

        JSONObject subscriptionMessage = new JSONObject();
        subscriptionMessage.put("time", System.currentTimeMillis() / 1000);
        subscriptionMessage.put("channel", "spot.order_book");
        subscriptionMessage.put("event", "subscribe");
        subscriptionMessage.put("payload", new JSONArray(coinsMessage));

        return subscriptionMessage.toString();
    }

    @Override
    public String coinToTicker(String coin) {
        return String.format("%s_USDT", coin);
    }

    @Override
    public String tickerToCoin(String ticker) {
        return ticker.replace("_USDT", "");
    }
}
