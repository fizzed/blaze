package com.fizzed.blaze.maven;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HelloDemo {
    static private final Logger log = LoggerFactory.getLogger(HelloDemo.class);
    
    static public void main(String[] args) throws InterruptedException, IOException {
        System.out.println("Hello World!");
    }
    
}
