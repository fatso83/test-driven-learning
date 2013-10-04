/** @see http://docs.busterjs.org/en/latest/hybrid-testing/ */
var config = module.exports;

config["Tests"] = {
    rootPath: "..",
    tests: [
        "*-test.js"
    ]
};

config["Browser tests"] = {
    extends: "Tests",
    environment: "browser"
    //sources: ["lib**/*.js"]
};

config["Node tests"] = {
    extends: "Tests",
    environment: "node"
};
