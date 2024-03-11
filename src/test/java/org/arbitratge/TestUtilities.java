package org.arbitratge;

import org.arbitrage.bitget.BitgetOrderbook;
import org.arbitrage.coins.Coin;
import org.arbitrage.coins.Network;
import org.arbitrage.exchange.Exchange;
import org.arbitrage.gate.GateOrderbook;
import org.arbitrage.htx.HTXOrderbook;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class TestUtilities {
    public static final NavigableMap<Double, Double> sampleAsks = new TreeMap<>() {{
        put(120d, 50d);
        put(110d, 100d);
        put(100d, 100d);
    }};
    public static final NavigableMap<Double, Double> sampleBids = new TreeMap<>() {{
        put(90d, 100d);
        put(80d, 100d);
        put(70d, 50d);
    }};

    public static JSONArray createSampleQuotes(NavigableMap<Double, Double> quotes) {
        JSONArray quotesArray = new JSONArray();

        quotes.forEach((key, value) -> quotesArray.put(new JSONArray(List.of(key, value))));

        return quotesArray;
    }

    public static void updateOrderbook(Orderbook orderbook, NavigableMap<Double, Double> bids, NavigableMap<Double, Double> asks) {
        JSONArray jsonBids = createSampleQuotes(bids);
        JSONArray jsonAsks = createSampleQuotes(asks);

        orderbook.updateBids(jsonBids);
        orderbook.updateAsks(jsonAsks);
    }

    public static void updateOrderbook(Orderbook orderbook) {
        JSONArray jsonBids = createSampleQuotes(sampleBids);
        JSONArray jsonAsks = createSampleQuotes(sampleAsks);

        orderbook.updateBids(jsonBids);
        orderbook.updateAsks(jsonAsks);
    }

    public static Orderbook getOrderBookInstance(Exchange exchange) {
        List<Network> networks = List.of(
                new Network("BRC", 2d, true, true)
        );

        return switch (exchange) {
            case GATE -> new GateOrderbook(new Coin(new JSONObject()));
            case BITGET -> new BitgetOrderbook(new Coin(new JSONObject()));
            case HTX -> new HTXOrderbook(new Coin(new JSONObject()));
            default -> throw new IllegalArgumentException("Unknown order book type: " + exchange);
        };
    }
}
