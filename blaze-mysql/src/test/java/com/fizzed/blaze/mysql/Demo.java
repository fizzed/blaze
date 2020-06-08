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
package com.fizzed.blaze.mysql;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo {
    static private final Logger log = LoggerFactory.getLogger(Demo.class);
    
    static public void main(String[] args) throws InterruptedException, IOException {
        
        try (MysqlSession mysql = Mysqls.mysqlConnect("mysql://root:test@localhost:3309").run()) {
         
//            mysql.createDatabase("mytest", true);
            
            mysql.dropDatabase("mytest", true);
            
        }
    }
    
}
