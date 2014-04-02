var assert = require('assert');

describe("Array Tests - array splice", function() {

  it('should modify array' , function() {
    var arr = [1, 2, 3, 4, 5];
    arr.splice(2, 3);

    assert.deepEqual([1,2], arr);
  });

  it('should return removed items' , function() {
    var arr = [1, 2, 3, 4, 5];
    var result = arr.splice(2, 3);

    assert.deepEqual([3,4,5], result);
  });

});
