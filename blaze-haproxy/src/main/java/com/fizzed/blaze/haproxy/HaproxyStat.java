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

import java.util.LinkedHashMap;
import static java.util.Optional.ofNullable;

public class HaproxyStat extends LinkedHashMap<String,String> {
    
    //     {pxname=stats, svname=FRONTEND, qcur=, qmax=, scur=2, smax=3, slim=262121,
    //    stot=3332, bin=6510052, bout=586850229, dreq=0, dresp=0, ereq=3329, econ=, eresp=,
    //    wretr=, wredis=, status=OPEN, weight=, act=, bck=, chkfail=, chkdown=, lastchg=,
    //    downtime=, qlimit=, pid=1, iid=2, sid=0, throttle=, lbtot=, tracked=, type=0, rate=0,
    //    rate_lim=0, rate_max=2, check_status=, check_code=, check_duration=, hrsp_1xx=0,
    //    hrsp_2xx=13320, hrsp_3xx=0, hrsp_4xx=3329, hrsp_5xx=0, hrsp_other=0, hanafail=,
    //    req_rate=0, req_rate_max=3, req_tot=16649, cli_abrt=, srv_abrt=, comp_in=0,
    //    comp_out=0, comp_byp=0, comp_rsp=0, lastsess=, last_chk=, last_agt=, qtime=, ctime=,
    //    rtime=, ttime=, agent_status=, agent_code=, agent_duration=, check_desc=, agent_desc=,
    //    check_rise=, check_fall=, check_health=, agent_rise=, agent_fall=, agent_health=, 
    //    addr=, cookie=, mode=http, algo=, conn_rate=0, conn_rate_max=2, conn_tot=3332,
    //    intercepted=13320, dcon=0, dses=0, wrew=0, connect=, reuse=, cache_lookups=0, cache_hits=0}
    
    public String getString(String key) {
        return ofNullable(this.get(key))
            .map(v -> v.trim().isEmpty() ? null : v)
            .filter(v -> v != null)
            .orElse(null);
    }
    
    public Long getLong(String key) {
        return ofNullable(this.getString(key))
            .map(v -> Long.valueOf(v))
            .orElse(null);
    }

    public String getPrimaryName() {
        return this.get("pxname");
    }
    
    public String getServerName() {
        return this.get("svname");
    }
    
    public String getStatus() {
        return this.get("status");
    }
    
    public Long getSessionsCurrent() {
        return this.getLong("scur");
    }
    
    public Long getSessionsMax() {
        return this.getLong("smax");
    }
    
    public Long getBytesIn() {
        return this.getLong("bin");
    }
    
    public Long getBytesOut() {
        return this.getLong("bout");
    }
    
    public Long getWeight() {
        return this.getLong("weight");
    }
    
}