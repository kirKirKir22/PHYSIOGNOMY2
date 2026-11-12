$ErrorActionPreference = 'Continue'
$adb = 'C:\Users\Kirll\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$proj = 'C:\Users\Kirll\AndroidStudioProjects\physiognomy2'
$shots = Join-Path $proj 'shots'

$t = @{
    eyes = [string]::new([char[]]@(0x041A,0x0440,0x0443,0x0433,0x043B,0x044B,0x0435))
    next = [string]::new([char[]]@(0x0414,0x0410,0x041B,0x0415,0x0415))
    profiles = [string]::new([char[]]@(0x041F,0x0440,0x043E,0x0444,0x0438,0x043B,0x0438))
    settings = [string]::new([char[]]@(0x041D,0x0430,0x0441,0x0442,0x0440,0x043E,0x0439,0x043A,0x0438))
    checklist = [string]::new([char[]]@(0x0427,0x0435,0x043A,0x002D,0x043B,0x0438,0x0441,0x0442))
    save = [string]::new([char[]]@(0x0421,0x043E,0x0445,0x0440,0x0430,0x043D,0x0438,0x0442,0x044C))
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

TapText (Dump) $t.eyes | Out-Null
Start-Sleep -Milliseconds 800
Shot 'z1_eyes_selected'

TapText (Dump) $t.next | Out-Null
Start-Sleep -Milliseconds 800
TapText (Dump) $t.next | Out-Null
Start-Sleep -Milliseconds 800
Shot 'z2_step3_q2'

TapText (Dump) $t.checklist | Out-Null
Start-Sleep -Milliseconds 900
TapText (Dump) $t.profiles | Out-Null
Start-Sleep -Milliseconds 900
TapText (Dump) $t.save | Out-Null
Start-Sleep -Milliseconds 900
Shot 'z3_profiles'

TapText (Dump) $t.settings | Out-Null
Start-Sleep -Milliseconds 900
Shot 'z4_settings_light'

# switch to dark theme via the app's own control
$xml = Dump
$m = [regex]::Matches($xml, '<node[^>]*text="' + [regex]::Escape([string]::new([char[]]@(0x0422,0x0451,0x043C,0x043D,0x0430,0x044F))) + '[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
if ($m.Count -gt 0) {
    $g = $m[0].Groups
    $x = [int](([int]$g[1].Value + [int]$g[3].Value) / 2)
    $y = [int](([int]$g[2].Value + [int]$g[4].Value) / 2)
    & $adb shell input tap $x $y *> $null
    Start-Sleep -Milliseconds 900
}
Shot 'z5_settings_dark'

TapText (Dump) $t.checklist | Out-Null
Start-Sleep -Milliseconds 900
Shot 'z6_workspace_dark'

'DONE'
