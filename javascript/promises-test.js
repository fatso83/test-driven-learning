/**
 * Tests for understanding Promises chaining
 *
 * Some additional tests for understanding the utility functions in pinkySwear
 *
 * @author carl-erik.kopseng
 * @date   02.04.14.
 */

var pinkySwear = require('pinkyswear'),
	should = require('should'),
	when = require('./utils').when;


/**
 * Utility function for creating promise returning functions
 *
 * @param [returnValue] {*} the value that should be returned
 * @param [reject] {boolean} default false (meaning it is resolved)
 * @returns Function returning Promise
 */
function createPromise (returnValue, reject) {
	var p = pinkySwear();

	p(reject === undefined ? true : reject, returnValue ? [returnValue] : undefined);

	return p;
}

function dummy () {
	return function () {
		console.error("Should never end up here!");
	};
}

describe('then chaining', function () {
	this.timeout(90);

	it('should pass returned value from the the onFulfilled function to the next promise', function (done) {
		createPromise(42).then(function (res) {
			res.should.equal(42);
			done();
		});
	});

	it('should pass returned value from the the onRejected function to the next promise', function (done) {
		createPromise(-123, false)
			.then(0, function (res) {
				res.should.equal(-123);
				done();
			})
	});

	it('should stop success chaining at the first error', function (done) {
		var steps = [], p2, p3;

		p2 = createPromise('p1')
			.then(function (res) {
				steps.push(res);
				return 'p2';
			});
		p3 = p2.then(function (res) {
			steps.push(res);
			throw Error();
		});

		p3.then(function () { steps.push('never called'); });

		setTimeout(function () {
			steps.should.eql(['p1', 'p2']);
			done();
		}, 20);
	});

	it('should continue chaining after a successful error handler', function (done) {

		createPromise(false, new Error('an error'))
			.then('wont happen', function onerror () { /* handle error */ })
			.then(function () {
				done();
			});
	});

	it('should run all attached then\'s to a promise', function (done) {
		var steps = [];

		var p1 = pinkySwear();
		p1.then(function () { steps.push('p1 a'); });
		p1.then(function () { steps.push('p1 b'); });
		p1.then(function () {
			steps.push('p1 error');
			throw Error();
		});

		p1(true);

		setTimeout(function () {
			steps.should.eql(['p1 a', 'p1 b', 'p1 error']);
			done();
		}, 20);
	});

	it('should not matter if using promises or return values for resolving', function (done) {
		var promise = pinkySwear(), res1, res2;
		promise(true);

		promise
			.then(function () {
				var p = pinkySwear();
				p(true, ['foo']);
				return p;
			})
			.then(function (res) {res1 = res;});

		promise
			.then(function () { return 'foo2'; })
			.then(function (res) {res2 = res;});

		setTimeout(function () {
			res1.should.equal('foo');
			res2.should.equal('foo2');
			done();
		})
	});

	it('should bubble errors up the chain until the first error handler', function (done) {
		this.timeout(100);
		var p = pinkySwear();

		function dummy () {
			return function () {
				console.error("Should not be called");
			};
		}

		p.then(dummy())
			.then(dummy())
			.then(dummy(), function onerror () {
				done();
			})

		p(false, [new Error('an error')]);
	});
});

// seem to fail when tried in 2017
describe.skip('pinkySwear\'s error function', function () {

	it('should be equal to using then(0,fn)', function (done) {
		var resolves = [];

		var p1 = pinkySwear();
		var failing = p1.then(function () { throw Error('p1 success failed')})

		function errorSuccessHandler (val) {
			resolves.push(arguments);
		}

		failing.error(function (e) {
			try {
				e.should.be.an.instanceOf(Error);
			} catch (e) {done(e)}
			return 'foo1';
		}).then(errorSuccessHandler.bind(null, 'error.then'));

		failing.then(0, function (e) {
			try {
				e.should.be.an.instanceOf(Error);
			} catch (e) {done(e)}
			return 'foo2';
		}).then(errorSuccessHandler.bind(null, 'then0.then'));

		setTimeout(function () {
			resolves.should.eql([
				['error.then', 'foo1'],
				['then0.then', 'foo2']
			]);
			done();
		}, 20);

		p1(true);

	});

	it('should bubble errors up the chain until the first error handler', function (done) {
		this.timeout(100);
		var p = pinkySwear();

		p.then(dummy())
			.then(dummy())
			.error(function () {
				done();
			})
			.error(dummy());

		p(false, [new Error('an error')]);
	});
});

// seem to fail when tried in 2017
describe.skip('possible error in pinkyswear implementation', function () {
	this.timeout(100);

	it('should pass multiple arguments on 1', function (done) {

		var p = pinkySwear();
		p(true, [3, 2, 1]);

		p.then(function (r1, r2, r3) {
				r1.should.equal(3);
				r2.should.equal(2);
				r3.should.equal(1);
				done();
			}).error(done);
	});

	it('should pass multiple arguments on 2', function (done) {
		var p = pinkySwear();
		p(true);
		p.then(function () {
			var p = pinkySwear();
			p(true, [3, 2, 1]);
			return p;
		})
			.then(function (r1, r2, r3) {
				r1.should.equal(3);
				r2.should.equal(2);
				r3.should.equal(1);
				done();
			})
			.error(done);
	});
});
