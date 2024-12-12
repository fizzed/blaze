package com.fizzed.blaze.archive;

import java.util.List;

import static java.util.Arrays.asList;

public class ArchiveFormats {

    static public final List<ArchiveFormat> ALL = asList(
        new ArchiveFormat("zip", null, ".zip"),
        new ArchiveFormat("tar", null, ".tar"),
        new ArchiveFormat("tar", "gz", ".tar.gz", ".tgz"),
        new ArchiveFormat("tar", "bzip2", ".tar.bz2"),
        new ArchiveFormat("tar", "xz", ".tar.xz"),
        new ArchiveFormat("tar", "zstd", ".tar.zst"),
        new ArchiveFormat(null, "gz", ".gz"),
        new ArchiveFormat("7z", null, ".7z")
    );

    static public ArchiveFormat detectByFileName(String fileName) {
        final String n = fileName.toLowerCase();

        // find the longest matching extension
        String matchedExtension = null;
        ArchiveFormat matchedFormat = null;

        for (ArchiveFormat af : ALL) {
            for (String ext : af.getExtensions()) {
                if (n.endsWith(ext) && (matchedExtension == null || ext.length() > matchedExtension.length())) {
                    matchedExtension = ext;
                    matchedFormat = af;
                }
            }
        }

        return matchedFormat;
    }
    
}