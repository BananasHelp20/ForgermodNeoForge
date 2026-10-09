"""Package the authored Ancient Grave for collision-aware Ancient City generation.
Run from the repository root after Gradle has created Minecraft artifacts.
The vanilla center remains referenced, rather than copied or replaced.
"""
from pathlib import Path
import json, shutil
source = Path('run/saves/Structure/generated/minecraft/structures/forgermod_ancient_grave_v2.nbt')
output = Path('src/main/resources/data/forgermod/structure')
output.mkdir(parents=True, exist_ok=True)
shutil.copyfile(source, output/'ancient_grave.nbt')
# Preserve vanilla city settings and pool; use the collision-aware structure type.
import zipfile
with zipfile.ZipFile('build/moddev/artifacts/neoforge-21.1.93-client-extra-aka-minecraft-resources.jar') as z:
    structure=json.loads(z.read('data/minecraft/worldgen/structure/ancient_city.json'))
structure['type']='forgermod:ancient_city_with_grave'
p=Path('src/main/resources/data/minecraft/worldgen/structure/ancient_city.json'); p.parent.mkdir(parents=True,exist_ok=True); p.write_text(json.dumps(structure,indent=2)+'\n')
print('Packaged Ancient Grave for collision-aware city placement.')
