#!/usr/bin/env python3
"""Retrieve pinned upstream assets locally without committing reserved artwork."""
import hashlib
import io
import json
import tarfile
import urllib.request
import zipfile
from pathlib import Path, PurePosixPath


def main():
    root = Path(__file__).resolve().parents[1]
    manifest = json.loads((root / 'components/upstream-assets.json').read_text())
    for archive in manifest['archives']:
        request = urllib.request.Request(archive['url'], headers={'User-Agent': 'ValenJEG-source-assets/0.1'})
        with urllib.request.urlopen(request, timeout=60) as response:
            data = response.read()
        if hashlib.sha256(data).hexdigest() != archive['sha256']:
            raise RuntimeError('Upstream archive hash mismatch')
        if archive['format'] == 'zip':
            container = zipfile.ZipFile(io.BytesIO(data))
            read = container.read
        else:
            container = tarfile.open(fileobj=io.BytesIO(data), mode='r:gz')
            read = lambda path: container.extractfile(path).read()
        with container:
            for f in archive['files']:
                relative = PurePosixPath(f['destination'])
                if relative.is_absolute() or '..' in relative.parts or '\\' in f['destination']:
                    raise RuntimeError('Unsafe destination')
                destination = root / relative
                payload = read(f['member'])
                if hashlib.sha256(payload).hexdigest() != f['sha256']:
                    raise RuntimeError(f'Asset hash mismatch: {relative}')
                if destination.exists() and destination.read_bytes() != payload:
                    raise RuntimeError(f'Refusing to overwrite a modified asset: {relative}')
                if destination.parent.resolve() != root.resolve() and root.resolve() not in destination.parent.resolve().parents:
                    raise RuntimeError('Destination escapes checkout')
                if destination.is_symlink():
                    raise RuntimeError('Refusing asset symlink')
                destination.parent.mkdir(parents=True, exist_ok=True)
                destination.write_bytes(payload)
        print(f'Restored {len(archive["files"])} pinned upstream assets locally')
    if manifest['unresolved']:
        print(f'{len(manifest["unresolved"])} modified/unmatched reserved assets remain explicit source inputs; see the manifest.')


if __name__ == '__main__':
    main()
