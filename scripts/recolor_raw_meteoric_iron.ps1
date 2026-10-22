# Read only the approved crushed-copper reference and saved palette, never the old target.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path -Parent $PSScriptRoot
$palette = Get-Content (Join-Path $projectRoot 'markdown/metalPalettes.json') -Raw | ConvertFrom-Json
$colors = @($palette.palettes.meteoric_iron.colors | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
if ($colors.Count -ne 6) { throw 'Expected six meteoric iron palette colors.' }
$map = @{
    '#243B34'=0; '#2A5447'=1; '#3A7463'=2; '#4D8D79'=3; '#4FBA98'=4
    '#635B40'=1; '#6D3421'=0; '#84E2C5'=5; '#883F26'=1; '#946D3D'=2
    '#A54926'=2; '#C95E35'=3; '#E97A52'=3; '#FC9982'=4; '#FEC3B6'=5
}
$source = [System.Drawing.Bitmap]::new((Join-Path $projectRoot 'src/main/examples/create/crushed_raw_copper.png'))
$target = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
try {
    if ($source.Width -ne 16 -or $source.Height -ne 16) { throw 'Reference must be 16x16.' }
    for ($y = 0; $y -lt 16; $y++) {
        for ($x = 0; $x -lt 16; $x++) {
            $pixel = $source.GetPixel($x, $y)
            if ($pixel.A -eq 0) { $target.SetPixel($x, $y, $pixel); continue }
            $hex = '#{0:X2}{1:X2}{2:X2}' -f $pixel.R, $pixel.G, $pixel.B
            if (-not $map.ContainsKey($hex)) { throw "Unexpected reference color: $hex" }
            $mapped = $colors[$map[$hex]]
            $target.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, $mapped.R, $mapped.G, $mapped.B))
        }
    }
    $target.Save((Join-Path $projectRoot 'src/main/resources/assets/forgermod/textures/item/metals/raw/raw_meteoric_iron.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    $preview = [System.Drawing.Bitmap]::new(720, 300)
    $graphics = [System.Drawing.Graphics]::FromImage($preview)
    $font = [System.Drawing.Font]::new('Segoe UI', 12)
    try {
        $graphics.Clear([System.Drawing.ColorTranslator]::FromHtml('#20252C'))
        $graphics.FillRectangle([System.Drawing.Brushes]::WhiteSmoke, 480, 0, 240, 300)
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
        $graphics.DrawString('Crushed copper reference', $font, [System.Drawing.Brushes]::White, 8, 12)
        $graphics.DrawString('Raw meteoric iron', $font, [System.Drawing.Brushes]::White, 252, 12)
        $graphics.DrawString('Light background', $font, [System.Drawing.Brushes]::Black, 492, 12)
        $graphics.DrawImage($source, [System.Drawing.Rectangle]::new(24, 48, 192, 192))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(264, 48, 192, 192))
        $graphics.DrawImage($target, [System.Drawing.Rectangle]::new(504, 48, 192, 192))
        $graphics.DrawImageUnscaled($source, 24, 264)
        $graphics.DrawImageUnscaled($target, 264, 264)
        $graphics.DrawImageUnscaled($target, 504, 264)
        $preview.Save((Join-Path $projectRoot 'markdown/rawMeteoricIronPreview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $font.Dispose(); $graphics.Dispose(); $preview.Dispose() }
} finally { $target.Dispose(); $source.Dispose() }
Write-Output 'Saved raw meteoric iron using crushed copper layout and the saved meteoric iron palette.'
