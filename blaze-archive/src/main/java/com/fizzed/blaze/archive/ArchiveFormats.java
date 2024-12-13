package com.fizzed.blaze.archive;

import java.util.ArrayList;
import java.util.List;

public class ArchiveFormats {

    static public final List<ArchiveFormat> ALL;
    static {
        ALL = new ArrayList<>();
        // build our compatability list, then add in any special extra cases (e.g. extensions)
        // add all the archives in first
        for (Archiver archiver : Archiver.values()) {
            ALL.add(new ArchiveFormat(archiver, null, archiver.getExtension()));
        }

        // .tar archives can simply be compressed with any compression we support
        for (Compressor compressor : Compressor.values()) {
            ALL.add(new ArchiveFormat(Archiver.TAR, compressor, Archiver.TAR.getExtension() + compressor.getExtension()));
        }

        // .tgz special case
        ALL.add(new ArchiveFormat(Archiver.TAR, Compressor.GZ, ".tgz"));

        // all compressors too
        for (Compressor compressor : Compressor.values()) {
            ALL.add(new ArchiveFormat(null, compressor, compressor.getExtension()));
        }
    }

/*
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
    }*/
    
}