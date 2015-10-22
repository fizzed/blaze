
var main = function() {
    var Joiner = Packages.com.google.common.base.Joiner;
    var s = Joiner.on("; ").skipNulls().join("Harry", null, "Ron", "Hermione");
    print(s);
}
