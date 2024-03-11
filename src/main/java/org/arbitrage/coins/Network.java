package org.arbitrage.coins;

import org.json.JSONObject;

public class Network {
    public final String chain;
    public final Double fee;
    public final Boolean withdrawable;
    public final Boolean depositable;

    public Network(String chain, Double fee, Boolean withdrawable, Boolean depositable) {
        this.chain = chain;
        this.fee = fee;
        this.withdrawable = withdrawable;
        this.depositable = depositable;
    }

    public JSONObject toJson(double price) {
        JSONObject network = new JSONObject();

        network.put("chain", chain);
        network.put("fee", fee);
        network.put("usdtFee", price * fee);

        return network;
    }

}
