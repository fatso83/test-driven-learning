/**
 * @author carl-erik.kopseng
 * @date   30.10.13.
 */
/** for hybrid testing
 * @see http://docs.busterjs.org/en/latest/hybrid-testing/
 */
if (typeof module == "object" && typeof require == "function") {
	var buster = require("buster");
}

var sinon = require('sinon'),
	$ = require('jquery');
var assert = buster.referee.assert;


buster.testCase("deferred.then():", {
	"fires after a resolved deferred" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.then(spy);
		dfd.resolve();
		assert(spy.calledOnce);
	},

	"does not fire after a rejected deferred" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.then(spy);
		dfd.reject();
		assert(spy.notCalled);
	},

	"fires after a done() callback" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.done(sinon.stub()).then(spy);
		dfd.resolve();
		assert(spy.calledOnce);
	},

	"fires if a done() callback is rejected" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd
			.done(function () {
				var dfd = $.Deferred();
				dfd.reject();
				return dfd;
			})
			.then(spy);

		dfd.resolve();
		assert(spy.calledOnce);
	},

	"only error callback is fired if a previous then() callback is rejected" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy(),
			errorSpy = sinon.spy();

		dfd
			.then(function () {
				var dfd = $.Deferred();
				dfd.reject();
				return dfd;
			})
			.then(spy,errorSpy);

		dfd.resolve();
		assert(spy.notCalled);
		assert(errorSpy.calledOnce);
	},

	"is resolved with the original resolved value" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.promise()
			.then(spy);

		dfd.resolve("foo");
		assert(spy.calledWith("foo"));
	},

	"is called with the previous then()'s returned value" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.promise()
			.then(function () {
				return 2;
			})
			.then(spy);

		dfd.resolve("foo");
		assert(spy.calledWith(2));
	},

	"is not called with the previous done()'s returned value" : function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.promise()
			.done(function () {
				return 2;
			})
			.then(spy);

		dfd.resolve("foo");
		assert(spy.calledWith("foo"),"foo");
	}
});
