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

import static com.fizzed.blaze.util.BlazeUtils.sleep;
import java.util.concurrent.TimeUnit;

public interface Haproxy {
    
    default Haproxy useSudo() {
        return this.sudo(true);
    }
    
    Haproxy sudo(boolean sudo);
    
    Haproxy adminSocket(String path);
    
    String sendCommand(String command);
    
    HaproxyStats getStats();
    
    void setServerState(
        String backend,
        String server,
        String state);
    
    boolean isServerDrained(
        String backend,
        String server);
    
    default boolean waitTillServerDrained(
            String backend,
            String server,
            long timeout,
            long pollEvery,
            TimeUnit timeUnit) {
        
        long started = System.currentTimeMillis();
        long timeoutAt = started + timeUnit.toMillis(timeout);
        int count = 0;
        
        System.out.print("Waiting for " + timeout + " " + timeUnit + " till drained...");
        
        while (timeoutAt > System.currentTimeMillis()) {
            if (count > 0) {
                sleep(pollEvery, timeUnit);
            }
            
            final HaproxyStats stats = this.getStats();
            
            final HaproxyStat stat = stats.findServer(backend, server);
            
            if (stat.getSessionsCurrent() <= 0) {
                System.out.println("ok!");
                return true;
            }
            
            System.out.print("\rWaiting for " + timeout + " " + timeUnit + " till drained... (" + stat.getSessionsCurrent() + " current sessions)");
        }
        
        System.out.println("timed out!");
        return false;
    }
    
    boolean isServerUp(
        String backend,
        String server);
    
}