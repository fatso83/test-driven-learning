import pytest

items = []
called = []

def pytest_runtest_setup(item:pytest.Item):
    # called for running each test in 'a' directory
    print(f"Setting '{item.name}' up")
    items.append(item)
    called.append("pytest_runtest_setup")

def pytest_sessionfinish(session: pytest.Session):
    called.append("pytest_sessionfinish")
    print("SESSION FINISHED")
    print(f"Ran: {len(items)}")
    print(f"Called: {called}")

def pytest_runtest_call(item):
    called.append("pytest_runtest_call")

