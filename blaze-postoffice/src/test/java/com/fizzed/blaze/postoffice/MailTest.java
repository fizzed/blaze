package com.fizzed.blaze.postoffice;

import com.fizzed.blaze.Config;
import com.fizzed.blaze.internal.ConfigHelper;
import com.fizzed.blaze.internal.ContextImpl;
import org.junit.Before;

import java.nio.file.Paths;

import static org.mockito.Mockito.spy;

public class MailTest {

    private Config config;
    private ContextImpl context;

    @Before
    public void setup() throws Exception {
        this.config = ConfigHelper.create(null);
        this.context = spy(new ContextImpl(null, null, Paths.get("blaze.java"), config));
    }

}