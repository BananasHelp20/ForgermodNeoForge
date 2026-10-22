param([ValidateSet('all', 'damask', 'reinforced_iron')][string]$SelectedMaterial = 'all')
# Use only approved reference PNGs; never open the old billet textures.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path -Parent $PSScriptRoot
$palette = Get-Content (Join-Path $projectRoot 'markdown/metalPalettes.json') -Raw | ConvertFrom-Json
$jobs = @(
    @{
        Material = 'damask'; Reference = 'zinc_ingot'; Label = 'Damask Billet'; ReferenceLabel = 'Zinc chunk reference'
        ReferencePath = 'src/main/examples/create/zinc_ingot.png'
        PalettePath = 'src/main/resources/assets/forgermod/textures/item/metals/ingots/damask_ingot.png'
        OutputPath = 'src/main/resources/assets/forgermod/textures/item/metals/raw/damask_billet.png'
        Map = @{
            '#213B3F'=0; '#31544E'=1; '#506D63'=2; '#788273'=3; '#929B87'=4
            '#A5C0A0'=5; '#B9E9C1'=6; '#D6FFDC'=7; '#FFFFFF'=8
        }
    },
    @{
        Material = 'reinforced_iron'; Reference = 'zinc_ingot'; Label = 'Reinforced Iron Billet'; ReferenceLabel = 'Zinc chunk reference'
        ReferencePath = 'src/main/examples/create/zinc_ingot.png'
        OutputPath = 'src/main/resources/assets/forgermod/textures/item/metals/raw/reinforced_iron_billet.png'
        Map = @{
            '#213B3F'=0; '#31544E'=1; '#506D63'=2; '#788273'=3; '#929B87'=4
            '#A5C0A0'=5; '#B9E9C1'=6; '#D6FFDC'=7; '#FFFFFF'=8
        }
    }
)
$preview = [System.Drawing.Bitmap]::new(960, 300)
$graphics = [System.Drawing.Graphics]::FromImage($preview)
$graphics.Clear([System.Drawing.ColorTranslator]::FromHtml('#20252C'))
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
$font = [System.Drawing.Font]::new('Segoe UI', 12)
$brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
try {
    for ($jobIndex = 0; $jobIndex -lt $jobs.Count; $jobIndex++) {
        $job = $jobs[$jobIndex]; $material = $job.Material
        $source = [System.Drawing.Bitmap]::new((Join-Path $projectRoot $job.ReferencePath))
        $target = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            if ($source.Width -ne 16 -or $source.Height -ne 16) { throw 'Reference must be 16x16.' }
            if ($job.ContainsKey('PalettePath')) {
                $paletteSource = [System.Drawing.Bitmap]::new((Join-Path $projectRoot $job.PalettePath))
                try {
                    $unique = @{}
                    for ($py = 0; $py -lt $paletteSource.Height; $py++) {
                        for ($px = 0; $px -lt $paletteSource.Width; $px++) {
                            $sample = $paletteSource.GetPixel($px, $py)
                            if ($sample.A -eq 0) { continue }
                            $key = '#{0:X2}{1:X2}{2:X2}' -f $sample.R, $sample.G, $sample.B
                            $unique[$key] = [System.Drawing.Color]::FromArgb(255, $sample.R, $sample.G, $sample.B)
                        }
                    }
                    $colors = @($unique.Values | Sort-Object @{Expression={0.2126 * $_.R + 0.7152 * $_.G + 0.0722 * $_.B}}, @{Expression={$_.ToArgb()}})
                    if ($colors.Count -ne 9) { throw "Expected nine shades in the Damask ingot; found $($colors.Count). Review the mapping before regenerating." }
                } finally { $paletteSource.Dispose() }
            } else {
                $colors = @($palette.palettes.$material.colors | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
            }
            foreach ($role in $job.Map.Values) {
                if ($role -lt 0 -or $role -ge $colors.Count) { throw "Palette lacks role $role for $material." }
            }
            for ($y = 0; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    $pixel = $source.GetPixel($x, $y)
                    if ($pixel.A -eq 0) { $target.SetPixel($x, $y, $pixel); continue }
                    $hex = '#{0:X2}{1:X2}{2:X2}' -f $pixel.R, $pixel.G, $pixel.B
                    if (-not $job.Map.ContainsKey($hex)) { throw "Unexpected reference color: $hex" }
                    $mapped = $colors[$job.Map[$hex]]
                    $target.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, $mapped.R, $mapped.G, $mapped.B))
                }
            }
            if ($SelectedMaterial -eq 'all' -or $SelectedMaterial -eq $material) {
                $target.Save((Join-Path $projectRoot $job.OutputPath), [System.Drawing.Imaging.ImageFormat]::Png)
                Write-Output "Saved $material billet from $($job.Reference), preserving exact 16x16 layout and alpha."
            }
            $offset = $jobIndex * 480
            $graphics.DrawString($job.ReferenceLabel, $font, $brush, $offset + 12, 12)
            $graphics.DrawString($job.Label, $font, $brush, $offset + 252, 12)
            $graphics.DrawImage($source, [System.Drawing.Rectangle]::new($offset + 24, 48, 192, 192))
            $graphics.DrawImage($target, [System.Drawing.Rectangle]::new($offset + 264, 48, 192, 192))
            $graphics.DrawImageUnscaled($source, $offset + 24, 264)
            $graphics.DrawImageUnscaled($target, $offset + 264, 264)
        } finally { $target.Dispose(); $source.Dispose() }
    }
    $preview.Save((Join-Path $projectRoot 'markdown/billetPreview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $brush.Dispose(); $font.Dispose(); $graphics.Dispose(); $preview.Dispose()
}
