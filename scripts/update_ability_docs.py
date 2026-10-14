"""Refresh weapon/ability docs from the registry catalog exported by AbilityCatalogGameTests."""
import json
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
rows = json.loads((ROOT / "build/ability-catalog.json").read_text(encoding="utf-8"))
lang = json.loads((ROOT / "src/main/resources/assets/forgermod/lang/en_us.json").read_text(encoding="utf-8"))
effects = {f"effect.minecraft.{key}": name for key, name in {
    "glowing": "Glowing", "blindness": "Blindness", "confusion": "Nausea",
    "slowness": "Slowness", "levitation": "Levitation", "wither": "Wither",
    "hunger": "Hunger", "weakness": "Weakness", "darkness": "Darkness",
}.items()}


def component(value):
    if isinstance(value, str):
        return value
    text = value.get("text", lang.get(value.get("translate"), effects.get(value.get("translate"), value.get("translate", ""))))
    for arg in value.get("with", []):
        text = text.replace("%s", component(arg), 1)
    return text


def name(row):
    return lang[row["name"]]


def aliases(ability, row):
    result = [component(ability["name"])]
    if ability["id"] == "tooltips.forgermod.ability.direct_current":
        result.append(lang["tooltips.forgermod.ability.static_field.name"])
    if ability["id"] == "tooltips.forgermod.ability.windcharge" and row["type"] == "Axe":
        result.append(lang["tooltips.forgermod.ability.skybreaker.name"])
    if ability["id"] == "tooltips.forgermod.passive.double_jump":
        result.append(lang["tooltips.forgermod.passive.windwalker.name"])
    return result


families = defaultdict(list)
for row in rows:
    families[row["family"]].append(row)
lines = ["# Abilities by weapon", "", "Available ability names from the actual registered augmentation pools; gemstone variants are listed individually.", ""]
for family in sorted(families):
    lines += ["## " + ("Overgrown / Lush" if family == "Lush" else family), "", "| Weapon | Type | Available abilities |", "| --- | --- | --- |"]
    for row in sorted(families[family], key=lambda r: (r["type"], name(r))):
        available = sorted({n for a in row["abilities"] for n in aliases(a, row)})
        lines.append(f"| {name(row)} | {row['type']} | {', '.join(available) or 'None'} |")
    lines.append("")
(ROOT / "markdown/weaponAbilities.md").write_text("\n".join(lines), encoding="utf-8")

base_names = {(r["family"], r["type"]): name(r) for r in rows if r["family"] != "Other weapons" and not any(r["item"].endswith("_" + gem) for gem in ("ruby", "amber", "amethyst", "jade"))}
abilities = {}
weapons = defaultdict(set)
for row in rows:
    for ability in row["abilities"]:
        if ability["id"] == "augmentation.forgermod.material_hit":
            continue
        abilities[ability["id"]] = ability
        # The gemstone suffix does not change assignment, so list the base family once.
        base = row["item"].split(":", 1)[1]
        for gem in ("ruby", "amber", "amethyst", "jade"):
            if base.endswith("_" + gem):
                base = base[: -len(gem) - 1]
        weapons[ability["id"]].add(base_names.get((row["family"], row["type"]), name(row)))

active = sum(a["active"] for a in abilities.values())
passive = len(abilities) - active
lines = ["# In-game weapon abilities", "", f"English descriptions from the game language file and actual registered pools: {active} active abilities, {passive} passives, and nine material-hit effects. Unimplemented proposals are excluded.", "", "Special weapons learn these abilities through augmentation. Each assignment includes its base, Ruby, Amber, Amethyst and Jade variants. Item descriptions appear while holding Shift; cards use their shorter description where provided.", ""]
for is_active, heading in ((True, "Active abilities"), (False, "Passives")):
    lines += ["## " + heading, ""]
    for id, ability in sorted(abilities.items(), key=lambda item: component(item[1]["name"])):
        if ability["active"] != is_active:
            continue
        assert id in lang and id + ".name" in lang, "Missing ability translation: " + id
        lines += ["### " + component(ability["name"]), "", "**Weapons:** " + ", ".join(sorted(weapons[id])), "", "**Item tooltip:**", "", "> " + lang[id], ""]
        if id + ".card" in lang:
            lines += ["**Augmentation card:**", "", "> " + lang[id + ".card"], ""]
        if id == "tooltips.forgermod.ability.direct_current":
            lines += ["**Super I-IV name (transforms at original IV):** " + lang["tooltips.forgermod.ability.static_field.name"], ""]
        if id == "tooltips.forgermod.ability.windcharge":
            lines += ["**Axe super I-IV name (transforms at original IV):** " + lang["tooltips.forgermod.ability.skybreaker.name"], ""]

        if id == "tooltips.forgermod.passive.double_jump":
            lines += ["**Super I-IV name (transforms at original IV):** " + lang["tooltips.forgermod.passive.windwalker.name"], ""]

# Material duration/amplifier tables are unchanged by the new abilities.
path = ROOT / "markdown/abilityDescriptions.md"
old = path.read_text(encoding="utf-8")
material = old[old.index("## Material-hit effects"):]
path.write_text("\n".join(lines) + material, encoding="utf-8")
print(f"Updated descriptions for {active} active abilities and {passive} passives; assignments for {len(rows)} weapons.")
