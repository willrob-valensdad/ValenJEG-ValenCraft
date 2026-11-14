"""Check native recipe-holder references after replacing vanilla recipes with original stations."""
import argparse
import json
from pathlib import Path
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--source-jar', type=Path)
parser.add_argument('--mods', type=Path)
parser.add_argument('--report', type=Path)
args = parser.parse_args()
root = Path(__file__).resolve().parents[1]
removed = {row['vanilla_recipe'] for row in json.loads((root / 'provenance/removed-vanilla-crafting-shortcuts.json').read_text())['recipes']}
recipes, refs = set(), []

def ids(value):
    if isinstance(value, str):
        yield value
    elif isinstance(value, list):
        for child in value:
            yield from ids(child)
    elif isinstance(value, dict):
        # Native known-recipe player predicates use recipe IDs as map keys.
        for key in value:
            yield key

def walk(value, origin, path=''):
    if isinstance(value, dict):
        for key, child in value.items():
            at = path + '/' + key
            if key in {'recipes', 'recipe_id', 'recipe'}:
                for target in ids(child):
                    if ':' in target:
                        refs.append({'origin': origin, 'json_path': at, 'target': target})
            walk(child, origin, at)
    elif isinstance(value, list):
        for index, child in enumerate(value):
            walk(child, origin, path + '/' + str(index))

def consume(name, content, origin):
    parts = name.split('/')
    if len(parts) < 4 or parts[0] != 'data' or not name.endswith('.json'):
        return
    if parts[2] in {'recipe', 'recipes'}:
        recipes.add(parts[1] + ':' + '/'.join(parts[3:])[:-5])
    if parts[2] in {'advancement', 'advancements'}:
        walk(json.loads(content), origin)
    elif parts[2:4] in (['tags', 'recipe'], ['tags', 'recipes']):
        for value in json.loads(content).get('values', []):
            if isinstance(value, dict):
                if not value.get('required', True):
                    continue
                value = value['id']
            if not value.startswith('#'):
                refs.append({'origin': origin, 'json_path': '/values', 'target': value})

def archive(path, external=False):
    with zipfile.ZipFile(path) as jar:
        if external and 'fabric.mod.json' in jar.namelist():
            # The staged previous candidate must never satisfy references removed from this source.
            if json.loads(jar.read('fabric.mod.json'), strict=False).get('id') == 'jeg':
                return
        for name in jar.namelist():
            if name.startswith('data/') and name.endswith('.json'):
                # External sources may override resources but cannot reinstate removed bypasses.
                if external and name.startswith('data/jeg/recipe/'):
                    continue
                if '/advancement/' in name or '/advancements/' in name or '/recipe/' in name or '/recipes/' in name:
                    consume(name, jar.read(name), str(path) + '!' + name)

if args.source_jar:
    archive(args.source_jar)
else:
    resources = root / 'src/main/resources'
    for path in sorted((resources / 'data').rglob('*.json')):
        consume(path.relative_to(resources).as_posix(), path.read_bytes(), str(path.relative_to(root)))
if args.mods:
    for path in sorted(args.mods.glob('*.jar')):
        archive(path, external=True)
errors = [row for row in refs if row['target'] in removed or (row['target'].startswith('jeg:') and row['target'] not in recipes)]
report = {'status': 'fail' if errors else 'pass', 'loaded_recipe_resources': len(recipes), 'native_recipe_holder_references': len(refs), 'removed_recipe_ids': len(removed), 'errors': errors}
if args.report:
    args.report.write_text(json.dumps(report, indent=2) + chr(10))
print(json.dumps(report, indent=2))
raise SystemExit(1 if errors else 0)
