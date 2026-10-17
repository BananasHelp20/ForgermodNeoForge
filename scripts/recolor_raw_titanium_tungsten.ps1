# Preserve the approved unrefined steel sprites; never read existing target textures.
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$projectRoot = Split-Path -Parent $PSScriptRoot
$paletteData = Get-Content (Join-Path $projectRoot 'markdown/metalPalettes.json') -Raw | ConvertFrom-Json
$jobs = @(
    @{
        Material = 'titanium'; Reference = 'unrefined_steel'
        Map = @{
            '#13171B'=0; '#191F24'=1; '#1B2126'=1; '#20252B'=1; '#212224'=1
            '#232A30'=2; '#272E36'=2; '#282A2B'=2; '#2D2F30'=2
            '#3C3F42'=3; '#43464A'=3; '#474B4F'=3; '#4E5257'=3
            '#545759'=3; '#5B5E61'=3; '#606366'=3; '#909599'=4; '#A8ADB3'=5
        }
    },
    @{
        Material = 'tungsten'; Reference = 'unrefined_carbon_steel'
        Map = @{
            '#171717'=0; '#1F1F1F'=1; '#292929'=2; '#323232'=3; '#383838'=3
            '#3A3A3A'=3; '#4A4A4A'=4; '#525252'=4; '#616161'=4; '#737373'=5
        }
    }
)
$preview = [System.Drawing.Bitmap]::new(896, 300)
$graphics = [System.Drawing.Graphics]::FromImage($preview)
$graphics.Clear([System.Drawing.ColorTranslator]::FromHtml('#20252C'))
$graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::NearestNeighbor
$graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::Half
$font = [System.Drawing.Font]::new('Segoe UI', 12)
$brush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
try {
    for ($jobIndex = 0; $jobIndex -lt $jobs.Count; $jobIndex++) {
        $job = $jobs[$jobIndex]
        $material = $job.Material
        $source = [System.Drawing.Bitmap]::new((Join-Path $projectRoot "src/main/resources/assets/forgermod/textures/item/metals/raw/$($job.Reference).png"))
        $target = [System.Drawing.Bitmap]::new(16, 16, [System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
        try {
            if ($source.Width -ne 16 -or $source.Height -ne 16) { throw 'Reference must be 16x16.' }
            $colors = @($paletteData.palettes.$material.colors | ForEach-Object { [System.Drawing.ColorTranslator]::FromHtml($_) })
            if ($colors.Count -ne 6) { throw "Expected six palette colors for $material." }
            for ($y = 0; $y -lt 16; $y++) {
                for ($x = 0; $x -lt 16; $x++) {
                    $pixel = $source.GetPixel($x, $y)
                    if ($pixel.A -eq 0) { $target.SetPixel($x, $y, $pixel); continue }
                    $hex = '#{0:X2}{1:X2}{2:X2}' -f $pixel.R, $pixel.G, $pixel.B
                    if (-not $job.Map.ContainsKey($hex)) { throw "Unexpected reference color $hex." }
                    $mapped = $colors[$job.Map[$hex]]
                    $target.SetPixel($x, $y, [System.Drawing.Color]::FromArgb($pixel.A, $mapped.R, $mapped.G, $mapped.B))
                }
            }
            $target.Save((Join-Path $projectRoot "src/main/resources/assets/forgermod/textures/item/metals/raw/raw_$material.png"), [System.Drawing.Imaging.ImageFormat]::Png)
            $offset = $jobIndex * 448
            $graphics.DrawString($job.Reference, $font, $brush, $offset + 12, 12)
            $graphics.DrawString(('Raw ' + (Get-Culture).TextInfo.ToTitleCase($material)), $font, $brush, $offset + 236, 12)
            $graphics.DrawImage($source, [System.Drawing.Rectangle]::new($offset + 16, 48, 192, 192))
            $graphics.DrawImage($target, [System.Drawing.Rectangle]::new($offset + 240, 48, 192, 192))
            $graphics.DrawImageUnscaled($source, $offset + 16, 264)
            $graphics.DrawImageUnscaled($target, $offset + 240, 264)
            Write-Output "Saved raw $material from $($job.Reference), preserving exact 16x16 silhouette and alpha."
        } finally { $target.Dispose(); $source.Dispose() }
    }
    $preview.Save((Join-Path $projectRoot 'markdown/rawTitaniumTungstenPreview.png'), [System.Drawing.Imaging.ImageFormat]::Png)
} finally {
    $brush.Dispose(); $font.Dispose(); $graphics.Dispose(); $preview.Dispose()
}
