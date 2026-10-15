"""Generate the small, palette-matched material sprites used by the progression set.

Run with: python tools/generate_material_textures.py
This is an authoring tool; the generated PNGs are the runtime assets.
"""

from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1] / "src/main/resources/assets/forgermod/textures"
ITEM = ROOT / "item"
BLOCK = ROOT / "block"

PALETTES = {
    "scrap": ("#584334", "#9a7454", "#d4ad79"),
    "meteoric_iron": ("#333d4b", "#8399a8", "#d7e2df"),
    "reinforced_iron": ("#303742", "#87939b", "#d9d4c3"),
    "steel": ("#3b4c5b", "#99b0b9", "#e0e8e0"),
    "damask": ("#4d4256", "#a49ba6", "#ebe0d4"),
    "titanium": ("#47535a", "#abb7b2", "#f0e9cb"),
    "tungsten": ("#292e38", "#78818e", "#c1b5a5"),
    "carbon_steel": ("#202a30", "#687c7d", "#c5d4c9"),
    "inanisium": ("#252041", "#7664ad", "#dac8ff"),
    "ignisium": ("#5b251d", "#d56732", "#ffce6d"),
    "morsium": ("#303b39", "#829e8e", "#d3e4c8"),
    "somnium": ("#3c2d58", "#9775bc", "#e1c3ef"),
    "vulnusium": ("#531d34", "#b94d69", "#f5a9ac"),
    "lush": ("#22412c", "#61a75d", "#c4df83"),
    "pulsite": ("#153f46", "#47a3a0", "#a8e5da"),
    "electrium": ("#594a21", "#d5b64c", "#fff1a7"),
    "taifunite": ("#304d5e", "#74bac8", "#e3f6e7"),
}


def rgba(hex_color):
    return tuple(bytes.fromhex(hex_color[1:])) + (255,)


def save_item(name, pixels):
    image = Image.new("RGBA", (16, 16))
    image.putdata(pixels)
    image.save(ITEM / f"{name}.png")


def recolor(template, name, palette):
    source = Image.open(ITEM / f"{template}.png").convert("RGBA")
    colors = [rgba(c) for c in palette]
    output = []
    for red, green, blue, alpha in source.getdata():
        if not alpha:
            output.append((0, 0, 0, 0))
        else:
            luminance = (red * 3 + green * 6 + blue) / 10
            output.append(colors[0 if luminance < 85 else 1 if luminance < 170 else 2])
    save_item(name, output)


def ingot(name, palette, bands=False):
    image = Image.new("RGBA", (16, 16))
    draw = ImageDraw.Draw(image)
    dark, mid, light = map(rgba, palette)
    draw.polygon([(2, 9), (9, 5), (14, 7), (14, 10), (7, 13), (2, 11)], fill=dark)
    draw.polygon([(3, 8), (9, 4), (14, 6), (13, 8), (7, 11), (2, 10)], fill=mid)
    draw.line([(3, 8), (9, 5), (13, 6)], fill=light, width=1)
    draw.line([(7, 11), (7, 13), (14, 10)], fill=dark, width=1)
    if bands:
        draw.line([(6, 7), (8, 9)], fill=light)
        draw.line([(9, 5), (11, 7)], fill=light)
    image.save(ITEM / f"{name}.png")


def lump(name, palette, variant=0):
    image = Image.new("RGBA", (16, 16))
    draw = ImageDraw.Draw(image)
    dark, mid, light = map(rgba, palette)
    draw.polygon([(3, 5), (8, 3), (13, 6), (14, 10), (10, 13), (4, 12), (2, 9)], fill=dark)
    draw.polygon([(4, 5), (8, 4), (12, 6), (11, 9), (6, 10), (3, 8)], fill=mid)
    draw.line([(5, 6), (8, 5), (10, 6)], fill=light)
    if variant:
        draw.point([(7, 8), (10, 10), (5, 10)], fill=light)
    image.save(ITEM / f"{name}.png")


def tool(name, kind, palette):
    image = Image.new("RGBA", (16, 16))
    draw = ImageDraw.Draw(image)
    dark, mid, light = map(rgba, palette)
    wood, wood_light = rgba("#49342b"), rgba("#aa8155")
    if kind == "sword":
        draw.line([(3, 13), (12, 4)], fill=dark, width=3)
        draw.line([(3, 12), (5, 10)], fill=wood_light, width=2)
        draw.line([(6, 9), (12, 3)], fill=mid, width=2)
        draw.line([(7, 8), (12, 3)], fill=light)
        draw.line([(4, 8), (8, 12)], fill=dark, width=2)
        draw.point((2, 14), fill=light)
    else:
        draw.line([(4, 13), (11, 5)], fill=wood, width=3)
        draw.line([(5, 12), (10, 6)], fill=wood_light)
        if kind == "pickaxe":
            draw.polygon([(2, 4), (5, 2), (12, 3), (14, 6), (10, 5), (6, 5)], fill=dark)
            draw.line([(3, 3), (11, 4), (13, 5)], fill=mid, width=2)
            draw.line([(4, 3), (10, 4)], fill=light)
        elif kind == "axe":
            draw.polygon([(7, 2), (13, 3), (15, 5), (12, 9), (8, 8), (10, 5)], fill=dark)
            draw.polygon([(8, 3), (12, 4), (14, 5), (12, 8), (9, 7)], fill=mid)
            draw.line([(8, 3), (12, 4), (14, 5)], fill=light)
        else:
            draw.polygon([(4, 3), (8, 2), (14, 3), (13, 5), (7, 5), (5, 8)], fill=dark)
            draw.line([(5, 3), (13, 4)], fill=mid, width=2)
            draw.line([(6, 3), (12, 3)], fill=light)
    image.save(ITEM / f"{name}.png")


def ore(name, host, palette):
    base = Image.open(BLOCK / f"jade_{host}_ore.png").convert("RGBA")
    dark, mid, light = map(rgba, palette)
    output = []
    for x, y, (red, green, blue, alpha) in [
        (x, y, base.getpixel((x, y))) for y in range(16) for x in range(16)
    ]:
        if green > red * 1.22 and green > blue * 1.1:
            output.append(light if (x + y) % 4 == 0 else mid if (x + y) % 3 else dark)
        else:
            output.append((red, green, blue, alpha))
    image = Image.new("RGBA", (16, 16))
    image.putdata(output)
    image.save(BLOCK / f"{name}_{host}_ore.png")


for material, palette in PALETTES.items():
    tool(material + "_pickaxe", "pickaxe", palette)
    tool(material + "_axe", "axe", palette)
    tool(material + "_hoe", "hoe", palette)
    if material not in {"scrap", "reinforced_iron", "steel", "damask"}:
        tool(material + "_sword", "sword", palette)

for material in ("meteoric_iron", "titanium", "tungsten"):
    ingot(material + "_ingot", PALETTES[material])
    lump("raw_" + material, PALETTES[material], 1)

ingot("reinforced_iron_ingot", PALETTES["reinforced_iron"], True)
tool("reinforced_iron_sword", "sword", PALETTES["reinforced_iron"])
ingot("reinforced_iron_billet", PALETTES["reinforced_iron"])
ingot("damask_billet", PALETTES["damask"], True)
lump("dolomite", ("#625d55", "#a49a85", "#e4d9bc"))
lump("magnesium", ("#536273", "#a9bfc1", "#edf5e9"), 1)
lump("titanium_sponge", PALETTES["titanium"], 1)
lump("tungsten_powder", PALETTES["tungsten"], 1)

ore("meteoric_iron", "stone", PALETTES["meteoric_iron"])
ore("meteoric_iron", "end", PALETTES["meteoric_iron"])
ore("titanium", "stone", PALETTES["titanium"])
ore("titanium", "deepslate", PALETTES["titanium"])
ore("tungsten", "stone", PALETTES["tungsten"])
ore("tungsten", "deepslate", PALETTES["tungsten"])
ore("dolomite", "stone", ("#625d55", "#a49a85", "#e4d9bc"))

block = Image.new("RGBA", (16, 16), rgba("#303742"))
draw = ImageDraw.Draw(block)
draw.rectangle((1, 1, 14, 14), fill=rgba("#87939b"), outline=rgba("#d9d4c3"))
draw.line((2, 2, 13, 13), fill=rgba("#424d5b"), width=2)
draw.line((13, 2, 2, 13), fill=rgba("#424d5b"), width=2)
draw.rectangle((5, 5, 10, 10), fill=rgba("#303742"), outline=rgba("#d9d4c3"))
for pos in ((3, 3), (12, 3), (3, 12), (12, 12)):
    draw.point(pos, fill=rgba("#e9e3ce"))
block.save(BLOCK / "reinforced_iron_block.png")
