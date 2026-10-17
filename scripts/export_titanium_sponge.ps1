# Recolor only the approved Create crushed raw zinc reference; never open the old sponge texture.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path -Parent $PSScriptRoot
$palette = Get-Content (Join-Path $projectRoot 'markdown/metalPalettes.json') -Raw | ConvertFrom-Json
$colors = @($palette.palettes.titanium.colors | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
$roles = @{
    '#213B3F' = 0
    '#31544E' = 1
    '#506D63' = 2
    '#788273' = 2
    '#929B87' = 3
    '#A5C0A0' = 3
    '#B9E9C1' = 4
    '#D6FFDC' = 4
    '#FFFFFF' = 5
}
$source = [System.Drawing.Bitmap]::new((Join-Path $projectRoot 'src/main/examples/create/crushed_raw_zinc.png'))
$target = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
try {
    if ($source.Width -ne 16 -or $source.Height -ne 16 -or $colors.Count -ne 6) { throw 'Unexpected reference size or palette.' }
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
    $target.Save((Join-Path $projectRoot 'src/main/resources/assets/forgermod/textures/item/metals/raw/titanium_sponge.png'), [System.Drawing.Imaging.ImageFormat]::Png)
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
        $graphics.DrawString('Crushed raw zinc reference', $font, $light, 12, 12)
        $graphics.DrawString('Titanium Sponge', $font, $light, 288, 12)
        $graphics.DrawString('Light background', $font, $dark, 560, 12)
        $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(16, 44, 240, 240))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(288, 44, 240, 240))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(560, 44, 240, 240))
        $graphics.DrawImageUnscaled($source, 240, 12)
        $graphics.DrawImageUnscaled($target, 512, 12)
        $graphics.DrawImageUnscaled($target, 784, 12)
        $preview.Save((Join-Path $projectRoot 'markdown/titaniumSpongeCrushedZincPreview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $font.Dispose(); $dark.Dispose(); $light.Dispose(); $graphics.Dispose(); $preview.Dispose()
    }
    Write-Output 'Wrote titanium sponge: exact crushed raw zinc layout and alpha, six saved titanium colors.'
} finally { $target.Dispose(); $source.Dispose() }
