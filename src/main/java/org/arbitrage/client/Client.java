package org.arbitrage.client;

import org.arbitrage.exchange.Exchange;
import org.arbitrage.orderbook.Orderbook;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface Client extends Runnable {
    List<String> getCoins();

    Exchange getExchange();

    Map<String, Orderbook> getOrderbooks();

    String tickerToCoin(String ticker);

    String coinToTicker(String coin);

    default int[] createSubscriptionMilestones(int coinsCount) {
        int[] milestonesPercentage = new int[]{25, 50, 75, 100};

        return Arrays.stream(milestonesPercentage)
            .map(milestone -> (int) coinsCount * milestone / 100)
            .toArray();
    }
    default Integer getMilestone(long value, int[] milestones) {
        int[] currentMilestone = Arrays.stream(milestones)
            .filter(milestone -> milestone == value)
            .toArray();

        if (currentMilestone.length != 1) return null;

        return currentMilestone[0];
    }

}
