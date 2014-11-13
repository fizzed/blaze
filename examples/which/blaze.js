$T.demo = Task.create(function() {
    var mvnExeFile = $A.which("mvn").run();
    log.info("mvn which result: {}", mvnExeFile);
    
    // try to find "hello" exe which is not in PATH
    var helloExeFile = $A.which("hello").run();
    log.info("hello which first result: {} (null is expected)", helloExeFile);
    
    // try to find "hello" after adding additional path to search
    helloExeFile = $A.which("hello").addPath("bin").run();
    log.info("hello which first result: {} (null is NOT expected)", helloExeFile);
});
