/*
 * Copyright 2016 Fizzed, Inc.
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
package com.fizzed.blaze.haproxy.impl;

import com.fizzed.blaze.Systems;
import com.fizzed.blaze.haproxy.Haproxy;
import com.fizzed.blaze.haproxy.HaproxyStat;
import com.fizzed.blaze.haproxy.HaproxyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fizzed.blaze.system.ExecSession;

public class HaproxyImpl implements Haproxy {
    static private final Logger log = LoggerFactory.getLogger(HaproxyImpl.class);
    
    protected final ExecSession execSession;
    protected boolean sudo;
    protected String adminSocket;

    public HaproxyImpl(ExecSession execSession) {
        this.execSession = execSession;
        this.adminSocket = "/run/haproxy/admin.sock";
        this.sudo = false;
    }

    @Override
    public HaproxyImpl sudo(boolean sudo) {
        this.sudo = sudo;
        return this;
    }
    
    @Override
    public HaproxyImpl adminSocket(String adminSocket) {
        this.adminSocket = adminSocket;
        return this;
    }
    
    @Override
    public String sendCommand(
            String command) {

        log.debug("Running haproxy admin command: {}", command);
        
        String result = Systems.execOn(execSession)
            .command("socat")
            .args("stdio", this.adminSocket)
            .sudo(this.sudo)
            .pipeInput(command + "\r\n")
            .runCaptureOutput()
            .asString()
            .trim();
        
        log.trace("Result was:\n{}", result);
        
        if (result.startsWith("Unknown command.")) {
            throw new RuntimeException("Invalid haproxy command!");
        }
        else if (result.startsWith("Require")) {
            // Require 'backend/server'
            throw new RuntimeException("Invalid haproxy command!");
        }
            
        return result;
    }
    
    @Override
    public HaproxyStats getStats() {
        
        // build command
        String command = "show stat";
        
        String s = this.sendCommand(command);
        
        // # pxname,svname,qcur,qmax,scur,smax,slim,stot,bin,bout,dreq,dresp,ereq,econ,eresp,wretr,wredis,status,weight,act,bck,chkfail,chkdown,lastchg,downtime,qlimit,pid,iid,sid,throttle,lbtot,tracked,type,rate,rate_lim,rate_max,check_status,check_code,check_duration,hrsp_1xx,hrsp_2xx,hrsp_3xx,hrsp_4xx,hrsp_5xx,hrsp_other,hanafail,req_rate,req_rate_max,req_tot,cli_abrt,srv_abrt,comp_in,comp_out,comp_byp,comp_rsp,lastsess,last_chk,last_agt,qtime,ctime,rtime,ttime,agent_status,agent_code,agent_duration,check_desc,agent_desc,check_rise,check_fall,check_health,agent_rise,agent_fall,agent_health,addr,cookie,mode,algo,conn_rate,conn_rate_max,conn_tot,intercepted,dcon,dses,wrew,connect,reuse,cache_lookups,cache_hits,srv_icur,src_ilim,qtime_max,ctime_max,rtime_max,ttime_max,
        // stats,FRONTEND,,,4,5,262123,434,783966,50230846,0,0,430,,,,,OPEN,,,,,,,,,1,2,0,,,,0,0,0,2,,,,0,1727,0,430,0,0,,0,5,2157,,,0,0,0,0,,,,,,,,,,,,,,,,,,,,,http,,0,2,434,1727,0,0,0,,,0,0,,,,,,,

        // split into lines
        String[] lines = s.split("\n");
        
        String header = lines[0];
        
        if (!header.startsWith("# ")) {
            throw new RuntimeException("Invalid header line");
        }
        
        String[] keys = lines[0].substring(2).split(",");

        final HaproxyStats stats = new HaproxyStats();
        
        for (int i = 1; i < lines.length; i++) {
            String[] values = lines[i].split(",");
            
            if (values.length > keys.length) {
                throw new RuntimeException("Invalid value field count " + values.length + " > header count " + keys.length);
            }
            
            HaproxyStat stat = new HaproxyStat();
            
            for (int j = 0; j < values.length; j++) {
                stat.put(keys[j], values[j]);
            }
            
            stats.add(stat);
        }
        
        return stats;
    }
    
    @Override
    public void setServerState(
            String backend,
            String server,
            String state) {
        
        // build command
        String command = "set server " + backend + "/" + server + " state " + state;
        
        this.sendCommand(command);
    }
    
    @Override
    public boolean isServerDrained(
            String backend,
            String server) {
        
        final HaproxyStats stats = this.getStats();
            
        // find the backend server stats we want
        final Long sessionCurrent = stats.findServer(backend, server).getSessionsCurrent();
        
        return sessionCurrent <= 0;
    }
    
    @Override
    public boolean isServerUp(
            String backend,
            String server) {
        
        final HaproxyStats stats = this.getStats();
            
        // find the backend server stats we want
        final String s = stats.findServer(backend, server).getStatus();
        
        return "UP".equalsIgnoreCase(s);
    }
    
}