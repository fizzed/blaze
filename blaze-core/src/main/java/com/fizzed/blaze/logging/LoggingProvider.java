package com.fizzed.blaze.logging;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

import java.util.Deque;
import java.util.Map;

public class LoggingProvider implements SLF4JServiceProvider {

    // --- Singleton instances ---
    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;

    /**
     * This is the "boot" method for the logging system.
     */
    @Override
    public void initialize() {
        loggerFactory = new LoggerFactory();
        markerFactory = new BasicMarkerFactory();
        // We'll use a no-op MDC adapter for this simple implementation
        mdcAdapter = new NopMDCAdapter();
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0";
    }

    // --- A simple No-Op MDC Adapter ---
    // (MDC is for mapped diagnostic context, e.g., tracking a user ID
    // across all log statements in a thread)
    private static class NopMDCAdapter implements MDCAdapter {
        @Override
        public void put(String key, String val) {
        }

        @Override
        public String get(String key) {
            return null;
        }

        @Override
        public void remove(String key) {
        }

        @Override
        public void clear() {
        }

        @Override
        public Map<String, String> getCopyOfContextMap() {
            return null;
        }

        @Override
        public void setContextMap(Map<String, String> contextMap) {
        }

        @Override
        public void pushByKey(String s, String s1) {
        }

        @Override
        public String popByKey(String s) {
            return "";
        }

        @Override
        public Deque<String> getCopyOfDequeByKey(String s) {
            return null;
        }

        @Override
        public void clearDequeByKey(String s) {

        }
    }

}