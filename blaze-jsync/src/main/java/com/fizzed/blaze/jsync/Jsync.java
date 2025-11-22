package com.fizzed.blaze.jsync;

import com.fizzed.blaze.Context;
import com.fizzed.blaze.core.Action;
import com.fizzed.blaze.core.BlazeException;
import com.fizzed.blaze.core.ProgressMixin;
import com.fizzed.blaze.core.VerbosityMixin;
import com.fizzed.blaze.util.ValueHolder;
import com.fizzed.blaze.util.VerboseLogger;

public class Jsync extends Action<Jsync.Result,Boolean> implements VerbosityMixin<Jsync>, ProgressMixin<Jsync> {

    static public class Result extends com.fizzed.blaze.core.Result<Jsync,Boolean,Result> {

        Result(Jsync action, Boolean value) {
            super(action, value);
        }

    }

    private final VerboseLogger log;
    private final ValueHolder<Boolean> progress;
    private final JsyncEngine engine;

    public Jsync(Context context) {
        super(context);
        this.log = new VerboseLogger(this);
        this.progress = new ValueHolder<>(false);
        this.engine = new JsyncEngine();
    }

    @Override
    public VerboseLogger getVerboseLogger() {
        return this.log;
    }

    @Override
    public ValueHolder<Boolean> getProgressHolder() {
        return this.progress;
    }

    public Jsync delete(boolean delete) {
        this.engine.setDelete(delete);
        return this;
    }

    public Jsync preferredChecksums(Checksum... checksum) {
        this.engine.setPreferredChecksums(checksum);
        return this;
    }

    @Override
    protected Result doRun() throws BlazeException {


        return new Result(this, true);
    }

}