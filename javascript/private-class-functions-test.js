var assert = require('assert');

var Foo = (function () {
	function private1 (val) {
		this.val1 = val;
	}

	function Foo () { }

	Foo.prototype.public1 = function (val) {
		private1.call(this, val);
	};

	return Foo;
})();

var Bar = function () {
	function Bar () {
		var private1;

		// true private variables
		// downside,memory and performance hit by creating
		//    a closure (function) for every attribute in every object
		//    5 variables and 100 objects = 500 closures created
		this.getSetPrivate1 = function (val) {
			if (!val) return private1;
			private1 = val;
		}
	}

	Bar.prototype = new Foo;

	return Bar;
}();

describe("Private method tests", function () {
	it("should creates unique values in each object", function () {

		var f1 = new Foo, f2 = new Foo;
		f1.public1(2);
		f2.public1(1);

		assert.equal(2, f1.val1);
		assert.equal(1, f2.val1);
	});

	it("should the private functions are not listed", function () {

		var f1 = new Foo

    assert.equal(true, "public1" in f1);
    assert.equal(false, "private1" in f1);
	});
});

describe("Private attribute tests", function () {
	it("should the private attribute is not listed", function () {

		var b = new Bar;
		b.foo = 'fooval';
		b.getSetPrivate1('heisann');

		assert('foo' in b);
		assert(!('private1' in b));
		assert.equal(b.getSetPrivate1(), 'heisann');
	});
});
