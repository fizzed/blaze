var sh = Packages.com.fizzed.blaze.Shells;

var main = function() {
    print("Finding mvn...");
    var mvn = sh.which("mvn").run();

    print("Using mvn " + mvn);
    sh.exec("mvn").arg("-v").run();
}
