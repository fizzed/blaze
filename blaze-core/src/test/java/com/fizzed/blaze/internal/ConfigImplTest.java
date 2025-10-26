package com.fizzed.blaze.internal;

import com.fizzed.blaze.Config;
import com.fizzed.crux.util.Maps;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ConfigImplTest {

    @Test
    public void configString() throws Exception {
        Config config = ConfigHelper.create(false, null, Maps.mapOf("v", "1"));

        assertThat(config.value("v").get(), is("1"));

        try {
            config.value("x").get();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void configConverted() throws Exception {
        Config config = ConfigHelper.create(false, null, Maps.mapOf("v", "1"));

        assertThat(config.value("v", int.class).get(), is(1));
        assertThat(config.value("v", Integer.class).get(), is(1));
        assertThat(config.value("v", Long.class).get(), is(1L));
        assertThat(config.value("v", Double.class).get(), is(1d));

        try {
            config.value("x", int.class).get();
            fail();
        } catch (NoSuchElementException e) {
            // expected
        }
    }

    @Test
    public void configStringListTypesafeLibMethod() throws Exception {
        Config config = ConfigHelper.create(false, null, Maps.mapOf("v.0", "1", "v.1", "2"));

        assertThat(config.valueList("v").get(), hasItems("1", "2"));
    }

    @Test
    public void configConvertedListTypesafeLibMethod() throws Exception {
        Config config = ConfigHelper.create(false, null, Maps.mapOf("v.0", "1", "v.1", "2"));

        assertThat(config.valueList("v", int.class).get(), hasItems(1, 2));
        assertThat(config.valueList("v", Integer.class).get(), hasItems(1, 2));
    }

    @Test
    public void configStringListBlazeMethod() throws Exception {
        Config config = ConfigHelper.create(false, null, Maps.mapOf("v", " 1 , 2"));

        assertThat(config.valueList("v").get(), hasItems("1", "2"));
    }

    @Test
    public void configConvertedListBlazeMethod() throws Exception {
        Config config = ConfigHelper.create(false, null, Maps.mapOf("v", " 1 , 2"));

        assertThat(config.valueList("v", int.class).get(), hasItems(1, 2));
        assertThat(config.valueList("v", Integer.class).get(), hasItems(1, 2));
        assertThat(config.valueList("v", Long.class).get(), hasItems(1L, 2L));
        assertThat(config.valueList("v", Short.class).get(), hasItems((short)1, (short)2));
    }

    @Test
    public void configFlag() throws Exception {
        // these mimic how command line arguments are set as "flags" and in a config file you'd do something like "flag = true"
        Config config = ConfigHelper.create(false, null, Maps.mapOf("flag", "true", "this.is.a.more.complex.key", "true"));

        assertThat(config.flag("flag").orElse(false), is(true));
        assertThat(config.flag("flag1").orElse(false), is(false));
        assertThat(config.flag("this.is.a.more.complex.key").orElse(false), is(true));
    }

}