pytest_plugins = "pytester"

from pathlib import Path
import pytest

@pytest.fixture
def inject_local_plugin(pytester):
    """
    Local plugins are not available for Pytester, unless
    provided explicitly using `pytester.runpytest('-p my.local.plugin')` 

    This plugin makes it possible to load these plugins normally using `pytest.plugins`
    by copying the source files into the temporary directory of the test
    """
    def _inject(plugin_import_path: str, plugin_file: Path):
        """
        plugin_import_path: e.g., 'foopkg.my_plugin'
        plugin_file: Path to the plugin file (e.g., Path("foopkg/my_plugin.py"))
        """
        plugin_name = plugin_import_path.split('.')[-1]
        pytester.syspathinsert()  # adds the tmpdir to sys.path

        # Write the plugin file into temp dir (as conftest includes it via pytest_plugins)
        plugin_target = pytester.path / f"{plugin_name}.py"
        plugin_target.write_text(plugin_file.read_text())

        # Add pytest_plugins = [...] to conftest.py
        pytester.makeconftest(f"pytest_plugins = ['{plugin_name}']")

    return _inject


