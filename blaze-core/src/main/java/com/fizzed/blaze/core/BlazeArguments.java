package com.fizzed.blaze.core;

import com.fizzed.blaze.internal.TaskHelper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BlazeArguments {

    // blaze.jar arguments
    private boolean showVersion;                    // -v || --version
    private boolean showHelp;                       // -h || --help
    private boolean listTasks;                      // -l || --list
    private boolean generateMavenProject;           // --generate-maven-project arg
    private Path installDir;                        // -i <dir> arg
    private int loggingLevel;                       // e.g. qq (-2), q (-1), x (1), xx (2), or xxx (3)
    private Path blazeFile;
    private Path blazeDir;
    private final Map<String,String> systemProperties;
    // task arguments
    private final Map<String,String> configProperties;
    private final List<String> tasks;

    public BlazeArguments() {
        this.tasks = new ArrayList<>();
        this.systemProperties = new LinkedHashMap<>();
        this.configProperties = new LinkedHashMap<>();
    }

    public boolean isShowVersion() {
        return showVersion;
    }

    public BlazeArguments setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
        return this;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public BlazeArguments setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
        return this;
    }

    public boolean isListTasks() {
        return listTasks;
    }

    public BlazeArguments setListTasks(boolean listTasks) {
        this.listTasks = listTasks;
        return this;
    }

    public boolean isGenerateMavenProject() {
        return generateMavenProject;
    }

    public BlazeArguments setGenerateMavenProject(boolean generateMavenProject) {
        this.generateMavenProject = generateMavenProject;
        return this;
    }

    public int getLoggingLevel() {
        return loggingLevel;
    }

    public BlazeArguments setLoggingLevel(int loggingLevel) {
        this.loggingLevel = loggingLevel;
        return this;
    }

    public Path getInstallDir() {
        return installDir;
    }

    public BlazeArguments setInstallDir(Path installDir) {
        this.installDir = installDir;
        return this;
    }

    public Path getBlazeFile() {
        return blazeFile;
    }

    public BlazeArguments setBlazeFile(Path blazeFile) {
        this.blazeFile = blazeFile;
        return this;
    }

    public Path getBlazeDir() {
        return blazeDir;
    }

    public BlazeArguments setBlazeDir(Path blazeDir) {
        this.blazeDir = blazeDir;
        return this;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public Map<String, String> getConfigProperties() {
        return configProperties;
    }

    public List<String> getTasks() {
        return tasks;
    }

    static public BlazeArguments parse(String[] arguments) {
        return parse(Arrays.asList(arguments));
    }

    static public BlazeArguments parse(List<String> arguments) {
        // converts arguments to a deque for easy popping/next/etc
        final Deque<String> args = new ArrayDeque<>(arguments);

        final BlazeArguments blazeArgs = new BlazeArguments();

        // are we switching to parsing arguments in task mode?  where basically we treat everything after this point
        // as though its being fed to the task configuration rather than possibly being eaten up by blaze.jar
        boolean taskParsingMode = false;

        while (!args.isEmpty()) {
            final String arg = args.remove();

            //
            // these are blaze.jar arguments UNTIL a task is provided, then we will switch and treat them explicitly
            // as config properties, so tasks could technically ask for things like "--version" as well
            //
            if (!taskParsingMode) {
                boolean handled = true;
                if (arg.equals("-v") || arg.equals("--version")) {
                    blazeArgs.setShowVersion(true);
                    // TODO: should we exit parsing?
                } else if (arg.equals("-q") || arg.equals("-qq") || arg.equals("-x") || arg.equals("-xx") || arg.equals("-xxx")) {
                    switch (arg.substring(1)) {
                        case "q":
                            blazeArgs.setLoggingLevel(-1);
                            break;
                        case "qq":
                            blazeArgs.setLoggingLevel(-2);
                            break;
                        case "x":
                            blazeArgs.setLoggingLevel(1);
                            break;
                        case "xx":
                            blazeArgs.setLoggingLevel(2);
                            break;
                        case "xxx":
                            blazeArgs.setLoggingLevel(3);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid logging level '" + arg + "'");
                    }
                } else if (arg.equals("-h") || arg.equals("--help")) {
                    blazeArgs.setShowHelp(true);
                    // TODO: should we exit parsing?
                } else if (arg.equals("-f") || arg.equals("--file")) {
                    String nextArg = nextArg(args, arg, "<file>");
                    Path f = Paths.get(nextArg);
                    blazeArgs.setBlazeFile(f);
                } else if (arg.equals("-d") || arg.equals("--dir")) {
                    String nextArg = nextArg(args, arg, "<dir>");
                    Path d = Paths.get(nextArg);
                    blazeArgs.setBlazeDir(d);
                } else if (arg.equals("--generate-maven-project")) {
                    blazeArgs.setGenerateMavenProject(true);
                    // TODO: should we exit parsing?
                } else if (arg.equals("-i") || arg.equals("--install")) {
                    String nextArg = nextArg(args, arg, "<dir>");
                    Path installDir = Paths.get(nextArg);
                    blazeArgs.setInstallDir(installDir);
                } else if (arg.equals("-l") || arg.equals("--list")) {
                    blazeArgs.setListTasks(true);
                    // TODO: should we exit parsing?
                } else if (arg.startsWith("-D")) {
                    // strip -D then split on first equals char
                    final String nv = arg.substring(2);
                    final int equalsPos = nv.indexOf('=');
                    final String key = equalsPos < 0 ? nv : nv.substring(0, equalsPos);
                    final String val = equalsPos > 0 && equalsPos + 1 < nv.length() ? nv.substring(equalsPos + 1) : null;
                    blazeArgs.getSystemProperties().put(key, val);
                } else {
                    handled = false;
                }

                // if we already handled this argument then we are good to go
                if (handled) {
                    continue;
                }
            }

            // otherwise, handle arguments, which allows for hijacking blaze.jar arguments once at least 1 task is called
            if (arg.startsWith("--")) {
                // this is a config property
                // TODO: should we support the = syntax to set a value as well?
                final String key = arg.substring(2);
                String val = args.peek();
                if (val == null || val.startsWith("--")) {
                    // this is a flag and in order for config options to work, we'll implicitly set the value to "true"
                    val = "true";
                } else {
                    // this is a real value and we need to remove it from the args queue
                    args.remove();
                }

                // is it a valid key?
                if (!TaskHelper.isValidConfigKey(key)) {
                    throw new IllegalArgumentException("Argument name '" + key + "' is not valid. Argument names must start with a letter or number (e.g. --key)");
                }

                blazeArgs.getConfigProperties().put(key, val);
            } else if (arg.startsWith("-")) {
                // this is an indicator of a bad value
                throw new IllegalArgumentException("Arguments must start with '--' (you provided '" + arg + "'");
            } else {
                // this may either be a task to run OR a blaze script to execute -- we will use some logic to figure out which
                // is this possibly a blaze script to execute? tasks must be empty, blaze file not explicitly set, and it will
                // have a file extension (e.g. blaze.java or blaze.kt, etc), plus tasks cannot contain dots anyway
                int dotPos = arg.lastIndexOf('.');
                int fileExtLength = dotPos > 0 ? arg.length()-dotPos-1 : 0;
                if (blazeArgs.getBlazeFile() == null && !taskParsingMode && fileExtLength > 0 && fileExtLength < 7) {
                    blazeArgs.setBlazeFile(Paths.get(arg));
                } else {
                    // otherwise, this is a task that is being requested to be run
                    // we should validate that its a valid task name though to catch errors as soon as possible
                    if (!TaskHelper.isValidName(arg)) {
                        throw new IllegalArgumentException("Task name '" + arg + "' is not valid. Task names must be alphanumeric and cannot contain spaces or periods (e.g. 'my-task' or 'my_task')");
                    }
                    blazeArgs.getTasks().add(arg);
                }
            }

            // also, this now indicates we've moved to task parsing modef
            taskParsingMode = true;
        }

        return blazeArgs;
    }

    static private String nextArg(Deque<String> args, String arg, String valueDescription) {
        if (args.isEmpty()) {
            throw new IllegalArgumentException(arg + " argument requires next arg to be a " + valueDescription);
        }
        return args.remove();
    }

}