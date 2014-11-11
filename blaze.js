
var group = "co.fizzed";
var name = "fizzed-blaze";
var version = "1.0.0-SNAPSHOT";
var flavor = "main";
var targetDir = new java.io.File("target-blaze");
var classesDir = new java.io.File(targetDir, "classes/" + flavor);
var sourceDirs = [ "src/main/java" ];
var resourceDirs = [ "src/main/resources" ];
var dependenciesDir = new java.io.File(targetDir, "dependencies");
var ivyClasspath = undefined;
var storkLauncherGenerateInputDir = new java.io.File("src/main/launchers");
var stageDir = new java.io.File(targetDir, "stork");
var compressJar = true;
var jarFile = new java.io.File(targetDir, group + "." + name + "-" + version + ".jar");

$T.dependencies = Task.create(function() {
    print("DEPENDENCIES TASK");
    
    var ivy = $A.ivy()
        .projectGroup(group)
        .projectName(name)
        .projectName(version)
        .resolveScope("runtime")
        .outputDir(dependenciesDir);
    
    ivy.resolvers
        //.addIvyLocal()
        .addMavenLocal()
        .addMavenCentral();
    
    ivy.dependencies
        .add("org.zeroturnaround", "zt-exec", "1.7")
        .add("org.apache.ivy", "ivy", "2.4.0-rc1")
        .add("co.fizzed", "fizzed-stork-launcher", "1.2.0-SNAPSHOT")
        .add("ch.qos.logback", "logback-classic", "1.1.2");
    
    // any action can be directly called as a function or via run()
    //ivy.run();
    ivy();
    
    ivyClasspath = ivy.classpath();
});

$T.compile = Task.create(function() {
    print("COMPILE TASK");
    $T.dependencies();
    
    var sourceEncoding = "UTF-8";
    var sourceCompat = "1.8";
    targetDir.mkdirs();
    classesDir.mkdirs();
    
    // generate list of files to compile
    var javaInputFilesFile = new java.io.File(targetDir, "compiler/JavaInputFiles.txt");
    javaInputFilesFile.parentFile.mkdirs();
    var pw = new java.io.PrintWriter(javaInputFilesFile, "UTF-8");
    for (var i = 0; i < sourceDirs.length; i++) {
        var sourceDir = sourceDirs[i];
        var javaFiles = $A.ls(sourceDir).recursive(true).filter(function(f) { return f.isFile(); }).call();
        for each (f in javaFiles) {
            pw.println(f.absolutePath);
        }
    }
    pw.close();
    
    $A.exec("javac", "-sourcepath", sourceDir, "-d", classesDir, "-cp", ivyClasspath, "-encoding", sourceEncoding, "-source", sourceCompat, "@"+javaInputFilesFile.absolutePath).call();
});

$T.jar = Task.create(function() {
    print("JAR TASK");
    $T.compile();
    var args = "cf";
    if (!compressJar) {
        args += "0";
    }
    $A.exec("jar", "cf", jarFile, "-C", classesDir, ".").call();
});

$T.storkify = Task.create(function() {
    print("STORKIFY TASK");
    // uses fizzed-stork to compile rock-solid launch scripts
    $A.storkLauncherGenerate().outputDir(stageDir).inputFile(storkLauncherGenerateInputDir).run();
});

$T.stage = Task.create(function() {
    print("STAGE TASK");
    
    var dependLibDir = new java.io.File(dependenciesDir, "runtime");
    var stageLibDir = new java.io.File(stageDir, "lib");
    stageLibDir.mkdirs();
    
    // prep actions to add to first pipeline
    var copyProjectJar = $A.cp(jarFile).target(stageLibDir);
    var copyJarDependencies = $A.cp(dependLibDir).target(stageLibDir);
    
    
    // wait for pipeline of async action groups to all complete
    $A.pipeline(
        $A.async($T.jar, copyProjectJar, copyJarDependencies),
        $A.async($T.storkify)
    );
    
   
    /**
    // serial
    $T.jar();
    copyProjectJar();
    copyJarDependencies();
    $T.storkify();
    */
    
    print("FINISHED");
});
