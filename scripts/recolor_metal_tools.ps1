param(
    [switch]$VerifyOnly,
    [switch]$ReplaceCarbonSteelShovel,
    [switch]$RefreshGeneratedTextures,
    [switch]$RefreshDamaskTextures,
    [string]$JarPath
)

# User-approved deterministic recoloring, not AI-generated artwork. Existing completed art is protected.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$toolProject = Split-Path -Parent $PSScriptRoot
$toolRoot = Join-Path $toolProject 'src/main/resources/assets/forgermod/textures'
$toolTypes = @('sword', 'pickaxe', 'axe', 'hoe', 'shovel')
$toolMaterials = @('scrap', 'meteoric_iron', 'reinforced_iron', 'steel', 'damask', 'titanium', 'tungsten', 'carbon_steel')

# Six ordered metal shades sampled from the actual completed swords/tools or ingots.
# Deliberately do not copy isolated stray colors from the hand-authored reference sprites.
$toolPalettes = @{
    scrap = @('item/metals/ingots/scrap_ingot', '#28130D', '#4A2C23', '#5D342C', '#7E6059', '#95867E', '#D8D8D8')
    meteoric_iron = @('item/metals/ingots/meteoric_iron_ingot', '#29272B', '#4C494A', '#73716D', '#9D9A8D', '#CCC7AE', '#F3E9CB')
    reinforced_iron = @('item/metals/ingots/reinforced_iron_ingot', '#20252A', '#434B52', '#59636B', '#7B868D', '#A8B2B7', '#F0F3F4')
    steel = @('item/metals/weapons/normal/steel_sword', '#13171B', '#1C2023', '#20282C', '#252F33', '#323B3E', '#3D4548')
    damask = @('item/metals/weapons/normal/damask_knife', '#111111', '#312929', '#3B393B', '#5E5E5E', '#737173', '#828282')
    titanium = @('item/metals/weapons/normal/titanium_sword', '#302D3B', '#4C475C', '#555066', '#6F687F', '#9892A8', '#C7C3D2')
    tungsten = @('item/metals/weapons/normal/tungsten_sword', '#1C222B', '#303A46', '#4B5967', '#697B89', '#8298A8', '#A7BBC9')
    carbon_steel = @('item/metals/tools/carbon_steel_pickaxe', '#231012', '#2B2424', '#3B393B', '#4F4F4F', '#666566', '#828282')
}
$toolMetalRoles = @{
    '#181818' = 0; '#444444' = 1; '#6B6B6B' = 2; '#969696' = 2
    '#BEBEBE' = 3; '#C1C1C1' = 3; '#D8D8D8' = 4; '#FFFFFF' = 5
}
$toolWoodRoles = @{ '#281E0B' = 0; '#493615' = 1; '#684E1E' = 2; '#896727' = 3 }

function Get-ToolHex([System.Drawing.Color]$Color) {
    return '#{0:X2}{1:X2}{2:X2}' -f $Color.R, $Color.G, $Color.B
}
function Get-ToolColors([System.Drawing.Bitmap]$Bitmap) {
    $found = @{}
    for ($y = 0; $y -lt $Bitmap.Height; $y++) {
        for ($x = 0; $x -lt $Bitmap.Width; $x++) {
            $pixel = $Bitmap.GetPixel($x, $y)
            if ($pixel.A -gt 0) { $found[(Get-ToolHex $pixel)] = $true }
        }
    }
    return $found
}
function Assert-ToolPixels([System.Drawing.Bitmap]$Actual, [System.Drawing.Bitmap]$Expected, [string]$Name) {
    if ($Actual.Width -ne 16 -or $Actual.Height -ne 16) { throw "$Name is not 16x16." }
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $a = $Actual.GetPixel($x, $y); $e = $Expected.GetPixel($x, $y)
            if ($a.A -ne $e.A -or ($a.A -gt 0 -and $a.ToArgb() -ne $e.ToArgb())) {
                throw "$Name differs at ($x,$y). Existing artwork will not be overwritten."
            }
        }
    }
}

$toolHandle = [System.Drawing.Bitmap]::new((Join-Path $toolRoot 'item/other/weapon_parts/advanced_handle.png'))
try {
    $toolWoodHex = @((Get-ToolColors $toolHandle).Keys | Sort-Object {
        $color = [System.Drawing.ColorTranslator]::FromHtml($_)
        .2126 * $color.R + .7152 * $color.G + .0722 * $color.B
    })
    if ($toolWoodHex.Count -ne 4) { throw 'Advanced Handle reference changed: review its four wood shade roles first.' }
    $toolWoodColors = @($toolWoodHex | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
} finally { $toolHandle.Dispose() }

# Only these 31 requested sprites may be created/replaced. All other existing art is left alone.
$toolJobs = @(
    foreach ($material in $toolMaterials) {
        foreach ($type in $toolTypes) {
            if ($material -eq 'carbon_steel' -and $type -ne 'shovel') { continue }
            if ($type -eq 'sword' -and $material -notin @('meteoric_iron', 'reinforced_iron')) { continue }
            $folder = if ($type -eq 'sword') { 'weapons/normal' } else { 'tools' }
            @{ Material = $material; Type = $type; Path = "item/metals/$folder/${material}_$type.png" }
        }
    }
)
$toolTargets = @($toolJobs | ForEach-Object { [System.IO.Path]::GetFullPath((Join-Path $toolRoot $_.Path)) })
$toolProtected = @{}
Get-ChildItem -LiteralPath $toolRoot -Filter '*.png' -Recurse | ForEach-Object {
    if ($_.FullName -notin $toolTargets) { $toolProtected[$_.FullName] = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash }
}
$toolWritten = 0
foreach ($job in $toolJobs) {
    $entry = $toolPalettes[$job.Material]
    $reference = [System.Drawing.Bitmap]::new((Join-Path $toolRoot ($entry[0] + '.png')))
    try {
        $referenceColors = Get-ToolColors $reference
        foreach ($hex in $entry[1..6]) {
            if (-not $referenceColors.ContainsKey($hex)) { throw "Palette color $hex missing from $($entry[0])." }
        }
    } finally { $reference.Dispose() }
    $metalColors = @($entry[1..6] | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
    $iron = [System.Drawing.Bitmap]::new((Join-Path $toolRoot "examples/vanilla/iron_$($job.Type).png"))
    $result = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
    try {
        if ($iron.Width -ne 16 -or $iron.Height -ne 16) { throw 'Iron template must be 16x16.' }
        $woodPoints = @(); $metalPoints = @()
        for ($y = 0; $y -lt 16; $y++) {
            for ($x = 0; $x -lt 16; $x++) {
                $pixel = $iron.GetPixel($x, $y)
                if ($pixel.A -eq 0) { continue }
                $hex = Get-ToolHex $pixel
                if ($toolWoodRoles.ContainsKey($hex)) { $woodPoints += ,@($x, $y) }
                elseif ($hex -ne '#181818' -and $toolMetalRoles.ContainsKey($hex)) { $metalPoints += ,@($x, $y) }
                elseif ($hex -ne '#181818') { throw "Unmapped iron color $hex." }
            }
        }
        for ($y = 0; $y -lt 16; $y++) {
            for ($x = 0; $x -lt 16; $x++) {
                $pixel = $iron.GetPixel($x, $y)
                if ($pixel.A -eq 0) { $result.SetPixel($x, $y, $pixel); continue }
                $hex = Get-ToolHex $pixel
                if ($toolWoodRoles.ContainsKey($hex)) { $mapped = $toolWoodColors[$toolWoodRoles[$hex]] }
                elseif ($hex -eq '#181818') {
                    # Vanilla shares one black outline between wood and metal. Assign it to
                    # the nearest material region, keeping the exact original outline mask.
                    $woodDistance = 1000; $metalDistance = 1000
                    foreach ($point in $woodPoints) { $woodDistance = [Math]::Min($woodDistance, ($x-$point[0])*($x-$point[0])+($y-$point[1])*($y-$point[1])) }
                    foreach ($point in $metalPoints) { $metalDistance = [Math]::Min($metalDistance, ($x-$point[0])*($x-$point[0])+($y-$point[1])*($y-$point[1])) }
                    $mapped = if ($woodDistance -lt $metalDistance) { $toolWoodColors[0] } else { $metalColors[0] }
                } else {
                    $role = $toolMetalRoles[$hex]
                    if ($job.Material -eq 'damask' -and $role -ge 2) {
                        # The knife alternates bright/dark diagonal bands around a darker
                        # middle seam. Carry that small-scale pattern into the metal only;
                        # keep the original outline, wood, alpha and pixel positions.
                        $role = if (($x+$y)%2 -ne 0) { 2 } elseif ($x%2 -eq 0) { 5 } else { 3 }
                    }
                    $mapped = $metalColors[$role]
                }
                $result.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, $mapped.R, $mapped.G, $mapped.B))
            }
        }
        $destination = Join-Path $toolRoot $job.Path
        # Explicit refresh is restricted to the 31 generated jobs, never the protected finished art.
        $replace = $RefreshGeneratedTextures -or ($RefreshDamaskTextures -and $job.Material -eq 'damask') -or ($ReplaceCarbonSteelShovel -and $job.Material -eq 'carbon_steel' -and $job.Type -eq 'shovel')
        if (-not $VerifyOnly -and (-not (Test-Path -LiteralPath $destination) -or $replace)) {
            [System.IO.Directory]::CreateDirectory((Split-Path -Parent $destination)) | Out-Null
            $result.Save($destination, [System.Drawing.Imaging.ImageFormat]::Png)
            $toolWritten++
        }
        if (-not (Test-Path -LiteralPath $destination)) { throw "Missing sprite $destination." }
        $actual = [System.Drawing.Bitmap]::new($destination)
        try {
            Assert-ToolPixels $actual $result $job.Path
            for ($y = 0; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    if ($actual.GetPixel($x,$y).A -ne $iron.GetPixel($x,$y).A) { throw 'Iron silhouette/transparency changed.' }
                }
            }
        } finally { $actual.Dispose() }
    } finally { $iron.Dispose(); $result.Dispose() }
}
foreach ($path in $toolProtected.Keys) {
    if ((Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash -ne $toolProtected[$path]) { throw "Protected artwork changed: $path" }
}

# Every ordinary tool, including the untouched completed sprites, must actually use its own art.
$toolArchive = $null
if ($JarPath) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $toolArchive = [System.IO.Compression.ZipFile]::OpenRead([System.IO.Path]::GetFullPath($JarPath))
}
try {
    foreach ($material in $toolMaterials) {
        foreach ($type in $toolTypes) {
            $id = "${material}_$type"
            $folder = if ($type -eq 'sword') { 'weapons/normal' } else { 'tools' }
            $textureId = "item/metals/$folder/$id"
            $modelPath = "assets/forgermod/models/item/$id.json"
            $texturePath = "assets/forgermod/textures/$textureId.png"
            if ($toolArchive) {
                $modelEntry = $toolArchive.GetEntry($modelPath)
                $textureEntry = $toolArchive.GetEntry($texturePath)
                if (-not $modelEntry -or -not $textureEntry) { throw "Packaged model/texture missing for $id." }
                $reader = [System.IO.StreamReader]::new($modelEntry.Open())
                try { $model = $reader.ReadToEnd() | ConvertFrom-Json } finally { $reader.Dispose() }
                $stream = $textureEntry.Open(); $sha = [System.Security.Cryptography.SHA256]::Create()
                try { $packedHash = [System.BitConverter]::ToString($sha.ComputeHash($stream)).Replace('-', '') }
                finally { $stream.Dispose(); $sha.Dispose() }
                $sourceHash = (Get-FileHash -LiteralPath (Join-Path $toolRoot "$textureId.png") -Algorithm SHA256).Hash
                # toolRoot already ends in textures, whereas textureId starts in item/.
                if ($packedHash -ne $sourceHash) { throw "Packaged pixels differ for $id." }
            } else {
                $models = @(foreach ($root in @('src/main/resources', 'src/generated/resources')) {
                    $candidate = Join-Path $toolProject "$root/$modelPath"
                    if (Test-Path -LiteralPath $candidate) { $candidate }
                })
                if ($models.Count -ne 1) { throw "Expected exactly one model for $id, found $($models.Count)." }
                $model = Get-Content -LiteralPath $models[0] -Raw | ConvertFrom-Json
            }
            if ($model.parent -ne 'minecraft:item/handheld' -or $model.textures.layer0 -ne "forgermod:$textureId") {
                throw "Model for $id still uses a fallback or wrong sprite."
            }
        }
    }
} finally { if ($toolArchive) { $toolArchive.Dispose() } }

# Nearest-neighbor contact sheet. All preview output stays outside packaged resources.
$toolPreviewPath = Join-Path $toolProject 'build/metal-tools-preview.png'
[System.IO.Directory]::CreateDirectory((Split-Path -Parent $toolPreviewPath)) | Out-Null
$toolPreview = [System.Drawing.Bitmap]::new(704, 1024)
$toolGraphics = [System.Drawing.Graphics]::FromImage($toolPreview)
$toolFont = [System.Drawing.Font]::new('Segoe UI', 11)
$toolBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
try {
    $toolGraphics.Clear([System.Drawing.ColorTranslator]::FromHtml('#25282F'))
    $toolGraphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
    $toolGraphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
    for ($column = 0; $column -lt 5; $column++) { $toolGraphics.DrawString($toolTypes[$column], $toolFont, $toolBrush, 160+104*$column, 8) }
    $rows = @('iron') + $toolMaterials
    for ($row = 0; $row -lt $rows.Count; $row++) {
        $material = $rows[$row]; $top = 40+106*$row
        $toolGraphics.DrawString($material, $toolFont, $toolBrush, 8, $top+25)
        for ($column = 0; $column -lt 5; $column++) {
            $type = $toolTypes[$column]
            $folder = if ($type -eq 'sword') { 'weapons/normal' } else { 'tools' }
            $path = if ($material -eq 'iron') { "examples/vanilla/iron_$type.png" } else { "item/metals/$folder/${material}_$type.png" }
            $icon = [System.Drawing.Bitmap]::new((Join-Path $toolRoot $path))
            try { $toolGraphics.DrawImage($icon, [System.Drawing.Rectangle]::new(160+104*$column, $top, 80, 80)) } finally { $icon.Dispose() }
        }
    }
    $toolPreview.Save($toolPreviewPath, [System.Drawing.Imaging.ImageFormat]::Png)
} finally { $toolGraphics.Dispose(); $toolPreview.Dispose(); $toolFont.Dispose(); $toolBrush.Dispose() }
Write-Output "Verified $($toolJobs.Count) sprites (16x16, exact iron alpha/shapes, metal shades and Advanced Handle wood); wrote $toolWritten."
Write-Output "Protected $($toolProtected.Count) existing PNGs, including completed swords and carbon-steel pickaxe/axe/hoe."
Write-Output "Verified all 40 ordinary-tool model bindings$(if ($JarPath) { ' and packaged PNG hashes' })."
Write-Output "Preview: $toolPreviewPath"
