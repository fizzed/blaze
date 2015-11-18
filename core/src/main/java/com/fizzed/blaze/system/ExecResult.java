/*
 * Copyright 2015 Fizzed, Inc.
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
package com.fizzed.blaze.system;

import org.zeroturnaround.exec.ProcessResult;

/**
 *
 * @author joelauer
 */
public class ExecResult implements ExecResultSupport {
    
    final private ProcessResult result;

    public ExecResult(ProcessResult result) {
        this.result = result;
    }
    
    @Override
    public Integer exitValue() {
        return this.result.getExitValue();
    }
    
    /**
    @Override
    public String output() {
        return this.result.getOutput().getUTF8();
    }
    
    @Override
    public String output(String charset) {
        return this.result.getOutput().getString(charset);
    }
    
    @Override
    public byte[] outputBytes() {
        return this.result.getOutput().getBytes();
    }
    */
    
}
