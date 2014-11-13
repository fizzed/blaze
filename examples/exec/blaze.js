$T.demo = Task.create(function() {
    // try to find "hello" after adding additional path to search
    var result = $A.exec("hello").insertPath("bin").run();
    log.info("Previous command should have worked: exit_code={}", result.exitValue());
    
    // slurp up output into string
    result = $A.exec("hello").addPath("bin").readOutput().run();
    log.info("Previous command should have worked: exit_code={}, output={}", result.exitValue(), result.outputUTF8().trim()); 
    
    // list files in bin dir (contextually in this project)
    var result = $A.ls("bin").recursive(true).run();
    result.stream().forEach(function(f) { log.info(f); });
});
