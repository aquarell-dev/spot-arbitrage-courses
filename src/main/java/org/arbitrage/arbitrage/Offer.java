package org.arbitrage.arbitrage;

import org.arbitrage.coins.Network;
import org.arbitrage.orderbook.NotEnoughVolume;
import org.arbitrage.orderbook.Orderbook;
import org.json.JSONObject;

import java.util.NoSuchElementException;

public class Offer {
    private final Integer volume;
    private final Orderbook buyOrderbook;
    private final Orderbook sellOrderbook;
    private final Network network;
    private final Double spread;
    private final Double profit;

    public Offer(Integer volume, Network network, Orderbook buyOrderbook, Orderbook sellOrderbook) {
        this.volume = volume;
        this.network = network;
        this.buyOrderbook = buyOrderbook;
        this.sellOrderbook = sellOrderbook;
        this.spread = setSpread(volume);
        this.profit = setProfit();
    }

    private Double setSpread(int volume) {
        try {
            Double bestBuyPrice = buyOrderbook.getBestAsk();
            double coins = volume / bestBuyPrice;

            double sellPrice = sellOrderbook.calculateSellAveragePrice(coins);
            double buyPrice = buyOrderbook.calculateBuyAveragePrice(coins);

            return sellPrice / buyPrice - 1;
        } catch (NoSuchElementException | NotEnoughVolume e) {
            return 0d;
        }

    }

    private Double setProfit() {
        double netSpread = spread - buyOrderbook.exchange.takerFee - buyOrderbook.exchange.takerFee;

        try {
            double transactionFee = network.fee * buyOrderbook.getBestAsk();

            return volume * netSpread - transactionFee;
        } catch (NoSuchElementException e) {
            return -1d;
        }

    }

    public Double getProfit() {
        return profit;
    }

    public Double getSpread() {
        return spread;
    }

    public JSONObject toJson() {
        JSONObject offer = new JSONObject();

        offer.put("volume", volume);
        offer.put("spread", spread);
        offer.put("profit", profit);

        return offer;
    }
}
