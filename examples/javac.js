var sh = Packages.com.fizzed.blaze.Shells;

var main = function() {
    print("Finding javac...");
    var javac = sh.which("javac").run();

    print("Using javac " + javac);
    sh.exec("javac").arg("-version").run();
}
