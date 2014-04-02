/**
 * @author carl-erik.kopseng
 * @date   02.04.14.
 */

var pinkySwear =require('pinkyswear');

/**
 * Will resolve with the promises in the order passed (if successful),
 * else will reject the promise with the cause.
 *
 * Can also pass an array of promises as the argument.
 *
 * Example when(promise1, promise2).then(function(data1, data2){ ...}, function error() {...} )
 *
 * @param promise1 / promises {Promise|[Promise]}
 * @param promise2
 * @param promisen
 *
 * @returns Promise
 */
function when () {
	var args = Array.isArray(arguments[0]) ? arguments[0] : arguments,
		len = args.length,
		resolved = 0,
		promise = pinkySwear(),
		results = [];

	[].forEach.call(args, function (p, i) {
		p.then(function (res) {
			resolved++;
			results[i] = res;
			if (resolved === len) {
				promise(true, results);
			}
		}, function (e) {
			promise(false, [e]);
		});
	});

	return promise;
}

exports.when = when;