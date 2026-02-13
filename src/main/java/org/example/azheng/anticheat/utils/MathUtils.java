package org.example.azheng.anticheat.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MathUtils {

    /**
     * Finds the difference between two angles and clamps it to [-180, 180]
     */
    public static float angleDiff(float a1, float a2) {
        float diff = (a1 - a2) % 360f;
        if (diff > 180f) { diff -= 360f; }
        if (diff < -180f) { diff += 360f; }
        return diff;
    }

    /**
     * Calculate gcd of two floats using modified Euclidian Algorithm
     */
    public static float gcd(float curr, float prev) {
        if (curr < prev) return gcd(Math.abs(prev), Math.abs(curr));

        if (Math.abs(prev) <= 0.0001f) return curr;

        return gcd(prev, (float) (curr - Math.floor(curr / prev) * prev));
    }

    public static <T extends Number> T getMode(Collection<T> c) {
        Map<T, Integer> freq = new HashMap<>();
        c.forEach(val -> {
            int number = freq.getOrDefault(val, 0) + 1;
            freq.put(val, number);
        });

        return Collections.max(freq.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

}
