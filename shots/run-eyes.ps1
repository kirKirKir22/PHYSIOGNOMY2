$ErrorActionPreference = 'Continue'
$adb = 'C:\Users\Kirll\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$proj = 'C:\Users\Kirll\AndroidStudioProjects\physiognomy2'
$shots = Join-Path $proj 'shots'

$t = @{
    next = [string]::new([char[]]@(0x0414,0x0410,0x041B,0x0415,0x0415))
    result = [string]::new([char[]]@(0x0418,0x0422,0x041E,0x0413))
    checklist = [string]::new([char[]]@(0x0427,0x0435,0x043A,0x002D,0x043B,0x0438,0x0441,0x0442))
    eyes = [string]::new([char[]]@(0x0413,0x041B,0x0410,0x0417,0x0410))
    almond = [string]::new([char[]]@(0x041C,0x0438,0x043D,0x0434,0x0430,0x043B,0x0435,0x0432,0x0438,0x0434,0x043D,0x044B,0x0435))
}

function Shot($name) {
    & $adb shell screencap -p "/sdcard/$name.png" *> $null
    & $adb pull "/sdcard/$name.png" (Join-Path $shots "$name.png") *> $null
}
function Dump() {
    & $adb shell uiautomator dump /sdcard/ui.xml *> $null
    return ((& $adb shell cat /sdcard/ui.xml) -join '')
}
function TapText($xml, $text) {
    $pattern = '<node[^>]*text="' + [regex]::Escape($text) + '[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
    $m = [regex]::Matches($xml, $pattern)
    if ($m.Count -eq 0) { return 'missing' }
    $g = $m[0].Groups
    $x = [int](([int]$g[1].Value + [int]$g[3].Value) / 2)
    $y = [int](([int]$g[2].Value + [int]$g[4].Value) / 2)
    & $adb shell input tap $x $y *> $null
    return 'tapped'
}

for ($i = 0; $i -lt 2; $i++) { TapText (Dump) $t.next | Out-Null; Start-Sleep -Milliseconds 700 }
Shot 'e1_eyes_art'

TapText (Dump) $t.almond | Out-Null
Start-Sleep -Milliseconds 900
Shot 'e2_eyes_selected'

# checklist via progress
$x = Dump
$m = [regex]::Matches($x, '<node[^>]*text="[^"]*0[^"]*/[^"]*44[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
if ($m.Count -gt 0) {
    $g = $m[0].Groups
    & $adb shell input tap ([int](([int]$g[1].Value + [int]$g[3].Value) / 2)) ([int](([int]$g[2].Value + [int]$g[4].Value) / 2)) *> $null
    Start-Sleep -Seconds 1
    Shot 'e3_checklist'
}
'DONE'
