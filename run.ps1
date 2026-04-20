$ErrorActionPreference = "Stop"

$src = "src/main/java"
$out = "out"

if (Test-Path $out) {
    Remove-Item -Recurse -Force $out
}
New-Item -ItemType Directory -Path $out | Out-Null

$files = Get-ChildItem -Recurse -Path $src -Filter *.java | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d $out $files

java -cp $out com.leadelmarche.app.LeadelMarcheApp

