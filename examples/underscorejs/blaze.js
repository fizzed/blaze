// demo loading javascript via url
load("https://cdnjs.cloudflare.com/ajax/libs/underscore.js/1.7.0/underscore-min.js");

$T.demo = Task.create(function() {
	_.each([1, 2, 3], function(v) {
		log.info("{}", v);
	});
});
