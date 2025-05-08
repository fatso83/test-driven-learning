# Learning tests on using Pytester

Pytester is a module inside of Pytest that is created to
test pytest itself and any plugins you create for it.

These tests are designed to document what the result
of multiple calls to stuff like `pytester.makeconftest` is, 
how to assert on the output from generated pytester tests, etc
