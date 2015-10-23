/* global Packages */

var Joiner = Packages.com.google.common.base.Joiner;

var main = function() {
    var s = Joiner.on("; ").skipNulls().join("Harry", null, "Ron", "Hermione");
    print(s);
};
