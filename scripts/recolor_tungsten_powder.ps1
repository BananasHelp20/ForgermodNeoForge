# Only the approved vanilla gunpowder reference is read; never opens the old destination.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path -Parent $PSScriptRoot
$palette = Get-Content (Join-Path $projectRoot 'markdown/metalPalettes.json') -Raw | ConvertFrom-Json
$colors = @($palette.palettes.tungsten.colors | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
$roles = @{
    '#2D2D2D' = 0
    '#3E3E3E' = 1
    '#494949' = 2
    '#505050' = 3
    '#545454' = 3
    '#727272' = 4
    '#8A8A8A' = 5
}
$source = [System.Drawing.Bitmap]::new((Join-Path $projectRoot 'src/main/examples/vanilla/gunpowder.png'))
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
    $target.Save((Join-Path $projectRoot 'src/main/resources/assets/forgermod/textures/item/metals/raw/tungsten_powder.png'), [System.Drawing.Imaging.ImageFormat]::Png)
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
        $graphics.DrawString('Vanilla gunpowder', $font, $light, 16, 12)
        $graphics.DrawString('Tungsten Powder', $font, $light, 288, 12)
        $graphics.DrawString('Light background', $font, $dark, 560, 12)
        $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(16, 44, 240, 240))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(288, 44, 240, 240))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(560, 44, 240, 240))
        $graphics.DrawImageUnscaled($source, 240, 12)
        $graphics.DrawImageUnscaled($target, 512, 12)
        $graphics.DrawImageUnscaled($target, 784, 12)
        $preview.Save((Join-Path $projectRoot 'markdown/tungstenPowderPreview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $font.Dispose(); $dark.Dispose(); $light.Dispose(); $graphics.Dispose(); $preview.Dispose()
    }
    Write-Output 'Wrote tungsten powder: vanilla gunpowder pixel layout and transparency, six saved tungsten colors.'
} finally { $target.Dispose(); $source.Dispose() }
