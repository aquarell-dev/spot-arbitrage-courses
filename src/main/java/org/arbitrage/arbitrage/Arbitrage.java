package org.arbitrage.arbitrage;

import org.arbitrage.coins.Network;
import org.arbitrage.orderbook.Orderbook;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.*;

public class Arbitrage {
    private final String coin;
    private final Orderbook buyOrderbook;
    private final Orderbook sellOrderbook;
    private final Network network;
    private final Offer offer;

    public Arbitrage(@NotNull String coin, @NotNull List<Orderbook> orderbooks) {
        this.coin = coin;
        buyOrderbook = orderbooks.get(0);
        sellOrderbook = orderbooks.get(1);
        this.network = getCheapestNetwork();
        this.offer = getBestOffer(900);
    }

    private Network getCheapestNetwork() {
        List<Network> validBuyNetworks = buyOrderbook.coin.networks.stream()
                .filter(buyNetwork -> buyNetwork.withdrawable)
                .toList();
        List<Network> validSellNetworks = sellOrderbook.coin.networks.stream()
                .filter(sellNetwork -> sellNetwork.depositable)
                .toList();

        List<Network> commonNetworks = validBuyNetworks.stream()
                .flatMap(buyNetwork -> validSellNetworks.stream()
                        .filter(sellNetwork -> buyNetwork.chain.equals(sellNetwork.chain)))
                .toList();

        return commonNetworks.stream()
                .min(Comparator.comparing(network -> network.fee))
                .orElse(null);
    }

    private Offer getBestOffer(int maxVolume) {
        if (network == null) return null;

        int lowerBoundary = 100;
        int step = 100;

        List<Integer> volumes = new ArrayList<>();

        for (int i = lowerBoundary; i <= maxVolume; i += step) volumes.add(i);

        Offer bestOffer = volumes.stream()
                .map(currentVolume -> new Offer(currentVolume, network, buyOrderbook, sellOrderbook))
                .max(Comparator.comparing(Offer::getProfit))
                .orElse(null);

        boolean moreThanZero = bestOffer != null && bestOffer.getProfit() > 0;

        return moreThanZero ? bestOffer : null;

    }

    @Override
    public String toString() {
        return String.format("%s(%s->%s)", coin, buyOrderbook.exchange.exchange, sellOrderbook.exchange.exchange);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arbitrage arbitrage1 = (Arbitrage) o;
        return Objects.equals(coin, arbitrage1.coin) && Objects.equals(buyOrderbook,
                arbitrage1.buyOrderbook
        ) && Objects.equals(sellOrderbook, arbitrage1.sellOrderbook);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coin, buyOrderbook, sellOrderbook);
    }

    public JSONObject toJSON() {
        JSONObject arbitrage = new JSONObject();

        arbitrage.put("coin", coin);
        arbitrage.put("origin", buyOrderbook.toJson());
        arbitrage.put("destination", sellOrderbook.toJson());

        if (network == null) return null;
        if (offer == null) return null;
        if (offer.getSpread() > 0.2) return null;

        arbitrage.put("offer", offer.toJson());

        try {
            arbitrage.put("network", network.toJson(buyOrderbook.getBestBid()));
        } catch (NoSuchElementException e) {
            return null;
        }

        return arbitrage;
    }
}
