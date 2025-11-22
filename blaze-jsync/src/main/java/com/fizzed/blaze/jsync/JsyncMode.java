package com.fizzed.blaze.jsync;

public enum JsyncMode {

    /**
     * Result: Target / SourceName / ...
     * * BEHAVIOR:
     * - Directory Source: Creates a directory named 'source' INSIDE target.
     * - File Source: Copies the file INSIDE target (retaining filename).
     * - Requirement: Target must be a directory.
     */
    NEST,

    /**
     * Result: Target / ...
     * * BEHAVIOR:
     * - Directory Source: Dumps files directly INTO target (no sub-folder).
     * - File Source: Copies file AS target (Renames/Overwrites target).
     * - Requirement:
     * If Source is Dir -> Target must be Dir.
     * If Source is File -> Target must be File path.
     */
    MERGE;

}