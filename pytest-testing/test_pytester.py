"""
Learning tests on how the module resolution works in Pytester 
when testing Pytest plugins 
ref https://chatgpt.com/c/681b3b1f-3a8c-8004-bd3c-15ee45fe5007
"""
from pathlib import Path
import pytest

# need to set the path here, as the test might change CWD due to Pytester!
my_plugin_path = Path.cwd().joinpath('foopkg/my_plugin.py')

original_cwd = Path.cwd()

@pytest.fixture()
def my_pytester(pytester):
    pytester.makepyfile("""
    def test_foobar():
        assert 1 == 1""")
    return pytester

def test_using_plugin_by_cmdline(my_pytester):
    result = my_pytester.runpytest('-s', '-p foopkg.my_plugin ')

    assert "Setting 'test_foobar' up" in result.stdout.str()

@pytest.mark.xfail(reason="Will not work as local plugins are not in the pytester PYTHONPATH")
def test_using_plugin_by_plugins_field(my_pytester):
    my_pytester.plugins.append('foopkg.my_plugin')
    result = my_pytester.runpytest('-s')

    assert "Setting 'test_foobar' up" in result.stdout.str(), "Expected string not found"

def test_using_plugin_by_injecting_then_using_plugins(my_pytester, inject_local_plugin):
    # print(f'PATH CWD: {Path.cwd()}')
    # print(f'PATH path: {my_plugin_path}')
    inject_local_plugin('foopkg.my_plugin', my_plugin_path)

    my_pytester.plugins.append('foopkg.my_plugin')
    result = my_pytester.runpytest('-s')

    assert "Setting 'test_foobar' up" in result.stdout.str(), "Expected string not found"

def test_pytester_changes_cwd(pytester):
    assert original_cwd != Path.cwd()

def test_cwd_of_normal_test_has_unchanged_cwd():
    assert original_cwd == Path.cwd()

