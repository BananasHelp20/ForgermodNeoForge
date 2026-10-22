"""Audit local model texture paths without reading or changing any texture pixels."""
import argparse
from collections import defaultdict
import json
from pathlib import Path
import sys
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--jar', type=Path, help='Check the packaged mod instead of source resources')
    args = parser.parse_args()
    project = Path(__file__).resolve().parent.parent
    models = {}
    textures = set()
    if args.jar:
        with zipfile.ZipFile(args.jar) as archive:
            for name in archive.namelist():
                if name.startswith('assets/forgermod/textures/') and name.endswith('.png'):
                    textures.add(name[len('assets/forgermod/textures/'):-4])
                elif name.startswith('assets/forgermod/models/') and name.endswith('.json'):
                    models[name] = json.loads(archive.read(name))
    else:
        for root in ('src/generated/resources', 'src/main/resources'):
            assets = project / root / 'assets/forgermod'
            textures.update(p.relative_to(assets / 'textures').as_posix()[:-4]
                            for p in (assets / 'textures').rglob('*.png'))
            for path in (assets / 'models').rglob('*.json'):
                # build.gradle excludes this stale manual duplicate in favor of datagen's model.
                if root == 'src/main/resources' and path.relative_to(assets).as_posix() == 'models/item/sapphire_gemstone.json':
                    continue
                models['assets/forgermod/' + path.relative_to(assets).as_posix()] = json.loads(path.read_text(encoding='utf-8'))

    by_name = defaultdict(list)
    for texture in textures:
        by_name[texture.rsplit('/', 1)[-1]].append(texture)
    missing = defaultdict(list)
    checked = 0
    for model, data in models.items():
        for slot, value in data.get('textures', {}).items():
            if not isinstance(value, str) or not value.startswith('forgermod:'):
                continue
            checked += 1
            texture = value.split(':', 1)[1]
            if texture not in textures:
                missing[texture].append(f'{model} [{slot}]')
    relocated = 0
    print(f'Checked {len(models)} models and {checked} local texture entries.')
    for texture, uses in sorted(missing.items()):
        candidates = by_name.get(texture.rsplit('/', 1)[-1], [])
        relocated += bool(candidates)
        detail = 'relocated to ' + ', '.join(sorted(candidates)) if candidates else 'PNG absent'
        print(f'{texture}: {detail} ({len(uses)} reference(s))')
    print(f'Unresolved relocated paths: {relocated}; truly missing textures: {len(missing) - relocated}.')
    return 1 if missing else 0


if __name__ == '__main__':
    sys.exit(main())
