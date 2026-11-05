"""Validate the two baked display models and render a geometry preview (not an in-game screenshot)."""
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageStat

ROOT = Path(__file__).resolve().parent.parent
ASSETS = ROOT / 'src/main/resources/assets/forgermod'
IDS = ('ancient_dagger_pedestal', 'ancient_axe_stand')


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def texture_path(name):
    namespace, name = name.split(':')
    if namespace == 'forgermod':
        return ASSETS / 'textures' / (name + '.png')
    return ASSETS / 'textures/examples/vanilla' / (name.rsplit('/', 1)[-1] + '.png')


def check():
    for block_id in IDS:
        variants = read(ASSETS / f'blockstates/{block_id}.json')['variants']
        assert len(variants) == 8
        for direction, rotation in zip(('north', 'east', 'south', 'west'), (0, 90, 180, 270)):
            for full in (False, True):
                variant = variants[f'facing={direction},full={str(full).lower()}']
                assert variant['y'] == rotation
                assert variant['model'] == f'forgermod:block/{block_id}' + ('' if full else '_empty')
        for full in (False, True):
            suffix = '' if full else '_empty'
            model = read(ASSETS / f'models/block/{block_id}{suffix}.json')
            heights = []
            for cube in model['elements']:
                assert all(-16 <= low < high <= 32 for low, high in zip(cube['from'], cube['to']))
                assert set(cube['faces']) == {'north', 'south', 'east', 'west', 'up', 'down'}
                for face in cube['faces'].values():
                    assert texture_path(model['textures'][face['texture'][1:]]).is_file()
                    assert len(face['uv']) == 4 and all(0 <= value <= 16 for value in face['uv'])
                heights.append(cube['to'][1])
            assert max(heights) == ((27 if full else 18) if 'axe' in block_id else (10.25 if full else 9.5))
        assert read(ASSETS / f'models/item/{block_id}.json')['parent'] == f'forgermod:block/{block_id}'
        loot = read(ROOT / f'src/generated/resources/data/forgermod/loot_table/blocks/{block_id}.json')
        assert loot['functions'][0] == {'function': 'minecraft:copy_state', 'block': f'forgermod:{block_id}', 'properties': ['full']}
    print('Validated 16 facing/full variants, four model geometries/textures, two item models and claim-state loot.')


def preview():
    sheet = Image.new('RGB', (1280, 500), '#262a32')
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 16)
    for index, (block_id, full) in enumerate((i, f) for i in IDS for f in (True, False)):
        model = read(ASSETS / f'models/block/{block_id}{"" if full else "_empty"}.json')
        polygons = []
        colors = {}
        for key, name in model['textures'].items():
            with Image.open(texture_path(name)).convert('RGBA').convert('RGB') as image:
                colors[key] = tuple(int(c) for c in ImageStat.Stat(image).mean)
        for cube in model['elements']:
            x0, y0, z0 = cube['from']; x1, y1, z1 = cube['to']
            for face, points, light in (
                ('east', [(x1,y0,z0),(x1,y0,z1),(x1,y1,z1),(x1,y1,z0)], .72),
                ('south', [(x0,y0,z1),(x1,y0,z1),(x1,y1,z1),(x0,y1,z1)], .9),
                ('up', [(x0,y1,z0),(x1,y1,z0),(x1,y1,z1),(x0,y1,z1)], 1.2),
            ):
                color = colors[cube['faces'][face]['texture'][1:]]
                depth = sum(x+y+z for x,y,z in points)/4
                projected = [(index*320+160+(x-z)*8.5, 430+(x+z-16)*4.25-y*11) for x,y,z in points]
                polygons.append((depth, projected, tuple(min(255, int(c*light)) for c in color)))
        for _, points, color in sorted(polygons, key=lambda p:p[0]):
            draw.polygon(points, fill=color, outline='#17191d')
        draw.text((index*320+14, 20), ('Dagger pedestal' if 'dagger' in block_id else 'Axe stand') + (' — full' if full else ' — claimed'), font=font, fill='white')
    draw.text((20,475), 'Geometry preview using existing textures; final lighting/UV appearance must be checked in Minecraft.', font=font, fill='#b7bdc8')
    destination = ROOT / 'build/relic-displays-preview.png'
    destination.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(destination)
    print(destination)


if __name__ == '__main__':
    check()
    preview()
