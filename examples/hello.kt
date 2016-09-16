#!/usr/bin/env blaze

import com.fizzed.blaze.Task

class hello {

    @Task(order=2, value="Prints hello world")
    fun main() {
        System.out.println("Hello World!");
    }
    
    @Task(order=1, value="Prints java version")
    fun version() {
        System.out.println(System.getProperty("java.version"));
    }

}