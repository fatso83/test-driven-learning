The most minimal (only gprbuild, no project file, no packages) example I could make:
- one procedure that invokes a function
- standalone files to avoid packages
- one testfile that tests the function
- relies on global `aunit` installation being available

To build the program and run tests:
```sh
make
```

This will run `./test_is_positive`. You can also run the procedure `./check_positive`

# Downsides

Downsides of this simple approach is you will end up with loads of temporary files as a result of the build process. Using Alire will avoid this, by creating project files for GPRBuild to use, that will put these files into own directories that are ignored by Git.
