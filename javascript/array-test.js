/** for hybrid testing
 * @see http://docs.busterjs.org/en/latest/hybrid-testing/
 */
if (typeof module == "object" && typeof require == "function") {
    var buster = require("buster");
}

buster.testCase("Array Tests", {
  "array splice should modify array" : function() {
    var arr = [1, 2, 3, 4, 5];
    arr.splice(2, 3);

    assert.equals([1,2], arr);
  },

  "array splice should return removed items" : function() {
    var arr = [1, 2, 3, 4, 5];
    var result = arr.splice(2, 3);

    assert.equals([3,4,5], result);
  }
});
