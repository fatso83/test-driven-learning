/**
 * Tests for understanding Promises chaining
 *
 * Some tests for the utility functions in pinkySwear
 *
 * @author carl-erik.kopseng
 * @date   02.04.14.
 */

var pinkySwear = require('pinkyswear'),
	should = require('should'),
	when = require('./utils').when;


describe('then chaining', function () {

	it('should stop success chaining at the first error', function (done) {
		var resolves = [];

		var p1 = pinkySwear(),
			p2 = p1.then(function () { resolves.push('p1'); }),
			p3 = p2.then(function () {
				resolves.push('p2');
				throw Error();
			}),
			p3
		.
		then(
			function () { resolves.push('never called'); }
		);

		p1(true);

		setTimeout(function () {
			resolves.should.eql(['p1', 'p2']);
			done();
		}, 20);
	});

	it('should continue chaining after a successful error handler', function (done) {
		var resolves = [];

		var willFail = pinkySwear();
		willFail.then('wont happen', function onerror () {return 'OK'; })
			.then(function (result) {
				result.should.equal('OK');
				resolves.push('error handled');
			});
		willFail(false, [new Error()]);

		setTimeout(function () {
			resolves.should.eql(['error handled']);
			done();
		}, 20);
	});

	it('should run all attached then\'s to a promise', function (done) {
		var resolves = [];

		var p1 = pinkySwear();
		p1.then(function () { resolves.push('p1 a'); });
		p1.then(function () { resolves.push('p1 b'); });
		p1.then(function () {
			resolves.push('p1 error');
			throw Error();
		});

		p1(true);

		setTimeout(function () {
			resolves.should.eql(['p1 a', 'p1 b', 'p1 error']);
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
});

describe('pinkySwear\'s error function', function () {

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
});
