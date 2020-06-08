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

import com.fizzed.blaze.SecureShells;
import com.fizzed.blaze.docker.DockerSession;
import com.fizzed.blaze.docker.Dockers;
import static com.fizzed.blaze.haproxy.Haproxys.haproxy;
import com.fizzed.blaze.ssh.SshSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo {
    static private final Logger log = LoggerFactory.getLogger(Demo.class);
    
    static public void main(String[] args) throws InterruptedException {
        
        SshSession target = SecureShells.sshConnect("ssh://us-chi1-stg-slb1").run();
        boolean sudo = true;

        // docker build -t blaze-haproxy:latest -f src/main/docker/Dockerfile .
        // docker run -it -d --name blaze-haproxy blaze-haproxy:latest
//        DockerSession target = Dockers.dockerConnect("docker://blaze-haproxy").run();
//        boolean sudo = false;

        
        Haproxy haproxy = haproxy(target).sudo(sudo);
        
        HaproxyStats stats = haproxy.getStats();
        
        log.debug("{}", stats);
        
        for (HaproxyStat stat : stats) {
            log.debug("{}/{} -> {} (weight={})",
                stat.getPrimaryName(), stat.getServerName(), stat.getStatus(), stat.getWeight());
        }
        
        haproxy.setServerState("stg-be-carbon", "us-chi1-stg-service1", "drain");
        
        log.debug("{}", haproxy.isServerDrained("stg-be-carbon", "us-chi1-stg-service1"));
        
        haproxy.setServerState("stg-be-carbon", "us-chi1-stg-service1", "ready");
        
        Thread.sleep(2000);
        
        log.debug("{}", haproxy.isServerUp("stg-be-carbon", "us-chi1-stg-service1"));
    }
    
}
