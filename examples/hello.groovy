#!/usr/bin/env blaze
import com.fizzed.blaze.Task

@Task(order=2, value="Prints hello world")
def main() {
    println("Hello World!")
}

@Task(order=1, value="Prints java version")
public void version() {
    println(System.getProperty("java.version"));
}