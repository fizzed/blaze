package com.fizzed.blaze.vfs.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

class ChecksumsTest {

    @Test
    public void parsePosixCksumOutput() throws Exception {
        final String output = "\n" +
            "2172985434 2414902 blaze.jar\n" +
            "2050415828 8389 pom.xml\n" +
            "2392810308 18263 README.md";

        final List<Checksums.HashEntry> entries = Checksums.parsePosixCksumOutput(output);

        assertThat(entries, hasSize(3));
        assertThat(entries.get(0).getCksum(), is(2172985434L));
        assertThat(entries.get(0).getFile(), is("blaze.jar"));
        assertThat(entries.get(2).getCksum(), is(2392810308L));
        assertThat(entries.get(2).getFile(), is("README.md"));
    }

    @Test
    public void parsePosixMd5Output() throws Exception {
        final String output = "\n" +
            "84e7baeaf93b7ddd9b13ca1187a32c26  blaze.jar\n" +
            "C7c338884f3adaa204cabc6f81cdfe96  pom.xml\n" +
            "E91b799c968c5f55bef214d296e35312  README.md\n";

        final List<Checksums.HashEntry> entries = Checksums.parsePosixHashOutput(output);

        assertThat(entries, hasSize(3));
        assertThat(entries.get(0).getHash(), is("84e7baeaf93b7ddd9b13ca1187a32c26"));
        assertThat(entries.get(0).getFile(), is("blaze.jar"));
        assertThat(entries.get(2).getHash(), is("e91b799c968c5f55bef214d296e35312"));
        assertThat(entries.get(2).getFile(), is("README.md"));
    }

    @Test
    public void parsePosixSha1Output() throws Exception {
        final String output = "\n" +
            "F4aa081eb0e0a01ac232adce116c9c76d6bf00f8  blaze.jar\n" +
            "68c33fb863cea757b998475bef61ec08aa3442f2  pom.xml\n" +
            "2F44473b039df1ab856a0051507c82efd09ee6c2  README.md";

        final List<Checksums.HashEntry> entries = Checksums.parsePosixHashOutput(output);

        assertThat(entries, hasSize(3));
        assertThat(entries.get(0).getHash(), is("f4aa081eb0e0a01ac232adce116c9c76d6bf00f8"));
        assertThat(entries.get(0).getFile(), is("blaze.jar"));
        assertThat(entries.get(2).getHash(), is("2f44473b039df1ab856a0051507c82efd09ee6c2"));
        assertThat(entries.get(2).getFile(), is("README.md"));
    }

    @Test
    public void parsePowershellHashFileOutput() throws Exception {
        final String output = "\n" +
            "\n" +
            "Hash : 7255E704ED4FB4DB41E11EE677715AD8\n" +
            "Path : C:\\Users\\builder\\test-sync\\.blaze\\blaze.java\n" +
            "\n" +
            "Hash : 901B64F7BAC049340CEB19F5602B975C\n" +
            "Path : C:\\Users\\builder\\test-sync\\.blaze\\pom.xml\n" +
            "\n" +
            "\n" +
            "\n";

        final List<Checksums.HashEntry> entries = Checksums.parsePowershellHashFileOutput(output);

        assertThat(entries, hasSize(2));
        assertThat(entries.get(0).getHash(), is("7255E704ED4FB4DB41E11EE677715AD8".toLowerCase()));
        assertThat(entries.get(0).getFile(), is("C:\\Users\\builder\\test-sync\\.blaze\\blaze.java"));
        assertThat(entries.get(1).getHash(), is("901B64F7BAC049340CEB19F5602B975C".toLowerCase()));
        assertThat(entries.get(1).getFile(), is("C:\\Users\\builder\\test-sync\\.blaze\\pom.xml"));
    }

}