var assert = require('assert');

describe("Prototype Tests", function(){
it("should A prototype's properties are shared and mutable through inheriting objects",function() {
		function Super () {
		}
		Super.prototype.list = [];

		s = new Super;
		s.list.push("foo");
		assert.deepEqual(["foo"], Super.prototype.list);
		assert.deepEqual(s.list, Super.prototype.list);
	});

it("should A property created in the prototype constructor is not shared",function() {
		function Super () {
			this.list = [];
		}

		s = new Super;
		s.list.push("foo");
		assert.deepEqual(undefined, Super.prototype.list);
		assert.deepEqual(["foo"], s.list);
	});
});
