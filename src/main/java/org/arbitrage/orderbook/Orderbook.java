package org.arbitrage.orderbook;

import org.arbitrage.coins.Coin;
import org.arbitrage.exchange.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public abstract class Orderbook {
    public final Coin coin;
    public final Exchange exchange;
    protected final TreeMap<Double, Double> bids = new TreeMap<>();
    protected final TreeMap<Double, Double> asks = new TreeMap<>();
    protected long lastUpdate;

    public Orderbook(Coin coin, Exchange exchange) {
        this.coin = coin;
        this.exchange = exchange;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void updateBids(JSONArray bidsArray) {
        updateOrderBook(bids, bidsArray);
    }

    public void updateAsks(JSONArray asksArray) {
        updateOrderBook(asks, asksArray);
    }

    public synchronized Double getBestAsk() throws NoSuchElementException {
        return asks.firstKey();
    }

    public synchronized Double getBestBid() throws NoSuchElementException {
        return bids.lastKey();
    }

    public synchronized TreeMap<Double, Double> getBids() {
        return bids;
    }

    public synchronized TreeMap<Double, Double> getBids(long count) {
        TreeMap<Double, Double> bids = new TreeMap<>();

        this.bids.descendingMap()
                .entrySet()
                .stream()
                .limit(count)
                .forEach(entry -> bids.put(entry.getKey(), entry.getValue()));

        return bids;
    }

    public synchronized TreeMap<Double, Double> getAsks() {
        return asks;
    }

    public synchronized TreeMap<Double, Double> getAsks(int count) {
        TreeMap<Double, Double> asks = new TreeMap<>();

        this.asks.entrySet()
                .stream()
                .limit(count)
                .forEach(entry -> asks.put(entry.getKey(), entry.getValue()));

        return asks;
    }

    public synchronized Double getMeanPrice() {
        return (getBestAsk() + getBestBid()) / 2;
    }

    public synchronized Double getOrderbookVolume(TreeMap<Double, Double> quotes) {
        return quotes.entrySet()
                .stream()
                .map(entry -> entry.getValue() * entry.getKey())
                .reduce(Double::sum)
                .orElse(0d);
    }

    public JSONObject toJson() {
        JSONObject orderbook = new JSONObject();

        TreeMap<Double, Double> asks = getAsks(7);
        TreeMap<Double, Double> bids = getBids(7);

        orderbook.put("coin", coin.toJson());
        orderbook.put("exchange", exchange.exchange);
        orderbook.put("bidsVolume", getOrderbookVolume(bids));
        orderbook.put("asksVolume", getOrderbookVolume(asks));

        return orderbook;
    }

    public synchronized Double calculateBuyAveragePrice(double volumeInUSDT) throws NotEnoughVolume {
        return calculateAveragePrice(asks, volumeInUSDT);
    }

    public synchronized Double calculateSellAveragePrice(double volumeInUSDT) throws NotEnoughVolume {
        return calculateAveragePrice(bids.descendingMap(), volumeInUSDT);
    }

    protected synchronized void updateOrderBook(Map<Double, Double> map, JSONArray data) {
        map.clear();

        for (int i = 0; i < data.length(); i++) {
            JSONArray entry = data.getJSONArray(i);
            map.put(entry.getDouble(0), entry.getDouble(1));
        }

        setLastUpdate(System.currentTimeMillis());
    }

    private synchronized Double calculateAveragePrice(NavigableMap<Double, Double> orderBook, double amount) throws NotEnoughVolume {
        double remainingCoinAmount = amount;
        double totalPrice = 0;
        double totalVolumeInCoins = 0;

        for (Map.Entry<Double, Double> entry : orderBook.entrySet()) {
            double price = entry.getKey();
            double volume = entry.getValue();

            if (remainingCoinAmount <= 0) {
                break;
            }

            if (remainingCoinAmount < volume) {
                double volumeInCoinsToBeBought = remainingCoinAmount;
                totalPrice += price * volumeInCoinsToBeBought;
                totalVolumeInCoins += volumeInCoinsToBeBought;
                remainingCoinAmount = 0;
                break; // We've bought all the coins needed
            }

            totalPrice += price * volume;
            totalVolumeInCoins += volume;
            remainingCoinAmount -= volume;
        }

        if (remainingCoinAmount > 0) {
            throw new NotEnoughVolume("There ain't enough volume in the order book");
        }

        return totalPrice / totalVolumeInCoins; // Calculate the average price
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Orderbook orderbook = (Orderbook) o;
        return Objects.equals(coin, orderbook.coin) && exchange == orderbook.exchange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coin, exchange);
    }
}
