import importlib.util
import json
import tempfile
import unittest
import zipfile
from pathlib import Path

spec = importlib.util.spec_from_file_location('pack', Path(__file__).parents[1] / 'scripts/pack.py')
pack = importlib.util.module_from_spec(spec)
spec.loader.exec_module(pack)


class PackContracts(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.inputs = self.root / 'inputs'
        self.inputs.mkdir()
        self.jar = self.inputs / 'example.jar'
        with zipfile.ZipFile(self.jar, 'w') as z:
            z.writestr('fabric.mod.json', '{"id":"example","version":"1"}')
        self.module = {'id': 'valenjeg', 'parent': 'valencraft', 'type': 'mini-modpack',
                       'game': {'minecraft': '26.3', 'fabric_loader': '0.20.1'},
                       'name': 'ValenJEG', 'version': 'test', 'summary': 'Test module'}
        self.artifact = {'id': 'example', 'side': 'client', 'file': self.jar.name,
                         'hashes': {a: pack.digest(self.jar, a) for a in ['sha1', 'sha256', 'sha512']},
                         'contains': ['example'], 'downloads': [], 'size': self.jar.stat().st_size}
        self.lock = {'module': 'valenjeg', 'game': self.module['game'], 'artifacts': [self.artifact],
                     'pending': [], 'required_ids': {'client': ['example'], 'server': []}}
        self.write()

    def write(self):
        (self.root / 'valencraft.module.json').write_text(json.dumps(self.module))
        (self.root / 'mods.lock.json').write_text(json.dumps(self.lock))

    def test_assembly_preserves_jar_bytes_and_copies_config(self):
        config = self.root / 'overrides/config/jeg-server.toml'
        config.parent.mkdir(parents=True)
        config.write_text('physicalMagazineFeedEnabled = true\n')
        out = self.root / 'assembled'
        pack.assemble(self.root, 'client', self.inputs, None, out)
        self.assertEqual(pack.digest(out / 'mods/ValenJEG--example.jar'), pack.digest(self.jar))
        self.assertEqual((out / 'config/jeg-server.toml').read_bytes(), config.read_bytes())

    def test_corrupt_input_creates_no_output(self):
        self.jar.write_bytes(b'corrupt')
        out = self.root / 'assembled'
        with self.assertRaisesRegex(pack.PackError, 'Hash mismatch'):
            pack.assemble(self.root, 'client', self.inputs, None, out)
        self.assertFalse(out.exists())

    def test_unbuilt_mod_blocks_output_even_if_other_inputs_exist(self):
        self.lock['pending'] = [{'id': 'hbm', 'sides': ['client', 'server'], 'contains': ['hbm']}]
        self.write()
        out = self.root / 'assembled'
        with self.assertRaisesRegex(pack.PackError, 'Unbuilt required components: hbm'):
            pack.assemble(self.root, 'client', self.inputs, None, out)
        self.assertFalse(out.exists())

    def test_missing_dependency_is_not_silently_ignored(self):
        self.lock['required_ids']['client'].append('missing')
        self.write()
        with self.assertRaisesRegex(pack.PackError, 'Unaccounted dependencies: missing'):
            pack.check(self.root, 'client')

    def test_existing_instance_is_never_overwritten(self):
        out = self.root / 'live-instance'
        out.mkdir()
        marker = out / 'owner-data'
        marker.write_text('preserve')
        with self.assertRaisesRegex(pack.PackError, 'Refusing to overwrite'):
            pack.assemble(self.root, 'client', self.inputs, None, out)
        self.assertEqual(marker.read_text(), 'preserve')

    def test_path_escape_rejected(self):
        for bad in ['../escape.jar', '/absolute.jar', 'C:\\escape.jar']:
            self.artifact['file'] = bad
            self.write()
            with self.assertRaises(pack.PackError):
                pack.load(self.root)

    def test_mrpack_has_index_and_both_required_hashes(self):
        self.artifact['downloads'] = ['https://cdn.modrinth.com/example.jar']
        self.write()
        out = self.root / 'test.mrpack'
        pack.mrpack(self.root, 'client', out)
        with zipfile.ZipFile(out) as z:
            index = json.loads(z.read('modrinth.index.json'))
        self.assertEqual(index['dependencies']['minecraft'], '26.3')
        self.assertEqual(index['files'][0]['hashes'], {a: pack.digest(self.jar, a) for a in ['sha1', 'sha512']})


if __name__ == '__main__':
    unittest.main()
