var sys = Packages.com.fizzed.blaze.Systems;
var ctx = Packages.com.fizzed.blaze.Contexts;
var str = "";

var main = function() {
    var binDir = ctx.withBaseDir("../bin");
    str = sys.exec("hello-world-test").path(binDir).captureOutput().run().output();
}

var output = function() {
    print(str);
}