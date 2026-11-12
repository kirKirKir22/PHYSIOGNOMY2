$ErrorActionPreference = 'Continue'
$adb = 'C:\Users\Kirll\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$proj = 'C:\Users\Kirll\AndroidStudioProjects\physiognomy2'
$shots = Join-Path $proj 'shots'

$t = @{
    next = [string]::new([char[]]@(0x0414,0x0410,0x041B,0x0415,0x0415))
    checklist = [string]::new([char[]]@(0x0427,0x0435,0x043A,0x002D,0x043B,0x0438,0x0441,0x0442))
    all = [string]::new([char[]]@(0x041F,0x043E,0x043A,0x0430,0x0437,0x0430,0x0442,0x044C,0x0020,0x0432,0x0441,0x0435))
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

& $adb shell pm clear com.kirill.physiognomy2 *> $null
& $adb shell am start -n com.kirill.physiognomy2/.MainActivity *> $null
Start-Sleep -Seconds 4

TapText (Dump) $t.next | Out-Null
Start-Sleep -Milliseconds 800
Shot 'f1_brows'

# go back to step 1 and select forehead to have interpretation
TapText (Dump) $t.checklist | Out-Null
Start-Sleep -Seconds 1
Shot 'f2_checklist'

'DONE'
