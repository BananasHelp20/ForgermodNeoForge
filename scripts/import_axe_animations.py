"""Import the authored axe animation curves and fallback mesh without changing keyframes."""
import base64
import json
import math
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / 'src/main/resources/assets/forgermod'


def rotate(point, origin, rotation):
    x, y, z = (point[i] - origin[i] for i in range(3))
    for axis, angle in enumerate(rotation):
        c, s = math.cos(math.radians(angle)), math.sin(math.radians(angle))
        if axis == 0:
            y, z = y*c-z*s, y*s+z*c
        elif axis == 1:
            x, z = x*c+z*s, -x*s+z*c
        else:
            x, y = x*c-y*s, x*s+y*c
    return [x+origin[0], y+origin[1], z+origin[2]]


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2)+'\n', encoding='utf-8')


def main(source_path, animations_only=False):
    source_path = Path(source_path)
    if source_path.is_dir():
        source_path = max(source_path.glob('axe_playerplaceholder_animations*.bbmodel'), key=lambda p: p.stat().st_mtime)
    folder = source_path.parent
    source = json.loads(source_path.read_text(encoding='utf-8-sig'))
    groups = {g['uuid']: g for g in source['groups']}
    curves = {}
    for animation in source['animations']:
        tracks = {}
        for uuid, track in animation['animators'].items():
            if uuid not in groups:
                continue  # External preview sound/particle files are not executable game resources.
            frames = []
            for key in track.get('keyframes', []):
                if key['channel'] not in ('rotation', 'position', 'scale'):
                    continue
                assert key['interpolation'] in ('linear', 'catmullrom'), 'Unsupported authored interpolation'
                assert len(key['data_points']) == 1, 'Discontinuous keys require pre/post support'
                point = key['data_points'][0]
                frames.append({'time': key['time'], 'channel': key['channel'], 'interpolation': key['interpolation'],
                               'value': [float(str(point.get(axis, '')).strip() or 0) for axis in 'xyz']})
            if frames:
                tracks[groups[uuid]['name']] = sorted(frames, key=lambda k: (k['time'], k['channel']))
        curves[animation['name']] = {'length': animation['length'], 'loop': animation['loop'] == 'loop', 'bones': tracks}
    rig = {g['name']: {'origin': g['origin'], 'rotation': g.get('rotation', [0, 0, 0])} for g in source['groups']}
    locators = {e['name']: e['position'] for e in source['elements'] if e.get('type') == 'locator'}
    write_json(ASSETS/'animations/axe_player.json', {'source': source_path.name, 'rig': rig, 'locators': locators, 'clips': curves})
    texture = next(t for t in source['textures'] if t.get('name') == 'swirl_grtg.png')
    particle = ASSETS/'textures/particle/axe_cast_swirl.png'
    particle.parent.mkdir(parents=True, exist_ok=True)
    particle.write_bytes(base64.b64decode(texture['source'].split(',', 1)[1]))
    write_json(particle.with_suffix('.png.mcmeta'), {'animation': {'frametime': 3}})
    write_json(ASSETS/'particles/axe_cast_swirl.json', {'textures': ['forgermod:axe_cast_swirl']})
    if animations_only:
        print(f'Imported {len(curves)} authored clips from {source_path.name}; mesh untouched.')
        return

    model = json.loads((folder/'Axe - Converted(1).bbmodel').read_text(encoding='utf-8-sig'))
    assert not any(any(g.get('rotation', [0, 0, 0])) for g in model['groups']), 'Rotated mesh groups require hierarchy baking'
    texture = model['textures'][0]
    png = ASSETS/'textures/item/axe_fallback.png'
    png.write_bytes(base64.b64decode(texture['source'].split(',', 1)[1]))
    width, height = texture.get('uv_width', model['resolution']['width']), texture.get('uv_height', model['resolution']['height'])
    lines = ['# Authored Axe - Converted(1).bbmodel; vertices baked, UVs preserved.', 'mtllib axe_fallback.mtl', 'usemtl axe']
    index = 1
    for cube in model['elements']:
        if cube.get('type') != 'cube' or not cube.get('export', True):
            continue
        a, b = cube['from'], cube['to']
        x0, y0, z0 = a
        x1, y1, z1 = b
        faces = {
            'north': [(x1,y0,z0),(x0,y0,z0),(x0,y1,z0),(x1,y1,z0)],
            'south': [(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)],
            'east': [(x1,y0,z1),(x1,y0,z0),(x1,y1,z0),(x1,y1,z1)],
            'west': [(x0,y0,z0),(x0,y0,z1),(x0,y1,z1),(x0,y1,z0)],
            'up': [(x0,y1,z1),(x1,y1,z1),(x1,y1,z0),(x0,y1,z0)],
            'down': [(x0,y0,z0),(x1,y0,z0),(x1,y0,z1),(x0,y0,z1)],
        }
        for side, vertices in faces.items():
            face = cube.get('faces', {}).get(side, {})
            if face.get('texture') is None:
                continue
            assert not face.get('rotation', 0), 'Rotated face UVs require explicit handling'
            for vertex in vertices:
                x, y, z = rotate(vertex, cube.get('origin', [0,0,0]), cube.get('rotation', [0,0,0]))
                lines.append(f'v {0.5+x/16:.9f} {0.5+(y-10)/16:.9f} {0.5+z/16:.9f}')
            u0, v0, u1, v1 = face['uv']
            for u, v in [(u0,v1),(u1,v1),(u1,v0),(u0,v0)]:
                lines.append(f'vt {u/width:.9f} {v/height:.9f}')
            lines.append('f '+' '.join(f'{i}/{i}' for i in range(index, index+4)))
            index += 4
    obj = ASSETS/'models/item/axe_fallback.obj'
    obj.write_text('\n'.join(lines)+'\n', encoding='utf-8')
    obj.with_suffix('.mtl').write_text('newmtl axe\nKd 1 1 1\nmap_Kd #axe\n', encoding='utf-8')
    display = json.loads((ASSETS/'models/item/axe_of_the_void.json').read_text())['display']
    display['gui'] = {'rotation': [30,225,0], 'translation': [0,-5,0], 'scale': [.3,.3,.3]}
    write_json(ASSETS/'models/item/axe_fallback.json', {
        'loader': 'neoforge:obj', 'model': 'forgermod:models/item/axe_fallback.obj',
        'automatic_culling': False, 'shade_quads': True, 'flip_v': False,
        'textures': {'axe': 'forgermod:item/axe_fallback', 'particle': 'forgermod:item/axe_fallback'}, 'display': display})
    print(f'Imported {len(curves)} unchanged clips and {index-1} mesh face vertices.')


if __name__ == '__main__':
    main(sys.argv[1], '--animations-only' in sys.argv[2:])
