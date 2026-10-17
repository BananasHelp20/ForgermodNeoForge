# Recolor only the approved IngotCraft steel reference; never open an existing destination.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path -Parent $PSScriptRoot
$palette = Get-Content (Join-Path $projectRoot 'markdown/metalPalettes.json') -Raw | ConvertFrom-Json
$colors = @($palette.palettes.reinforced_iron.colors | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
$roles = @{
    '#141414' = 0
    '#252525' = 1
    '#2F2F2F' = 2
    '#434343' = 3
    '#4E4E4E' = 4
    '#525252' = 5
    '#6A6A6A' = 6
    '#777777' = 7
    '#969696' = 8
    '#C8C8C8' = 9
}
$source = [System.Drawing.Bitmap]::new((Join-Path $projectRoot 'src/main/examples/ingotCraft/steel_ingot.png'))
$target = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
try {
    if ($source.Width -ne 16 -or $source.Height -ne 16 -or $colors.Count -ne 10) { throw 'Unexpected reference size or palette.' }
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $pixel = $source.GetPixel($x, $y)
            if ($pixel.A -eq 0) { $target.SetPixel($x, $y, $pixel); continue }
            $hex = '#{0:X2}{1:X2}{2:X2}' -f $pixel.R, $pixel.G, $pixel.B
            if (-not $roles.ContainsKey($hex)) { throw "Unexpected reference color: $hex" }
            $mapped = $colors[$roles[$hex]]
            $target.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, $mapped.R, $mapped.G, $mapped.B))
        }
    }
    $target.Save((Join-Path $projectRoot 'src/main/resources/assets/forgermod/textures/item/metals/ingots/reinforced_iron_ingot.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    $preview = [System.Drawing.Bitmap]::new(816, 300)
    $graphics = [System.Drawing.Graphics]::FromImage($preview)
    $dark = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#20252C'))
    $light = [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml('#E8E4DD'))
    $font = [System.Drawing.Font]::new('Segoe UI', 12)
    try {
        $graphics.Clear($dark.Color)
        $graphics.FillRectangle($light, 544, 0, 272, 300)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $graphics.DrawString('IngotCraft steel reference', $font, $light, 12, 12)
        $graphics.DrawString('Reinforced Iron', $font, $light, 288, 12)
        $graphics.DrawString('Light background', $font, $dark, 560, 12)
        $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(16, 44, 240, 240))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(288, 44, 240, 240))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(560, 44, 240, 240))
        $graphics.DrawImageUnscaled($source, 240, 12)
        $graphics.DrawImageUnscaled($target, 512, 12)
        $graphics.DrawImageUnscaled($target, 784, 12)
        $preview.Save((Join-Path $projectRoot 'markdown/reinforcedIronIngotPreview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $font.Dispose(); $dark.Dispose(); $light.Dispose(); $graphics.Dispose(); $preview.Dispose()
    }
    Write-Output 'Wrote reinforced iron ingot: exact IngotCraft steel pixel layout and alpha, ten neutral iron colors.'
} finally { $target.Dispose(); $source.Dispose() }
