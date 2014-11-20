load("mustache.js");

$T.demo = Task.create(function() {
    var view = {
	  title: "Joe",
	  calc: function () {
		return 2 + 4;
	  }
	};

	var output = Mustache.render("{{title}} spends {{calc}}", view);
	log.info("{}", output);
});
