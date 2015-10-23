var sys = Packages.com.fizzed.blaze.Systems;

var main = function() {
    print("Finding javac...");
    var javac = sys.which("javac").run();

    print("Using javac " + javac);
    sys.exec("javac").arg("-version").run();
}
