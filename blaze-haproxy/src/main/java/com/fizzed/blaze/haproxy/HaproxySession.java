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
package com.fizzed.blaze.haproxy;

import com.fizzed.blaze.util.DurationFormatter;
import com.fizzed.blaze.util.TerminalLine;
import com.fizzed.blaze.util.WaitFor;

public interface HaproxySession {
    
    String sendCommand(String command);
    
    HaproxyInfo getInfo();
    
    HaproxyStats getStats();
    
    void setServerState(
        String backend,
        String server,
        String state);
    
    boolean isServerDrained(
        String backend,
        String server);
    
    boolean isServerUp(
        String backend,
        String server);
    
    default boolean waitForServerDrained(
            String backend,
            String server,
            long timeoutMillis,
            long pollEveryMillis) throws InterruptedException {
        
        final TerminalLine term = new TerminalLine();

        final boolean success = new WaitFor(progress -> {
                HaproxyStat stat = this.getStats().findServer(backend, server);

                term.update("  sessions=%d (waiting %s/%s, attempt %d)",
                    stat.getSessionsCurrent(),
                    DurationFormatter.format(progress.getTimer().elapsed()),
                    DurationFormatter.format(progress.getTimeout()),
                    progress.getAttempt());

                return stat.getSessionsCurrent() <= 0;
            })
           .await(timeoutMillis, pollEveryMillis);

        term.done(" (" + (success ? "ok" : "failed") + ")");
            
        return success;
    }
    
    default boolean waitForServerUp(
            String backend,
            String server,
            long timeoutMillis,
            long pollEveryMillis) throws InterruptedException {
        
        final TerminalLine term = new TerminalLine();

        final boolean success = new WaitFor(progress -> {
                HaproxyStat stat = this.getStats().findServer(backend, server);
                            
                term.update("  status=%s, weight=%d (waiting %s/%s, attempt %d)",
                    stat.getStatus(), stat.getWeight(),
                   DurationFormatter.format(progress.getTimer().elapsed()),
                    DurationFormatter.format(progress.getTimeout()),
                    progress.getAttempt());

                return "up".equalsIgnoreCase(stat.getStatus());
            })
           .await(timeoutMillis, pollEveryMillis);

        term.done(" (" + (success ? "ok" : "failed") + ")");
            
        return success;
    }
    
}