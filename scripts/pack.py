#!/usr/bin/env python3
"""Validate and assemble the ValenJEG mini-modpack from its pinned inputs."""
import argparse
import hashlib
import json
import shutil
import tempfile
import urllib.request
import zipfile
from pathlib import Path, PurePosixPath


class PackError(Exception):
    pass


def relative(value):
    p = PurePosixPath(value)
    if not value or p.is_absolute() or '\\' in value or '..' in p.parts or ':' in value:
        raise PackError(f'Unsafe pack path: {value!r}')
    return p


def digest(path, algorithm='sha256'):
    h = hashlib.new(algorithm)
    with path.open('rb') as f:
        for block in iter(lambda: f.read(1024 * 1024), b''):
            h.update(block)
    return h.hexdigest()


def load(root):
    module = json.loads((root / 'valencraft.module.json').read_text())
    lock = json.loads((root / 'mods.lock.json').read_text())
    if module['id'] != lock['module'] or module['game'] != lock['game']:
        raise PackError('Module and dependency lock disagree')
    if module['type'] != 'mini-modpack' or module['parent'] != 'valencraft':
        raise PackError('Invalid ValenCraft module identity')
    seen = set()
    for a in lock['artifacts']:
        key = (a['id'], a['side'])
        if key in seen:
            raise PackError(f'Duplicate artifact: {key}')
        seen.add(key)
        relative(a['file'])
        if len(PurePosixPath(a['file']).parts) != 1:
            raise PackError('Artifact filenames must be basenames')
        for alg, size in [('sha256', 64), ('sha1', 40), ('sha512', 128)]:
            h = a['hashes'][alg]
            if len(h) != size or any(c not in '0123456789abcdef' for c in h):
                raise PackError(f'Invalid {alg} for {a["id"]}')
        if any(not u.startswith('https://') for u in a['downloads']):
            raise PackError('Downloads must use HTTPS')
    return module, lock


def selected(lock, side):
    return [a for a in lock['artifacts'] if a['side'] == side]


def check(root, side):
    module, lock = load(root)
    artifacts = selected(lock, side)
    provided = {'minecraft', 'java', 'fabricloader'}
    owners = {}
    top_level = set()
    for a in artifacts:
        if a['id'] in top_level:
            raise PackError(f'Duplicate top-level mod ID: {a["id"]}')
        top_level.add(a['id'])
        for mod_id in a['contains']:
            # Fabric resolves shared nested libraries; do not install another top-level copy.
            owners[mod_id] = a['id']
            provided.add(mod_id)
    pending = [p for p in lock['pending'] if side in p['sides']]
    promised = {i for p in pending for i in p['contains']}
    required = set(lock['required_ids'][side])
    unexplained = required - provided - promised
    if unexplained:
        raise PackError('Unaccounted dependencies: ' + ', '.join(sorted(unexplained)))
    return module, lock, artifacts, pending


def resolve(artifact, inputs, cache, download):
    for directory in [inputs, cache]:
        if directory is None:
            continue
        path = directory / artifact['file']
        if path.is_file():
            if path.is_symlink() or digest(path) != artifact['hashes']['sha256']:
                raise PackError(f'Hash mismatch: {artifact["file"]}')
            return path
    if not download or not artifact['downloads']:
        raise PackError(f'Missing pinned input: {artifact["file"]}')
    cache.mkdir(parents=True, exist_ok=True)
    target = cache / artifact['file']
    with tempfile.NamedTemporaryFile(dir=cache, delete=False) as tmp:
        temporary = Path(tmp.name)
    try:
        request = urllib.request.Request(artifact['downloads'][0], headers={'User-Agent': 'ValenJEG-pack-builder/0.1'})
        with urllib.request.urlopen(request, timeout=60) as response, temporary.open('wb') as output:
            shutil.copyfileobj(response, output)
        if digest(temporary) != artifact['hashes']['sha256']:
            raise PackError(f'Download hash mismatch: {artifact["file"]}')
        temporary.replace(target)
    finally:
        temporary.unlink(missing_ok=True)
    return target


def overrides(root, side):
    result = {}
    for folder in ['overrides', f'{side}-overrides']:
        base = root / folder
        if base.exists():
            for p in sorted(base.rglob('*')):
                if p.is_symlink():
                    raise PackError(f'Symlink in overrides: {p}')
                if p.is_file():
                    key = str(relative(p.relative_to(base).as_posix()))
                    result[key] = p
    return result


def assemble(root, side, inputs, cache, output, download=False):
    module, lock, artifacts, pending = check(root, side)
    if pending:
        raise PackError('Unbuilt required components: ' + ', '.join(p['id'] for p in pending))
    if output.exists() or output.is_symlink():
        raise PackError(f'Refusing to overwrite: {output}')
    resolved = [(a, resolve(a, inputs, cache, download)) for a in artifacts]
    files = overrides(root, side)
    # Resolve every dependency before creating any output; an incomplete pack is never emitted.
    output.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(dir=output.parent) as temp:
        staging = Path(temp) / 'pack'
        staging.mkdir()
        for a, source in resolved:
            dest = staging / 'mods' / ('ValenJEG--' + a['file'])
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, dest)
        for path, source in files.items():
            dest = staging / path
            if dest.exists():
                raise PackError(f'Override conflicts with artifact: {path}')
            dest.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, dest)
        (staging / 'valencraft-module.json').write_text(json.dumps(module, indent=2) + '\n')
        staging.rename(output)


def mrpack(root, side, output):
    module, lock, artifacts, pending = check(root, side)
    if pending:
        raise PackError('Unbuilt required components: ' + ', '.join(p['id'] for p in pending))
    if any(not a['downloads'] for a in artifacts):
        raise PackError('Every mrpack artifact needs a published HTTPS download; use assemble for local inputs')
    if output.exists() or output.is_symlink():
        raise PackError(f'Refusing to overwrite: {output}')
    files = overrides(root, side)
    index = {'formatVersion': 1, 'game': 'minecraft', 'versionId': module['version'],
             'name': module['name'], 'summary': module['summary'], 'files': [],
             'dependencies': {'minecraft': module['game']['minecraft'], 'fabric-loader': module['game']['fabric_loader']}}
    for a in artifacts:
        index['files'].append({'path': 'mods/ValenJEG--' + a['file'],
                               'hashes': {k: a['hashes'][k] for k in ['sha1', 'sha512']},
                               'downloads': a['downloads'], 'fileSize': a['size'],
                               'env': {side: 'required', 'server' if side == 'client' else 'client': 'unsupported'}})
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, 'x', zipfile.ZIP_DEFLATED) as z:
        z.writestr('modrinth.index.json', json.dumps(index, indent=2) + '\n')
        for path, source in files.items():
            z.write(source, 'overrides/' + path)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('command', choices=['check', 'assemble', 'mrpack'])
    parser.add_argument('--root', type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument('--side', choices=['client', 'server'], default='client')
    parser.add_argument('--inputs', type=Path)
    parser.add_argument('--cache', type=Path, default=Path.home() / '.cache/valenjeg')
    parser.add_argument('--download', action='store_true')
    parser.add_argument('--output', type=Path)
    args = parser.parse_args()
    try:
        if args.command == 'check':
            module, lock, artifacts, pending = check(args.root, args.side)
            print(json.dumps({'module': module['id'], 'side': args.side, 'pinned_artifacts': len(artifacts),
                              'pending_builds': [p['id'] for p in pending], 'release_ready': not pending}, indent=2))
        elif args.output is None:
            parser.error('--output is required')
        elif args.command == 'assemble':
            assemble(args.root, args.side, args.inputs, args.cache, args.output, args.download)
        else:
            mrpack(args.root, args.side, args.output)
    except (PackError, KeyError, ValueError) as exc:
        parser.exit(1, f'Pack error: {exc}\n')


if __name__ == '__main__':
    main()
