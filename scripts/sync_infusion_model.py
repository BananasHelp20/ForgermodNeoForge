"""Sync the user-edited infusion concept JSON to the game and complete Blockbench project."""
import base64
import copy
import json
import struct
import uuid
from pathlib import Path
from zipfile import ZipFile

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / 'src/main/resources/assets/forgermod'
SOURCE = ROOT / 'markdown/designs/infusion_table_concept.json'
NAMESPACE = uuid.UUID('b1aa3ed6-d976-4ee4-9447-41a275e7ea23')


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')


def main():
    model = json.loads(SOURCE.read_text(encoding='utf-8-sig'))
    model['textures']['frame'] = 'forgermod:block/infusion_table/forged_netherite'
    model['textures']['handle'] = 'forgermod:block/carbon_steel_block'
    model['textures']['wood_handle'] = 'minecraft:block/stripped_spruce_log'
    model['textures']['handle_end'] = 'minecraft:block/stripped_spruce_log_top'
    marker = next(e for e in model['elements'] if e.get('name', '').endswith('_ready_to_set'))
    for index, element in enumerate(model['elements']):
        if element is marker:
            continue
        size = [b - a for a, b in zip(element['from'], element['to'])]
        for face_index, (face, data) in enumerate(element['faces'].items()):
            width, height = {'north': (size[0], size[1]), 'south': (size[0], size[1]),
                             'east': (size[2], size[1]), 'west': (size[2], size[1]),
                             'up': (size[0], size[2]), 'down': (size[0], size[2])}[face]
            if width > 16 or height > 16:
                raise ValueError('A larger face needs a newly extended texture/UV region: ' + element['name'])
            # Proportional coverage: a 2x12 post uses a 2x12 UV rectangle, never a squashed square.
            u = (index * 3 + face_index * 2) % max(1, int(16 - width) + 1)
            v = (index * 5 + face_index * 3) % max(1, int(16 - height) + 1)
            data['uv'] = [u, v, u + width, v + height]
            data.pop('rotation', None)
            if element.get('name') == 'slender_hammer_handle':
                data['texture'] = '#handle_end' if face in ('north', 'south') else '#wood_handle'
    write(SOURCE, model)

    runtime = copy.deepcopy(model)
    runtime.pop('format_version', None)
    runtime['elements'] = [e for e in runtime['elements'] if not e.get('name', '').endswith('_ready_to_set')]
    write(ASSETS / 'models/block/infusion_table.json', runtime)
    variants = {f'facing={facing},lit={lit}': {'model': 'forgermod:block/infusion_table', 'y': rotation}
                for facing, rotation in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]
                for lit in ('false', 'true')}
    write(ASSETS / 'blockstates/infusion_table.json', {'variants': variants})

    # Keep the active gem's authored placement as a guide; it is not part of the static game mesh.
    tray = next(e for e in model['elements'] if e.get('name', '').startswith('recessed_'))
    heads = [e for e in model['elements'] if e.get('name') == 'polished_setting_face']
    anchor = {'gemstone': {'x': (marker['from'][0] + marker['to'][0]) / 32,
                            'y': tray['to'][1] / 16,
                            'z': (marker['from'][2] + marker['to'][2]) / 32},
              'weapon': {'x': (min(e['from'][0] for e in heads) + max(e['to'][0] for e in heads)) / 32,
                         'y': max(e['to'][1] for e in heads) / 16,
                         'z': (min(e['from'][2] for e in heads) + max(e['to'][2] for e in heads)) / 32}}
    write(ASSETS / 'rendering/infusion_table.json', anchor)

    jar = ROOT / 'build/moddev/artifacts/neoforge-21.1.93-client-extra-aka-minecraft-resources.jar'
    textures, lookup = [], {}
    with ZipFile(jar) as vanilla:
        for resource in sorted(set(model['textures'].values())):
            domain, path = resource.split(':') if ':' in resource else ('minecraft', resource)
            raw = (vanilla.read('assets/minecraft/textures/' + path + '.png') if domain == 'minecraft'
                   else (ASSETS / ('textures/' + path + '.png')).read_bytes())
            width, height = struct.unpack('>II', raw[16:24])
            lookup[resource] = len(textures)
            textures.append({'name': path.rsplit('/', 1)[-1] + '.png', 'id': str(len(textures)),
                             'uuid': str(uuid.uuid5(NAMESPACE, 'texture/' + resource)),
                             'width': width, 'height': height, 'uv_width': width, 'uv_height': height,
                             'path': '', 'internal': True, 'saved': False, 'visible': True,
                             'source': 'data:image/png;base64,' + base64.b64encode(raw).decode()})
    cubes, sections = [], {}
    for index, element in enumerate(model['elements']):
        name = element.get('name', 'cube')
        section = ('Dynamic gemstone preview - hidden in game' if element is marker else
                   'Hammer - wooden handle' if 'hammer' in name else
                   'Precision calipers' if 'caliper' in name else
                   'Setting anvil' if 'anvil' in name or 'setting_face' in name else
                   'Gemstone tray' if 'tray' in name else 'Red carpet' if 'carpet' in name else 'Cabinet and frame')
        rotation = [0, 0, 0]
        if 'rotation' in element:
            rotation['xyz'.index(element['rotation']['axis'])] = element['rotation']['angle']
        faces = {}
        for face, data in element['faces'].items():
            tex = lookup[model['textures'][data['texture'][1:]]]
            faces[face] = {'texture': tex, 'uv': [v * textures[tex]['width'] / 16 for v in data['uv']]}
        cube_id = str(uuid.uuid5(NAMESPACE, 'cube/' + str(index)))
        cubes.append({'name': name, 'uuid': cube_id, 'type': 'cube', 'from': element['from'], 'to': element['to'],
                      'origin': element.get('rotation', {}).get('origin', [8, 0, 8]), 'rotation': rotation,
                      'box_uv': False, 'autouv': 0, 'export': element is not marker, 'faces': faces})
        sections.setdefault(section, []).append(cube_id)
    groups, tree = [], []
    for name, children in sections.items():
        group_id = str(uuid.uuid5(NAMESPACE, 'group/' + name))
        groups.append({'name': name, 'uuid': group_id, 'origin': [8, 0, 8], 'rotation': [0, 0, 0],
                       'export': True, 'visibility': not name.startswith('Dynamic'), 'isOpen': True})
        tree.append({'uuid': group_id, 'isOpen': True, 'children': children})
    write(ASSETS / 'models/block/infusion_table/infusion_table_concept.bbmodel', {
        'meta': {'format_version': '5.0', 'model_format': 'free', 'box_uv': False},
        'name': 'Infusion Table - Edited Gemsetting Bench', 'resolution': {'width': 16, 'height': 16},
        'elements': cubes, 'groups': groups, 'outliner': tree, 'textures': textures})
    print(f'Preserved {len(model["elements"])} authored objects; {len(runtime["elements"])} static game cubes, one dynamic gem guide.')


if __name__ == '__main__':
    main()
