package com.fizzed.blaze.jsync;

public class JsyncStatsModified {

    private final boolean ownership;
    private final boolean permissions;
    private final boolean timestamps;

    JsyncStatsModified(boolean ownership, boolean permissions, boolean timestamps) {
        this.ownership = ownership;
        this.permissions = permissions;
        this.timestamps = timestamps;
    }

    public boolean isOwnership() {
        return ownership;
    }

    public boolean isPermissions() {
        return permissions;
    }

    public boolean isTimestamps() {
        return timestamps;
    }

    public boolean isAny() {
        return ownership || permissions || timestamps;
    }

}