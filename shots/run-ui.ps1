$ErrorActionPreference = 'Continue'
$adb = 'C:\Users\Kirll\AppData\Local\Android\Sdk\platform-tools\adb.exe'
$proj = 'C:\Users\Kirll\AndroidStudioProjects\physiognomy2'
$shots = Join-Path $proj 'shots'

$texts = @{
    next = [string]::new([char[]]@(0x0414,0x0410,0x041B,0x0415,0x0415))
    forehead = [string]::new([char[]]@(0x041A,0x0440,0x0443,0x0442,0x043E,0x0439))
}

function Shot($name) {
    & $adb shell screencap -p "/sdcard/$name.png" *> $null
    & $adb pull "/sdcard/$name.png" (Join-Path $shots "$name.png") *> $null
}
function Dump() {
    & $adb shell uiautomator dump /sdcard/ui.xml *> $null
    return ((& $adb shell cat /sdcard/ui.xml) -join '')
}
function FindCenter($xml, $text) {
    $pattern = '<node[^>]*text="' + [regex]::Escape($text) + '[^"]*"[^>]*bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"'
    $m = [regex]::Matches($xml, $pattern)
    if ($m.Count -eq 0) { return $null }
    $g = $m[0].Groups
    $x = ([int]$g[1].Value + [int]$g[3].Value) / 2
    $y = ([int]$g[2].Value + [int]$g[4].Value) / 2
    return @([int]$x, [int]$y)
}
function TapText($xml, $text) {
    $c = FindCenter $xml $text
    if ($c -ne $null) {
        & $adb shell input tap $c[0] $c[1] *> $null
        return 'tapped'
    }
    return 'missing'
}

& $adb shell pm clear com.kirill.physiognomy2 *> $null
& $adb shell am start -n com.kirill.physiognomy2/.MainActivity *> $null
Start-Sleep -Seconds 4
Shot 'y1_start'

$x = Dump
Write-Output ('option ' + (TapText $x $texts.forehead))
Start-Sleep -Milliseconds 900
Shot 'y2_selected'

for ($i = 0; $i -lt 4; $i++) {
    $x = Dump
    $r = TapText $x $texts.next
    Write-Output ('next ' + $i + ' ' + $r)
    Start-Sleep -Milliseconds 900
}
Shot 'y3_step3_eyes'

Write-Output 'DONE'
