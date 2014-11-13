$T.test = Task.create(function() {
    //prints to stdout
    //print("Hello World!");
    // full logging via slf4j (and its substition)
    log.info("Hello {}!", "World");
    log.debug("Only logged if -v flag passed to blaze");
    log.info("If you run blaze with the -v flag then you will see extra debug logging");
});
