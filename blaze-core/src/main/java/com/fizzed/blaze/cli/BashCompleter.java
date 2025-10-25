package com.fizzed.blaze.cli;

import com.fizzed.blaze.core.Blaze;
import com.fizzed.blaze.core.BlazeArguments;
import com.fizzed.blaze.core.BlazeTask;

import java.util.Arrays;
import java.util.List;

public class BashCompleter extends Bootstrap1 {

    static public boolean isRequested(String[] args) {
        return args.length > 0 && (
            args[0].equals("--_generate_completion") || args[0].equals("--_generate_completion_with_desc")
        );
    }

    public void run(String[] args) {
        // should we include descriptions in the completion?
        final boolean includeDescriptions = args.length > 1 && args[0].equals("--_generate_completion_with_desc");

        // the first argument needs removed, the remaining arguments are what is currently on the cli
        final List<String> cliArgs = Arrays.asList(args).subList(1, args.length);

        // parse the arguments
        final BlazeArguments arguments = BlazeArguments.parse(cliArgs);

        // we need blaze to be SUPER quiet
        this.configureLogging(-3);  // turn everything off

        // try to build & compile the blaze script, so we  can get a list of tasks
        final Blaze blaze = this.buildBlaze(arguments, true);

        for (BlazeTask task : blaze.getTasks()) {
            if (includeDescriptions && task.getDescription() != null) {
                System.out.println(task.getName() + " - " + task.getDescription());
            } else {
                System.out.println(task.getName());
            }
        }

        System.exit(0);
    }

}