
all: test main run-tests

main: is_positive.adb check_positive.adb
	gprbuild check_positive.adb

test: test_is_positive.adb
	gprbuild test_is_positive.adb

run-tests:
	./test_is_positive

