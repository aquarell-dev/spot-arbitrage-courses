package org.arbitrage.coins;

import org.arbitrage.exchange.Exchange;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Coin {
    public final String coin;
    public final Exchange exchange;
    public final List<Network> networks;
    public final Double change;
    public final Double turnover;
    public final String tradeUrl;
    public final String withdrawUrl;
    public final String depositUrl;

    public Coin(String coin, Exchange exchange, List<Network> networks, Double change, Double turnover, String tradeUrl, String withdrawUrl, String depositUrl) {
        this.coin = coin;
        this.exchange = exchange;
        this.networks = networks;
        this.change = change;
        this.turnover = turnover;
        this.tradeUrl = tradeUrl;
        this.withdrawUrl = withdrawUrl;
        this.depositUrl = depositUrl;
    }

    public Coin(JSONObject coin) {
        this.coin = coin.getString("coin");
        this.tradeUrl = coin.getString("trade_url");
        this.depositUrl = coin.getString("deposit_url");
        this.withdrawUrl = coin.getString("withdraw_url");
        this.turnover = coin.isNull("volume") ? null : coin.getDouble("volume");
        this.change = coin.isNull("percentage") ? null : coin.getDouble("percentage");
        this.exchange = Exchange.getByCaption(coin.getString("exchange"));
        this.networks = parseNetworks(coin.getJSONArray("networks"));
    }

    public JSONObject toJson() {
        JSONObject coin = new JSONObject();

        coin.put("coin", this.coin);
        coin.put("tradeUrl", this.tradeUrl);
        coin.put("depositUrl", this.depositUrl);
        coin.put("withdrawUrl", this.withdrawUrl);
        coin.put("change", this.change == null ? Optional.empty() : this.change);
        coin.put("turnover", this.turnover == null ? Optional.empty() : this.turnover);

        return coin;
    }

    private List<Network> parseNetworks(JSONArray jsonNetworks) {
        List<Network> networks = new ArrayList<>();

        for (int i = 0; i < jsonNetworks.length(); i++) {
            JSONObject jsonNetwork = jsonNetworks.getJSONObject(i);

            if (jsonNetwork.isNull("chain")) continue;

            Network network = new Network(jsonNetwork.getString("chain"), jsonNetwork.getDouble("fee"),
                    jsonNetwork.getBoolean("withdrawable"), jsonNetwork.getBoolean("depositable"));

            if (network.depositable || network.withdrawable) networks.add(network);
        }

        return networks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coin coin1 = (Coin) o;
        return Objects.equals(coin, coin1.coin) && exchange == coin1.exchange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(coin, exchange);
    }
}
