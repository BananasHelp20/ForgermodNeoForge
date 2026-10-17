# Recolor only the approved steel reference. Existing destination textures are never read.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path -Parent $PSScriptRoot
$paletteFile = Join-Path $projectRoot 'markdown/metalPalettes.json'
$paletteData = Get-Content -LiteralPath $paletteFile -Raw | ConvertFrom-Json
$referencePath = Join-Path $projectRoot 'src/main/resources/assets/forgermod/textures/item/metals/ingots/steel_ingot.png'
$reference = [System.Drawing.Bitmap]::new($referencePath)
$colorRoles = @{
    '#090B0D' = 0
    '#13171B' = 1
    '#1C2023' = 2
    '#20282C' = 2
    '#252F33' = 3
    '#323B3E' = 4
    '#3D4548' = 5
}
# Meteoric iron uses IngotCraft lead; reproduce it with recolor_meteoric_iron_ingot.ps1.
$materialNames = @('tungsten', 'titanium')
$preview = [System.Drawing.Bitmap]::new(672, 300)
$graphics = [System.Drawing.Graphics]::FromImage($preview)
$graphics.Clear([System.Drawing.ColorTranslator]::FromHtml('#20252C'))
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
$font = [System.Drawing.Font]::new('Segoe UI', 12)
$brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
try {
    if ($reference.Width -ne 16 -or $reference.Height -ne 16) { throw 'Reference must be 16x16.' }
    $graphics.DrawString('Steel reference', $font, $brush, 12, 12)
    $graphics.DrawImage($reference, [System.Drawing.Rectangle]::new(16, 48, 192, 192))
    $graphics.DrawImageUnscaled($reference, 16, 264)
    for ($materialIndex = 0; $materialIndex -lt $materialNames.Count; $materialIndex++) {
        $material = $materialNames[$materialIndex]
        $colors = @($paletteData.palettes.$material.colors | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
        if ($colors.Count -ne 6) { throw "Expected six colors for $material." }
        $texture = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            for ($y = 0; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    $pixel = $reference.GetPixel($x, $y)
                    if ($pixel.A -eq 0) { $texture.SetPixel($x, $y, $pixel); continue }
                    $hex = '#{0:X2}{1:X2}{2:X2}' -f $pixel.R, $pixel.G, $pixel.B
                    if (-not $colorRoles.ContainsKey($hex)) { throw "Unexpected reference color $hex." }
                    $mapped = $colors[$colorRoles[$hex]]
                    $texture.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, $mapped.R, $mapped.G, $mapped.B))
                }
            }
            # Write the new PNG directly, without opening or sampling the old texture.
            $destination = Join-Path $projectRoot "src/main/resources/assets/forgermod/textures/item/metals/ingots/${material}_ingot.png"
            $texture.Save($destination, [System.Drawing.Imaging.ImageFormat]::Png)
            $offset = ($materialIndex + 1) * 224
            $label = (Get-Culture).TextInfo.ToTitleCase($material.Replace('_', ' '))
            $graphics.DrawString($label, $font, $brush, $offset + 12, 12)
            $graphics.DrawImage($texture, [System.Drawing.Rectangle]::new($offset + 16, 48, 192, 192))
            $graphics.DrawImageUnscaled($texture, $offset + 16, 264)
            for ($i = 0; $i -lt 6; $i++) {
                $swatch = [System.Drawing.SolidBrush]::new($colors[$i])
                try { $graphics.FillRectangle($swatch, $offset + 48 + $i * 26, 264, 24, 16) }
                finally { $swatch.Dispose() }
            }
            Write-Output "Saved $material ingot: exact 16x16 reference silhouette, alpha and pixel positions; six palette colors."
        } finally { $texture.Dispose() }
    }
    $preview.Save((Join-Path $projectRoot 'markdown/metalIngotPreview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $brush.Dispose()
    $font.Dispose()
    $graphics.Dispose()
    $preview.Dispose()
    $reference.Dispose()
}
