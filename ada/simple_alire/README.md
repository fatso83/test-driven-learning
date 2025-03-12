This example uses Alire to setup the initial project structure.
I then use `alr with aunit` to add dependencies.
`alr build` will build the project using _local dependencies_, instead
of relying on globally installed libraries.

# Building and running 
```
alr build
./bin/testrunner


FAIL Test Simple Alire package
    Failed something
    at simple_alire-test.adb:14

Total Tests Run:   1
Successful Tests:  0
Failed Assertions: 1
Unexpected Errors: 0
```

# Notes
- Loads of overhead with additional files (need testrunner and suite files)
- Unable to get the testrunner to run automatically when doing `alr run`
- The example in `aunit/examples/simple_example` shows an alternative approach 
