package com.fizzed.blaze.jsync;

public class JsyncPathChanges {

    final private boolean missing;
    final private boolean size;
    final private boolean timestamps;
    final private boolean permissions;
    final private boolean ownership;
    final private Boolean checksum;               // this is not always determined since its expensive

    public JsyncPathChanges(boolean missing, boolean size, boolean timestamps, boolean permissions, boolean ownership, Boolean checksum) {
        this.missing = missing;
        this.size = size;
        this.timestamps = timestamps;
        this.permissions = permissions;
        this.ownership = ownership;
        this.checksum = checksum;
    }

    public boolean isMissing() {
        return missing;
    }

    public boolean isSize() {
        return size;
    }

    public boolean isTimestamps() {
        return timestamps;
    }

    public boolean isPermissions() {
        return permissions;
    }

    public boolean isOwnership() {
        return ownership;
    }

    public boolean hasChecksum() {
        return checksum != null;
    }

    public Boolean getChecksum() {
        return checksum;
    }

    // helpers

    /**
     * Determines if the file "content" should be synced.
     * If either missing, or the size changed, or the timestamp changed (and we aren't ignoring that), or the checksum is
     * known and that changed, then we need to send the content.
     * @param ignoreTimes
     * @return
     */
    public boolean isContentModified(boolean ignoreTimes) {
        return this.missing
            || this.size
            || (checksum != null && checksum);
    }

    /**
     * Determines if the file sync should be deferred till later.
     * If the file isn't missing, the size is the same, we're ignoring timestamps
     * @return
     */
    public boolean isDeferredProcessing(boolean ignoreTimes) {
        return !this.missing
            && !this.size
            && (ignoreTimes || this.timestamps)     // if we are ignoring timestamps or the timestamps have changed
            && checksum == null;
    }

    public boolean isStatModified() {
        return this.missing
            || this.timestamps
            || this.permissions
            || this.ownership;
    }

    @Override
    public String toString() {
        return
            "missing=" + missing +
            ", size=" + size +
            ", times=" + timestamps +
            ", perms=" + permissions +
            ", ownership=" + ownership +
            ", checksum=" + checksum;
    }
}