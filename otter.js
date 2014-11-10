
$T.install = function() {
    print("I am a new function attached to an object...");
}

$T.test = function() {
    print("Hi there from Javascript... test");
    //$T.install();
    
    var mvn = $A.which("mvn").run();
    print("mvn: " + mvn);
    
    var ls = $A.ls("src/main/java").run();
    for each (f in ls) {
       print("file: " + f.absolutePath); 
    }
};


var ivy = $A.ivy();
$T.dependencies = function() {
    ivy.dependencies()
        .add("org.zeroturnaround", "zt-exec", "1.7")
        .add("org.apache.ivy", "ivy", "2.4.0-rc1")
        .add("ch.qos.logback", "logback-classic", "1.1.2");
    
    //ivy.resolvers()
    
    ivy.run();
}

$T.compile = function() {
    $T.dependencies();
    
    var classpath = ivy.classpath();
    print("ivy.classpath=" + classpath);
    
    var sourceEncoding = "UTF-8";
    var sourceCompat = "1.8";
    var sourceDir = "src/main/java";
    var targetDir = new java.io.File("target-new");
    targetDir.mkdirs();
    var classesDir = new java.io.File(targetDir, "classes");
    classesDir.mkdirs();
    
    // generate list of files to compile
    var javaFiles = $A.ls("src/main/java").recursive(true).filter(function(f) { return f.isFile() }).run();
    var javaFilesFile = new java.io.File(targetDir, "javafiles");
    var pw = new java.io.PrintWriter(javaFilesFile, "UTF-8");
    for each (f in javaFiles) {
       pw.println(f.absolutePath);
    }
    pw.close();
    
    $A.exec("javac", "-cp", "lib/*", "-sourcepath", sourceDir, "-d", classesDir, "-cp", classpath, "-encoding", sourceEncoding, "-source", sourceCompat, "@"+javaFilesFile.absolutePath).run();
}


$T.which = function() {
    var exe = "mvn";
    print("Trying out which for: " + exe);
    var exeFile = F.which(exe);
    print("Which result: " + exeFile);
}

$T.ls = function() {
    F.execute("ls", "-la");
};

$T.mvn = function() {
    F.execute("mvn", "compile");
};

$T.play = function() {
    F.execute("play", "--version");
};

$T.fun2 = function (object) {
    print("JS Class Definition: " + Object.prototype.toString.call(object));
};
