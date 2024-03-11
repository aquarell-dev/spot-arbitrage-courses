package org.arbitrage;

import org.arbitrage.server.SpreadServer;


public class Main {
    public static void main(String[] args) {
        SpreadServer server = new SpreadServer(8080);
        server.start();
    }
}
