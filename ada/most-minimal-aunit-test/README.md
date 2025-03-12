The most minimal (only gprbuild, no project file, no packages) example I could make:
- one procedure that invokes a function
- standalone files to avoid packages
- one testfile that tests the function

To build the program and run tests:
```sh
make
```

This will run `./test_is_positive`. You can also run the procedure `./check_positive`
