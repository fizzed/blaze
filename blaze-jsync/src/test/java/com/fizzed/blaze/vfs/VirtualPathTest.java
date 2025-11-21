package com.fizzed.blaze.vfs;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

class VirtualPathTest {

    @Test
    public void relative() {
        VirtualPath vp = new VirtualPath(null, "a", false, null);

        assertThat(vp.getParentPath(), is(nullValue()));
        assertThat(vp.getName(), is("a"));
        assertThat(vp.isDirectory(), is(false));
        assertThat(vp.toFullPath(), is("a"));
        assertThat(vp.toString(), is("a"));
        assertThat(vp.isAbsolute(), is(false));
        assertThat(vp.isRelative(), is(true));

        vp = VirtualPath.parse("a", true);

        assertThat(vp.getParentPath(), is(nullValue()));
        assertThat(vp.getName(), is("a"));
        assertThat(vp.isDirectory(), is(true));
        assertThat(vp.toFullPath(), is("a"));
        assertThat(vp.toString(), is("a"));
        assertThat(vp.isAbsolute(), is(false));
        assertThat(vp.isRelative(), is(true));

        // resolve another relative with it
        VirtualPath vp2 = vp.resolve("b", true, null);

        assertThat(vp2.getParentPath(), is("a"));
        assertThat(vp2.getName(), is("b"));
        assertThat(vp2.isDirectory(), is(true));
        assertThat(vp2.toFullPath(), is("a/b"));
        assertThat(vp2.toFullPath(), is("a/b"));
        assertThat(vp2.isAbsolute(), is(false));
        assertThat(vp2.isRelative(), is(true));

        // resolve an absolute with it
        vp2 = vp.resolve("/b", true, null);

        assertThat(vp2.getParentPath(), is(""));
        assertThat(vp2.getName(), is("b"));
        assertThat(vp2.isDirectory(), is(true));
        assertThat(vp2.toFullPath(), is("/b"));
        assertThat(vp2.toFullPath(), is("/b"));
        assertThat(vp2.isAbsolute(), is(true));
        assertThat(vp2.isRelative(), is(false));
    }

    @Test
    public void absolute() {
        VirtualPath vp = new VirtualPath("", "a", false, null);

        assertThat(vp.getParentPath(), is(""));
        assertThat(vp.getName(), is("a"));
        assertThat(vp.isDirectory(), is(false));
        assertThat(vp.toFullPath(), is("/a"));
        assertThat(vp.toString(), is("/a"));
        assertThat(vp.isAbsolute(), is(true));
        assertThat(vp.isRelative(), is(false));

        vp = VirtualPath.parse("/a", true);

        assertThat(vp.getParentPath(), is(""));
        assertThat(vp.getName(), is("a"));
        assertThat(vp.isDirectory(), is(true));
        assertThat(vp.toFullPath(), is("/a"));
        assertThat(vp.toString(), is("/a"));
        assertThat(vp.isAbsolute(), is(true));
        assertThat(vp.isRelative(), is(false));

        // resolve another relative with it
        VirtualPath vp2 = vp.resolve("b", true, null);

        assertThat(vp2.getParentPath(), is("/a"));
        assertThat(vp2.getName(), is("b"));
        assertThat(vp2.isDirectory(), is(true));
        assertThat(vp2.toFullPath(), is("/a/b"));
        assertThat(vp2.toFullPath(), is("/a/b"));
        assertThat(vp2.isAbsolute(), is(true));
        assertThat(vp2.isRelative(), is(false));

        // resolve another absolute with it
        vp2 = vp.resolve("/b", true, null);

        assertThat(vp2.getParentPath(), is(""));
        assertThat(vp2.getName(), is("b"));
        assertThat(vp2.isDirectory(), is(true));
        assertThat(vp2.toFullPath(), is("/b"));
        assertThat(vp2.toFullPath(), is("/b"));
        assertThat(vp2.isAbsolute(), is(true));
        assertThat(vp2.isRelative(), is(false));
    }

}