package com.fizzed.blaze.archive;

import java.nio.file.Path;

public class ArchiveHelper {

    static public boolean isArchived(Path file) {
        return archiveInfo(file) != null;
    }

    static public ArchiveInfo archiveInfo(Path file) {
        return archiveInfo(file.getFileName().toString());
    }

    static public ArchiveInfo archiveInfo(String archivedName) {
        final String n = archivedName.toLowerCase();

        // find the longest matching extension
        String matchedExtension = null;
        ArchiveFormat matchedFormat = null;

        for (ArchiveFormat af : ArchiveFormats.ALL) {
            for (String ext : af.getExtensions()) {
                if (n.endsWith(ext) && (matchedExtension == null || ext.length() > matchedExtension.length())) {
                    matchedExtension = ext;
                    matchedFormat = af;
                }
            }
        }

        if (matchedExtension == null) {
            return null;
        }

        // build out the info we need
        String unarchivedName = archivedName.substring(0, archivedName.length()-matchedExtension.length());

        return new ArchiveInfo(matchedFormat.getArchiver(), matchedFormat.getCompressor(), archivedName, unarchivedName);
    }

    /**
     * Strips a path from the front of an entry name, flattening it. For example, an entry name of sample/a/b.txt
     * would become a/b.txt if componentCount=1 and b.txt if componentCount=2 or 3, etc.
     * @param entryName
     * @param componentCount
     * @return
     */
    static public String[] stripComponents(String entryName, int componentCount) {
        final String[] result = new String[2];

        int stripPos = -1;
        if (componentCount > 0) {
            for (int i = 0; i < componentCount; i++) {
                int slashPos = entryName.indexOf('/', stripPos);
                if (slashPos <= stripPos) {
                    break;      // done finding components
                }
                stripPos = slashPos + 1;
            }
        }

        if (stripPos < 0) {
            result[0] = entryName;
            result[1] = null;
        } else {
            result[0] = entryName.substring(stripPos);
            result[1] = entryName.substring(0, stripPos);
        }

        return result;
    }

    static public String getCommonsCompressorName(Compressor compressor) {
        switch (compressor) {
            case GZ:
                return "gz";
            case BZ2:
                return "bzip2";
            case XZ:
                return "xz";
            case ZSTD:
                return "zstd";
            default:
                throw new IllegalArgumentException("Unable to map compressor " + compressor + " to an apache commons compressor name (was it not added?)");
        }
    }

    static public String getCommonsArchiverName(Archiver archiver) {
        switch (archiver) {
            case TAR:
                return "tar";
            case ZIP:
                return "zip";
            case SEVENZ:
                return "7z";
            default:
                throw new IllegalArgumentException("Unable to map archiver " + archiver + " to an apache commons archiver name (was it not added?)");
        }
    }

}