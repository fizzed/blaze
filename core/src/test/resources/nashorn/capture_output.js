var sys = Packages.com.fizzed.blaze.Systems;
var ctx = Packages.com.fizzed.blaze.Contexts;
var strms = Packages.com.fizzed.blaze.util.Streamables;
var str = "";

var main = function() {
    var binDir = ctx.withBaseDir("../bin");
    var capture = strms.captureOutput();
    sys.exec("hello-world-test")
        .path(binDir)
        .pipeOutput(capture)
        .run();
    str = capture.toString();
}

var output = function() {
    print(str);
}