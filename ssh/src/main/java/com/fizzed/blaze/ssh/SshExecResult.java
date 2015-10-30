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
package com.fizzed.blaze.ssh;

import com.fizzed.blaze.system.ExecResultSupport;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author joelauer
 */
public class SshExecResult implements ExecResultSupport {
    
    final private Integer exitValue;
    final private ByteArrayOutputStream captureOutputStream;
    
    public SshExecResult(Integer exitValue, ByteArrayOutputStream captureOutputStream) {
        this.exitValue = exitValue;
        this.captureOutputStream = captureOutputStream;
    }
    
    @Override
    public Integer exitValue() {
        return exitValue;
    }
    
    private void verifyOutputCaptured() {
        if (this.captureOutputStream == null) {
            throw new IllegalStateException("Output must be captured before running. Use SshExec.captureOutput() method");
        }
    }
    
    @Override
    public String output() {
        verifyOutputCaptured();
        try {
            return this.captureOutputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    @Override
    public String output(String charset) {
        verifyOutputCaptured();
        try {
            return this.captureOutputStream.toString(charset);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
    
    @Override
    public byte[] outputBytes() {
        verifyOutputCaptured();
        return this.captureOutputStream.toByteArray();
    }
    
}
