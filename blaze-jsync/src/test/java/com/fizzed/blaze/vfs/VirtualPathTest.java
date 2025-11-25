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
        assertThat(vp.isAbsolute(), is(false));
        assertThat(vp.isRelative(), is(true));

        vp = VirtualPath.parse("a", true);

        assertThat(vp.getParentPath(), is(nullValue()));
        assertThat(vp.getName(), is("a"));
        assertThat(vp.isDirectory(), is(true));
        assertThat(vp.toFullPath(), is("a"));
        assertThat(vp.isAbsolute(), is(false));
        assertThat(vp.isRelative(), is(true));

        // resolve another relative with it
        VirtualPath vp2 = vp.resolve("b", true, null);

        assertThat(vp2.getParentPath(), is("a"));
        assertThat(vp2.getName(), is("b"));
        assertThat(vp2.isDirectory(), is(true));
        assertThat(vp2.toFullPath(), is("a/b"));
        assertThat(vp2.isAbsolute(), is(false));
        assertThat(vp2.isRelative(), is(true));

        // resolve an absolute with it
        vp2 = vp.resolve("/b", true, null);

        assertThat(vp2.getParentPath(), is(""));
        assertThat(vp2.getName(), is("b"));
        assertThat(vp2.isDirectory(), is(true));
        assertThat(vp2.toFullPath(), is("/b"));
        assertThat(vp2.isAbsolute(), is(true));
        assertThat(vp2.isRelative(), is(false));
    }

    @Test
    public void relativeWindows() {
        VirtualPath vp = VirtualPath.parse("a\\b", true);

        assertThat(vp.getParentPath(), is("a"));
        assertThat(vp.getName(), is("b"));
        assertThat(vp.isDirectory(), is(true));
        assertThat(vp.toFullPath(), is("a/b"));
        assertThat(vp.isAbsolute(), is(false));
        assertThat(vp.isRelative(), is(true));
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

    @Test
    public void absoluteWindows() {
        VirtualPath vp = VirtualPath.parse("C:\\a", true);

        assertThat(vp.getParentPath(), is("C:"));
        assertThat(vp.getName(), is("a"));
        assertThat(vp.isDirectory(), is(true));
        assertThat(vp.toFullPath(), is("C:/a"));
        assertThat(vp.isAbsolute(), is(true));
        assertThat(vp.isRelative(), is(false));
    }

    @Test
    public void normalize() {
        VirtualPath vp;

        vp = VirtualPath.parse("a/./b", true, null);

        assertThat(vp.normalize().toFullPath(), is("a/b"));

        vp = VirtualPath.parse("./b", true, null);

        assertThat(vp.normalize().toFullPath(), is("b"));

        vp = new VirtualPath(null, "b", true, null);

        assertThat(vp.normalize().toFullPath(), is("b"));

        vp = VirtualPath.parse("a/../b", true, null);

        assertThat(vp.normalize().toFullPath(), is("b"));

        /*
        /a/./b/../../c/	/c	. ignored, b popped, a popped.
        //a//b//	/a/b	Multiple slashes (//) collapsed.
                    /../	/	Cannot traverse above root.
        ../a/b	../a/b	Preserves .. in relative paths.
        ./foo	foo	Leading . removed.
        a/b/../c	a/c	b removed by ...
        */
        vp = VirtualPath.parse("/a/./b/../../c/", true, null);

        assertThat(vp.normalize().toFullPath(), is("/c"));

        vp = VirtualPath.parse("//a//b//", true, null);

        assertThat(vp.normalize().toFullPath(), is("/a/b"));

        vp = VirtualPath.parse("/../", true, null);

        assertThat(vp.normalize().toFullPath(), is("/"));

        vp = VirtualPath.parse("../a/b", true, null);

        assertThat(vp.normalize().toFullPath(), is("../a/b"));

        vp = VirtualPath.parse("a/b/../c", true, null);

        assertThat(vp.normalize().toFullPath(), is("a/c"));
    }

    @Test
    public void normalizeWindows() {
        VirtualPath vp;

        vp = VirtualPath.parse("a\\.\\b", true, null);

        assertThat(vp.normalize().toFullPath(), is("a/b"));

        vp = VirtualPath.parse(".\\b", true, null);

        assertThat(vp.normalize().toFullPath(), is("b"));

        vp = new VirtualPath(null, "b", true, null);

        assertThat(vp.normalize().toFullPath(), is("b"));

        vp = VirtualPath.parse("C:/b", true, null);

        assertThat(vp.normalize().toFullPath(), is("C:/b"));
    }

}