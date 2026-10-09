"""Package the authored Ancient Grave as a mandatory part of each city start.
Requires nbtlib (python -m pip install nbtlib). Run from the repository root.
The vanilla center remains referenced, rather than copied or replaced.
"""
from pathlib import Path
import copy, json, nbtlib, shutil
source = Path('run/saves/Structure/generated/minecraft/structures/forgermod_ancient_grave_v2.nbt')
output = Path('src/main/resources/data/forgermod/structure')
output.mkdir(parents=True, exist_ok=True)
shutil.copyfile(source, output/'ancient_grave.nbt')
grave = nbtlib.load(source)
offset = (18, 0, 10)
for block in grave['blocks']:
    block['pos'] = nbtlib.List[nbtlib.Int]([int(v)+d for v,d in zip(block['pos'], offset)])
for entity in grave.get('entities', []):
    entity['pos'] = nbtlib.List[nbtlib.Double]([float(v)+d for v,d in zip(entity['pos'],offset)])
    entity['blockPos'] = nbtlib.List[nbtlib.Int]([int(v)+d for v,d in zip(entity['blockPos'],offset)])
grave['size'] = nbtlib.List[nbtlib.Int]([int(v)+d for v,d in zip(grave['size'],offset)])
grave.save(output/'ancient_grave_city_room.nbt')
def single(location, processors):
    return {'element_type':'minecraft:single_pool_element','location':location,'processors':processors,'projection':'rigid'}
pool = {'fallback':'minecraft:empty','elements':[
    {'weight':1,'element': {'element_type':'minecraft:list_pool_element','projection':'rigid','elements':[
        single(f'minecraft:ancient_city/city_center/city_center_{i}','minecraft:ancient_city_start_degradation'),
        single('forgermod:ancient_grave_city_room','minecraft:empty')
    ]}} for i in range(1,4)]}
p=Path('src/main/resources/data/forgermod/worldgen/template_pool/ancient_city_start.json'); p.parent.mkdir(parents=True, exist_ok=True); p.write_text(json.dumps(pool,indent=2)+'\n')
# Preserve vanilla city settings; replace only its starting pool.
import zipfile
with zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.93-client-extra-aka-minecraft-resources.jar') as z:
    structure=json.loads(z.read('data/minecraft/worldgen/structure/ancient_city.json'))
structure['start_pool']='forgermod:ancient_city_start'
p=Path('src/main/resources/data/minecraft/worldgen/structure/ancient_city.json'); p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(structure,indent=2)+'\n')
print('Packaged Ancient Grave, dimensions', list(grave['size']))
