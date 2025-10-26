/*
 * Copyright 2020 Fizzed, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fizzed.blaze.util;

import java.util.concurrent.TimeUnit;

public class DurationFormatter {
 
    static public String format(double millis) {
        return format((long)millis);
    }
    
    static public String format(long millis) {
        int seconds = (int) (millis / 1000) % 60 ;
        int minutes = (int) ((millis / (1000*60)) % 60);
        int hours   = (int) ((millis / (1000*60*60)) % 24);
//        int days    = (int) (millis / (1000*60*60*24));

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    /**
     * Formats a duration in milliseconds into a compact, human-readable string.
     *
     * @param millis The duration in milliseconds.
     * @return A compact string representation (e.g., "12 ms", "1.5s", "2m 10s").
     */
    public static String formatShort(long millis) {
        if (millis < 0) {
            // Or throw an IllegalArgumentException, depending on desired behavior
            return "N/A";
        }
        if (millis == 0) {
            return "0 ms";
        }

        // Using Java 8's TimeUnit for clarity
        final long DAY = TimeUnit.DAYS.toMillis(1);
        final long HOUR = TimeUnit.HOURS.toMillis(1);
        final long MINUTE = TimeUnit.MINUTES.toMillis(1);
        final long SECOND = TimeUnit.SECONDS.toMillis(1);

        if (millis >= DAY) {
            long days = millis / DAY;
            long hours = (millis % DAY) / HOUR;
            return String.format("%dd %dh", days, hours);
        }
        if (millis >= HOUR) {
            long hours = millis / HOUR;
            long minutes = (millis % HOUR) / MINUTE;
            return String.format("%dh %dm", hours, minutes);
        }
        if (millis >= MINUTE) {
            long minutes = millis / MINUTE;
            long seconds = (millis % MINUTE) / SECOND;
            return String.format("%dm %ds", minutes, seconds);
        }
        if (millis >= SECOND) {
            // Show one decimal place for seconds
            return String.format("%.1fs", millis / 1000.0);
        }

        // Less than a second
        return String.format("%d ms", millis);
    }
    
}