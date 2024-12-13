package com.fizzed.blaze;

import com.fizzed.blaze.archive.Unarchive;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Archives {

    static public Unarchive unarchive(Path file) {
        return new Unarchive(Contexts.currentContext(), file);
    }

    static public Unarchive unarchive(File file) {
        return new Unarchive(Contexts.currentContext(), file.toPath());
    }

    static public Unarchive unarchive(String file) {
        return new Unarchive(Contexts.currentContext(), Paths.get(file));
    }

}