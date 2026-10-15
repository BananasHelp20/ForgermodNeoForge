"""Add names for the generated material progression assets to en_us.json."""

import json
import re
from pathlib import Path

path = Path(__file__).resolve().parents[1] / "src/main/resources/assets/forgermod/lang/en_us.json"
text = path.read_text(encoding="utf-8")
text = re.sub(r'^  "item\.forgermod\.[a-z_]+_tool_axe": [^\n]*\n', '', text, flags=re.MULTILINE)
existing = json.loads(text)
names = {
    "meteoric_iron": "Meteoric Iron",
    "reinforced_iron": "Reinforced Iron",
    "carbon_steel": "Carbon Steel",
    "titanium": "Titanium",
    "tungsten": "Tungsten",
    "damask": "Damask",
    "steel": "Steel",
    "scrap": "Scrap",
    "inanisium": "Inanisium",
    "ignisium": "Ignisium",
    "morsium": "Morsium",
    "somnium": "Somnium",
    "vulnusium": "Vulnusium",
    "lush": "Lush",
    "pulsite": "Pulsite",
    "electrium": "Electrium",
    "taifunite": "Taifunite",
}
entries = {}
for material, display in names.items():
    for suffix, label in (("pickaxe", "Pickaxe"), ("axe", "Axe"), ("hoe", "Hoe"), ("sword", "Sword")):
        key = f"item.forgermod.{material}_{suffix}"
        if key not in existing:
            entries[key] = f"{display} {label}"
for material in ("meteoric_iron", "titanium", "tungsten"):
    entries[f"item.forgermod.{material}_ingot"] = f"{names[material]} Ingot"
    entries[f"item.forgermod.raw_{material}"] = f"Raw {names[material]}"
for key, value in {
    "item.forgermod.dolomite": "Dolomite",
    "item.forgermod.magnesium": "Magnesium",
    "item.forgermod.titanium_sponge": "Titanium Sponge",
    "item.forgermod.tungsten_powder": "Tungsten Powder",
    "item.forgermod.reinforced_iron_billet": "Reinforced Iron Billet",
    "item.forgermod.damask_billet": "Damask Billet",
}.items():
    entries[key] = value
for material, host in (("meteoric_iron", "stone"), ("meteoric_iron", "end"),
                       ("titanium", "stone"), ("titanium", "deepslate"),
                       ("tungsten", "stone"), ("tungsten", "deepslate"), ("dolomite", "stone")):
    entries[f"block.forgermod.{material}_{host}_ore"] = f"{names.get(material, 'Dolomite')} Ore"

entries = {key: value for key, value in entries.items() if key not in existing}
if entries:
    end = text.rfind("\n}")
    assert end >= 0
    insertion = ",\n".join(f"  {json.dumps(key)}: {json.dumps(value)}" for key, value in entries.items())
    text = text[:end] + ",\n" + insertion + text[end:]
path.write_text(text, encoding="utf-8")
