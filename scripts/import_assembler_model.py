"""Export the authored assembler mesh and embedded PNGs without changing its design.

Run again after editing models/block/weapon_assembler/assembler.bbmodel in Blockbench.
OBJ preserves arbitrary cube rotations that Java block elements cannot express.
"""
import base64
import json
from pathlib import Path
from import_axe_animations import rotate

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / 'src/main/resources/assets/forgermod'
FOLDER = ASSETS / 'models/block/weapon_assembler'


def main():
    model = json.loads((FOLDER / 'assembler.bbmodel').read_text(encoding='utf-8-sig'))
    elements = {element['uuid']: element for element in model['elements']}
    groups = {group['uuid']: group for group in model.get('groups', [])}
    visible = []

    def walk(nodes, ancestors=()):
        for node in nodes:
            if isinstance(node, dict):
                group = groups.get(node.get('uuid'), node)
                if group.get('visibility', True) and group.get('export', True):
                    walk(node.get('children', []), (group,) + ancestors)
            elif node in elements:
                cube = elements[node]
                if cube.get('visibility', True) and cube.get('export', True):
                    assert cube.get('type') == 'cube', 'Non-cube geometry needs explicit export support'
                    visible.append((cube, ancestors))

    walk(model['outliner'])
    textures = sorted({face['texture'] for cube, _ in visible for face in cube['faces'].values()
                       if face.get('texture') is not None})
    texture_dir = ASSETS / 'textures/block/weapon_assembler'
    texture_dir.mkdir(parents=True, exist_ok=True)
    materials = []
    for index in textures:
        texture = model['textures'][index]
        raw = base64.b64decode(texture['source'].split(',', 1)[1])
        assert raw.startswith(b'\x89PNG\r\n\x1a\n'), 'Expected an embedded PNG'
        (texture_dir / f'texture_{index}.png').write_bytes(raw)
        materials.extend([f'newmtl texture_{index}', 'Kd 1 1 1', f'map_Kd #texture_{index}'])

    def export_mesh(name, cubes):
        lines = ['# Imported from assembler.bbmodel; hidden previews omitted.', 'mtllib assembler.mtl']
        vertex_index = 1
        for cube, ancestors in cubes:
            x0, y0, z0 = cube['from']
            x1, y1, z1 = cube['to']
            faces = {
                'north': [(x1,y0,z0),(x0,y0,z0),(x0,y1,z0),(x1,y1,z0)],
                'south': [(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)],
                'east': [(x1,y0,z1),(x1,y0,z0),(x1,y1,z0),(x1,y1,z1)],
                'west': [(x0,y0,z0),(x0,y0,z1),(x0,y1,z1),(x0,y1,z0)],
                'up': [(x0,y1,z1),(x1,y1,z1),(x1,y1,z0),(x0,y1,z0)],
                'down': [(x0,y0,z0),(x1,y0,z0),(x1,y0,z1),(x0,y0,z1)],
            }
            for side, vertices in faces.items():
                face = cube['faces'].get(side, {})
                index = face.get('texture')
                if index is None:
                    continue
                texture = model['textures'][index]
                width = texture.get('uv_width', model['resolution']['width'])
                height = texture.get('uv_height', model['resolution']['height'])
                lines.append(f'usemtl texture_{index}')
                for vertex in vertices:
                    point = rotate(vertex, cube.get('origin', [0,0,0]), cube.get('rotation', [0,0,0]))
                    for group in ancestors:
                        point = rotate(point, group.get('origin', [0,0,0]), group.get('rotation', [0,0,0]))
                    lines.append('v ' + ' '.join(f'{value/16:.9f}' for value in point))
                u0, v0, u1, v1 = face['uv']
                uv = [(u0,v1),(u1,v1),(u1,v0),(u0,v0)]
                shift = face.get('rotation', 0) // 90
                uv = uv[shift:] + uv[:shift]
                for u, v in uv:
                    lines.append(f'vt {u/width:.9f} {v/height:.9f}')
                lines.append('f ' + ' '.join(f'{i}/{i}' for i in range(vertex_index, vertex_index+4)))
                vertex_index += 4
        (FOLDER / f'{name}.obj').write_text('\n'.join(lines)+'\n', encoding='utf-8')
    (FOLDER / 'assembler.mtl').write_text('\n'.join(materials)+'\n', encoding='utf-8')
    display = model.get('display', {}).copy()
    display.setdefault('gui', {'rotation': [30,225,0], 'translation': [0,-2,0], 'scale': [.4,.4,.4]})
    block_model = {
        'parent': 'minecraft:block/block', 'loader': 'neoforge:obj',
        'model': 'forgermod:models/block/weapon_assembler/assembler.obj',
        'automatic_culling': False, 'shade_quads': True, 'flip_v': False,
        'render_type': 'minecraft:cutout',
        'textures': {f'texture_{i}': f'forgermod:block/weapon_assembler/texture_{i}' for i in textures},
        'display': display,
    }
    block_model['textures']['particle'] = block_model['textures'][f'texture_{textures[0]}']
    def write_model(path, mesh):
        value = dict(block_model, model=f'forgermod:models/block/weapon_assembler/{mesh}.obj')
        path.write_text(json.dumps(value, indent=2)+'\n', encoding='utf-8')

    export_mesh('assembler', visible)
    write_model(ASSETS / 'models/block/weapon_assembler_item.json', 'assembler')
    arms = {}
    base = []
    for cube, ancestors in visible:
        name = next((g['name'][:-4].lower() for g in ancestors if g.get('name', '').endswith('_arm')), None)
        if name is None:
            base.append((cube, ancestors))
        else:
            arms.setdefault(name, []).append((cube, ancestors))
    export_mesh('base', base)
    write_model(ASSETS / 'models/block/weapon_assembler.json', 'base')
    rig = {}
    for name, cubes in arms.items():
        # The twin struts have individual pivots. Average them to obtain the joint axle.
        lower = cubes[:2]
        upper_end = cubes[4:6] if name == 'lamp' else cubes[3:5]
        def average(points):
            return [sum(p[axis] for p in points)/len(points)/16 for axis in range(3)]
        def tips(cubes):
            return [rotate([(c['from'][0]+c['to'][0])/2,c['to'][1],(c['from'][2]+c['to'][2])/2],
                           c.get('origin',[0,0,0]),c.get('rotation',[0,0,0])) for c,_ in cubes]
        base_pivot = average([c['origin'] for c,_ in lower])
        elbow = average(tips(lower))
        wrist = average([cubes[6][0]['origin']]) if name == 'lamp' else average(tips(upper_end))
        split = 6 if name == 'lamp' else 5
        sections = {'lower':lower, 'upper':cubes[2:split], 'tool':cubes[split:8] if name == 'claw' else cubes[split:]}
        if name == 'claw':
            sections.update(jaw0=[cubes[8]],jaw1=[cubes[9]])
        for section, part in sections.items():
            key = f'{name}_{section}'
            export_mesh(key,part)
            write_model(FOLDER / f'{key}.json',key)
        rig[name] = {'base':base_pivot,'elbow':elbow,'wrist':wrist}
        if name == 'claw':
            rig[name]['jaws'] = [[v/16 for v in cubes[index][0]['origin']] for index in (8,9)]
    rendering = ASSETS / 'rendering/weapon_assembler.json'
    rendering.parent.mkdir(parents=True,exist_ok=True)
    rendering.write_text(json.dumps(rig,indent=2)+'\n',encoding='utf-8')
    assert len(base)+sum(len(cubes) for cubes in arms.values()) == len(visible)
    print(f'Exported {len(visible)} visible cubes as base + four hinged arms; complete inventory mesh preserved.')


if __name__ == '__main__':
    main()
