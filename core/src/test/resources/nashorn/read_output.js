var sh = Packages.com.fizzed.blaze.Shells;
var ctx = Packages.com.fizzed.blaze.Contexts;
var str = "";

var main = function() {
    var binDir = ctx.withBaseDir("../bin");
    str = sh.exec("hello-world-test").path(binDir).readOutput().run().output();
}

var output = function() {
    print(str);
}