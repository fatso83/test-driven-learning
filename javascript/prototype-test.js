if (typeof module == "object" && typeof require == "function") {
	var buster = require("buster");
}

buster.testCase("Prototype Tests", {
	"A prototype's properties are shared and mutable through inheriting objects" : function() {
		function Super () {
		}
		Super.prototype.list = [];

		s = new Super;
		s.list.push("foo");
		assert.equals(["foo"], Super.prototype.list);
		assert.equals(s.list, Super.prototype.list);
	},

	"A property created in the prototype constructor is not shared" : function() {
		function Super () {
			this.list = [];
		}

		s = new Super;
		s.list.push("foo");
		assert.equals(undefined, Super.prototype.list);
		assert.equals(["foo"], s.list);
	}
});
