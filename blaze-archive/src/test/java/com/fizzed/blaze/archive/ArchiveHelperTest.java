package com.fizzed.blaze.archive;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ArchiveHelperTest {

    @Test
    public void stripComponents() {
        String[] result;

        result = ArchiveHelper.stripComponents("b.txt", 0);
        assertThat(result[0], is("b.txt"));
        assertThat(result[1], is(nullValue()));

        result = ArchiveHelper.stripComponents("b.txt", 1);
        assertThat(result[0], is("b.txt"));
        assertThat(result[1], is(nullValue()));

        result = ArchiveHelper.stripComponents("b.txt", 2);
        assertThat(result[0], is("b.txt"));
        assertThat(result[1], is(nullValue()));


        result = ArchiveHelper.stripComponents("sample/b.txt", 0);
        assertThat(result[0], is("sample/b.txt"));
        assertThat(result[1], is(nullValue()));

        result = ArchiveHelper.stripComponents("sample/b.txt", 1);
        assertThat(result[0], is("b.txt"));
        assertThat(result[1], is("sample/"));

        result = ArchiveHelper.stripComponents("sample/b.txt", 2);
        assertThat(result[0], is("b.txt"));
        assertThat(result[1], is("sample/"));


        result = ArchiveHelper.stripComponents("sample/a/b.txt", 0);
        assertThat(result[0], is("sample/a/b.txt"));
        assertThat(result[1], is(nullValue()));

        result = ArchiveHelper.stripComponents("sample/a/b.txt", 1);
        assertThat(result[0], is("a/b.txt"));
        assertThat(result[1], is("sample/"));

        result = ArchiveHelper.stripComponents("sample/a/b.txt", 2);
        assertThat(result[0], is("b.txt"));
        assertThat(result[1], is("sample/a/"));

        result = ArchiveHelper.stripComponents("sample/a/b.txt", 3);
        assertThat(result[0], is("b.txt"));
        assertThat(result[1], is("sample/a/"));
    }

}