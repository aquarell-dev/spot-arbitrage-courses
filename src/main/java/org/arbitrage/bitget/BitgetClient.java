package org.arbitrage.bitget;

import org.arbitrage.client.SocketExchangeClient;
import org.arbitrage.coins.CoinsProcessor;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class BitgetClient extends SocketExchangeClient {
    private final String ORDERBOOK_SUBSCRIPTION = "subscribe";
    private final String ORDERBOOK_SNAPSHOT = "snapshot";

    public BitgetClient(CoinsProcessor processor) {
        super(Exchange.BITGET, processor, 10);
    }

    @Override
    public void onMessage(String s) {
        if (s.equals("pong")) {
            return;
        }

        JSONObject message = new JSONObject(s);

        if (message.has("event") && message.get("event").equals(ORDERBOOK_SUBSCRIPTION)) {
            String coin = tickerToCoin(message.getJSONObject("arg").getString("instId"));
            addCoinToOrderbook(coin);
            return;
        }

        if (!message.has("action")) return;

        if (!message.getString("action").equals(ORDERBOOK_SNAPSHOT)) return;

        String ticker = message.getJSONObject("arg").getString("instId");
        String coin = tickerToCoin(ticker);

        Orderbook orderbook = getOrderbooks().get(coin);

        JSONObject data = message.getJSONArray("data").getJSONObject(0);

        orderbook.updateAsks(data.getJSONArray("asks"));
        orderbook.updateBids(data.getJSONArray("bids"));
    }

    @Override
    protected String createSubscriptionMessage(List<String> coins) {
        JSONArray coinsMessage = new JSONArray();

        coins.forEach(coin -> {
            JSONObject args = new JSONObject();

            args.put("instType", "SPOT");
            args.put("channel", "books15");
            args.put("instId", coinToTicker(coin));

            coinsMessage.put(args);
        });

        JSONObject subscriptionMessage = new JSONObject();
        subscriptionMessage.put("op", "subscribe");
        subscriptionMessage.put("args", new JSONArray(coinsMessage));

        return subscriptionMessage.toString();
    }

    @Override
    public String coinToTicker(String coin) {
        return String.format("%sUSDT", coin);
    }

    @Override
    public String tickerToCoin(String ticker) {
        return ticker.replace("USDT", "");
    }

    @Override
    public String getPingMessage() {
        return "ping";
    }
}
