package com.fizzed.blaze.postoffice;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.internal.IntRangeHelper;
import com.fizzed.blaze.util.*;
import okhttp3.*;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Mail extends Action<Mail.Result,Integer> implements VerbosityMixin<Mail> {

    static public class Result extends com.fizzed.blaze.core.Result<Mail,Integer,Result> {

        Result(Mail action, Integer value) {
            super(action, value);
        }

    }

    private final VerboseLogger log;


    public Mail(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
    }

    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    @Override
    protected Result doRun() throws BlazeException {

        return null;
    }

}
