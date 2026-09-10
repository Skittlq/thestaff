[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidatePattern('^[a-z][a-z0-9_]{1,63}$')]
    [string] $ModId,

    [Parameter(Mandatory)]
    [ValidatePattern('^[^"\\=\r\n]+$')]
    [string] $ModName,

    [Parameter(Mandatory)]
    [ValidatePattern('^[a-z_][a-z0-9_]*(\.[a-z_][a-z0-9_]*)+$')]
    [string] $Package,

    [Parameter(Mandatory)]
    [ValidatePattern('^[A-Za-z_$][A-Za-z0-9_$]*$')]
    [string] $ClassName,

    [Parameter(Mandatory)]
    [ValidatePattern('^[^"\\\r\n]+$')]
    [string] $Author,

    [ValidatePattern('^[^"\\\r\n]+$')]
    [string] $Description = 'A multi-loader Minecraft mod.',

    [ValidatePattern('^[0-9A-Za-z][0-9A-Za-z.+_-]*$')]
    [string] $Version = '1.0.0'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$oldPackagePath = 'com\example\examplemod'
$sentinel = Join-Path $projectRoot "common\src\main\java\$oldPackagePath"

if (-not (Test-Path -LiteralPath $sentinel)) {
    throw 'This template has already been configured, or its placeholder package was moved.'
}

$replacements = @(
    [pscustomobject]@{ Old = 'com.example.examplemod'; New = $Package }
    [pscustomobject]@{ Old = 'Example Mod'; New = $ModName }
    [pscustomobject]@{ Old = 'ExampleMod'; New = $ClassName }
    [pscustomobject]@{ Old = 'examplemod'; New = $ModId }
    [pscustomobject]@{ Old = 'YOUR_NAME'; New = $Author }
    [pscustomobject]@{ Old = 'A multi-loader Minecraft mod.'; New = $Description }
    [pscustomobject]@{ Old = 'mod_version=1.0.0'; New = "mod_version=$Version" }
    [pscustomobject]@{ Old = 'Copyright (c) YEAR'; New = "Copyright (c) $((Get-Date).Year)" }
)

$textExtensions = @(
    '.gradle', '.properties', '.json', '.toml', '.java', '.md', '.yml',
    '.yaml', '.code-workspace', '.gitignore', '.gitattributes', '.editorconfig'
)
$scriptPath = $PSCommandPath
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)

Get-ChildItem -LiteralPath $projectRoot -Recurse -File |
    Where-Object {
        $_.FullName -ne $scriptPath -and
        $_.FullName -notmatch '[\\/](build|\.gradle)[\\/]' -and
        ($textExtensions -contains $_.Extension -or $_.Name -eq 'LICENSE')
    } |
    ForEach-Object {
        $content = [System.IO.File]::ReadAllText($_.FullName)
        $updated = $content
        foreach ($entry in $replacements) {
            $updated = $updated.Replace($entry.Old, $entry.New)
        }
        if ($updated -ne $content) {
            [System.IO.File]::WriteAllText($_.FullName, $updated, $utf8NoBom)
        }
    }

$sourceRoots = @(
    'common\src\main\java',
    'common\src\client\java',
    'common\src\test\java',
    'fabric\src\main\java',
    'fabric\src\client\java',
    'neoforge\src\main\java'
)
$newPackagePath = $Package.Replace('.', [System.IO.Path]::DirectorySeparatorChar)

foreach ($relativeSourceRoot in $sourceRoots) {
    $sourceRoot = Join-Path $projectRoot $relativeSourceRoot
    $oldDirectory = Join-Path $sourceRoot $oldPackagePath
    if (-not (Test-Path -LiteralPath $oldDirectory)) {
        continue
    }

    $newDirectory = Join-Path $sourceRoot $newPackagePath
    $newParent = Split-Path -Parent $newDirectory
    New-Item -ItemType Directory -Path $newParent -Force | Out-Null
    Move-Item -LiteralPath $oldDirectory -Destination $newDirectory
}

$resourceRoots = @(
    'common\src\main\resources',
    'common\src\client\resources',
    'fabric\src\main\resources',
    'fabric\src\client\resources',
    'fabric\src\generated\resources',
    'neoforge\src\main\resources',
    'neoforge\src\client\resources',
    'neoforge\src\generated\resources'
)

foreach ($relativeResourceRoot in $resourceRoots) {
    foreach ($namespaceType in @('assets', 'data')) {
        $oldNamespace = Join-Path $projectRoot "$relativeResourceRoot\$namespaceType\examplemod"
        if (Test-Path -LiteralPath $oldNamespace) {
            Rename-Item -LiteralPath $oldNamespace -NewName $ModId
        }
    }
}

Get-ChildItem -LiteralPath $projectRoot -Recurse -File -Filter '*ExampleMod*' |
    ForEach-Object {
        Rename-Item -LiteralPath $_.FullName -NewName ($_.Name.Replace('ExampleMod', $ClassName))
    }

$workspace = Join-Path $projectRoot 'template.code-workspace'
if (Test-Path -LiteralPath $workspace) {
    Rename-Item -LiteralPath $workspace -NewName "$ModId.code-workspace"
}

Write-Host "Configured $ModName ($ModId) using package $Package."
Write-Host 'Reload both Gradle projects in your IDE before starting development.'
