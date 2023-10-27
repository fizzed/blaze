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
package com.fizzed.blaze.kotlin;

import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity;
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation;
import org.jetbrains.kotlin.cli.common.messages.MessageCollector;
import org.slf4j.Logger;

/**
 * Collects messages from Kotlin compilation.
 * 
 * @author joelauer
 */
public class CountingSLF4JMessageCollector implements MessageCollector {

    private final Logger logger;
    private final AtomicInteger errors;
    private final AtomicInteger warnings;
    
    public CountingSLF4JMessageCollector(Logger logger) {
        this.logger = logger;
        this.errors = new AtomicInteger(0);
        this.warnings = new AtomicInteger(0);
    }

    public int getErrors() {
        return errors.get();
    }

    public int getWarnings() {
        return warnings.get();
    }
    
    /*@Override
    public void report(CompilerMessageSeverity severity, String message, CompilerMessageLocation location) {
        switch (severity) {
            case INFO:
                this.logger.info("{} @ {}", message, location);
                break;
            case WARNING:
                this.warnings.incrementAndGet();
                this.logger.warn("{} @ {}", message, location);
                break;
            case ERROR:
            case EXCEPTION:
                this.errors.incrementAndGet();
                this.logger.error("{} @ {}", message, location);
                break;
        }
    }*/

    @Override
    public void clear() {

    }

    @Override
    public boolean hasErrors() {
        return false;
    }

    @Override
    public void report(@NotNull CompilerMessageSeverity severity, @NotNull String message, @Nullable CompilerMessageSourceLocation location) {
        switch (severity) {
            case INFO:
                this.logger.info("{} @ {}", message, location);
                break;
            case WARNING:
                this.warnings.incrementAndGet();
                this.logger.warn("{} @ {}", message, location);
                break;
            case ERROR:
            case EXCEPTION:
                this.errors.incrementAndGet();
                this.logger.error("{} @ {}", message, location);
                break;
        }
    }
}
