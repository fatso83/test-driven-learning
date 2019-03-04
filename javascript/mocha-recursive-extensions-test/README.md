# Tests to verify Mocha's `--extension` behaviour
> Verifies [Mocha issue 3808](https://github.com/mochajs/mocha/issues/3808)

According to [Mocha's own docs](https://mochajs.org/#-extension-ext-watch-extensions-ext) one should be able to specify which extensions mocha will automatically load. 

> Specifying --extension will remove .js as a test file extension; use --extension js to re-add it. For example, to load .mjs and .js test files, you must supply --extension mjs --extension js.

It quite clearly says that by specifying an extension, the default should be cleared. I have never seen this happen :-(

## Output contradicting the docs

The [`test`](./test) directory only contains a single file with the extension `bar`, so this means only one file should be loaded according to the docs. Instead, four files are loaded:
```
mocha --extension bar

This is not a test: no-test.js


  normal.bar
    ✓ should do nothing but register the test

  normal.bar.js
    ✓ should do nothing but register the test

  normal.js
    ✓ should do nothing but register the test
```
