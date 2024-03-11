package org.arbitrage.common;

import java.util.ArrayList;
import java.util.List;

public class Batch {
    public static <T> List<List<T>> splitIntoBatches(List<T> inputList, int batchSize) {
        List<List<T>> batches = new ArrayList<>();

        for (int i = 0; i < inputList.size(); i += batchSize) {
            int end = Math.min(i + batchSize, inputList.size());
            batches.add(new ArrayList<>(inputList.subList(i, end)));
        }

        return batches;
    }
}
