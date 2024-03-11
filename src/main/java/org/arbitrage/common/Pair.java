package org.arbitrage.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Pair {
    public static <T> List<List<T>> generatePairs(List<T> list) throws NullPointerException {
        List<List<T>> pairs = new ArrayList<>();

        for (T element : list) {
            if (element == null) continue;
            for (T anotherElement : list) {
                if (anotherElement == null) continue;
                if (element.equals(anotherElement)) continue;

                // we're going this way to avoid not null in List.of
                pairs.add(Stream.of(element, anotherElement)
                        .toList());
            }
        }

        return pairs;
    }
}
