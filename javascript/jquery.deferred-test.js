var sinon = require('sinon'),
	$ = require('jquery'),
	assert = require('assert');


describe("deferred.then():", function () {

	it("should fire after a resolved deferred", function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.then(spy);
		dfd.resolve();
		assert(spy.calledOnce);
	});

	it("should not fire after a rejected deferred", function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.then(spy);
		dfd.reject();
		assert(spy.notCalled);
	});

	it("should fire after a done() callback", function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.done(sinon.stub()).then(spy);
		dfd.resolve();
		assert(spy.calledOnce);
	});

	it("should fire if a done() callback is rejected", function () {
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
	});

	it("should only error callback is fired if a previous then() callback is rejected", function () {
		var dfd = $.Deferred(),
			spy = sinon.spy(),
			errorSpy = sinon.spy();

		dfd
			.then(function () {
				var dfd = $.Deferred();
				dfd.reject();
				return dfd;
			})
			.then(spy, errorSpy);

		dfd.resolve();
		assert(spy.notCalled);
		assert(errorSpy.calledOnce);
	});

	it("should is resolved with the original resolved value", function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.promise()
			.then(spy);

		dfd.resolve("foo");
		assert(spy.calledWith("foo"));
	});

	it("should is called with the previous then()'s returned value", function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.promise()
			.then(function () {
				return 2;
			})
			.then(spy);

		dfd.resolve("foo");
		assert(spy.calledWith(2));
	});

	it("should is not called with the previous done()'s returned value", function () {
		var dfd = $.Deferred(),
			spy = sinon.spy();

		dfd.promise()
			.done(function () {
				return 2;
			})
			.then(spy);

		dfd.resolve("foo");
		assert(spy.calledWith("foo"), "foo");
	});
});
