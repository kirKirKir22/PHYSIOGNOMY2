$ErrorActionPreference = 'Continue'
$adb = 'C:\Users\Kirll\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$proj = 'C:\Users\Kirll\AndroidStudioProjects\physiognomy2'
$shots = Join-Path $proj 'shots'

$t = @{
    next = [string]::new([char[]]@(0x0414,0x0410,0x041B,0x0415,0x0415))
    eye = [string]::new([char[]]@(0x041A,0x0440,0x0443,0x0433,0x043B,0x044B,0x0435))
    progress = [string]::new([char[]]@(0x041F,0x0420,0x041E,0x0413,0x0420,0x0415,0x0421,0x0421))
    settings = [string]::new([char[]]@(0x041D,0x0430,0x0441,0x0442,0x0440,0x043E,0x0439,0x043A,0x0438))
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

& $adb shell am force-stop com.kirill.physiognomy2
& $adb shell am start -n com.kirill.physiognomy2/.MainActivity *> $null
Start-Sleep -Seconds 4

Write-Output ('progress_tap ' + (TapText (Dump) $t.progress))
Start-Sleep -Seconds 2
Shot 'e1_checklist'

'DONE'
