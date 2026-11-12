# Block a push to main and a force push.
# Windows runs this file.
# macOS and Linux run the bash file.
# If this file cannot read the command, it allows the command.

$ErrorActionPreference = 'Stop'

function Write-JsonResult([string]$json) {
  $utf8 = New-Object System.Text.UTF8Encoding $false
  $bytes = $utf8.GetBytes($json + "`n")
  $out = [Console]::OpenStandardOutput()
  $out.Write($bytes, 0, $bytes.Length)
  $out.Flush()
  exit 0
}

function Write-AllowResult {
  Write-JsonResult '{"permission":"allow"}'
}

function Write-DenyResult {
  Write-JsonResult '{"permission":"deny","user_message":"Blocked by team policy: do not push to main and do not force push. Push a feature branch and open a pull request.","agent_message":"This command was blocked by a project hook. Do not push to main or force push. Push the feature branch and open a pull request instead."}'
}

try {
  # On macOS and Linux the bash file decides.
  # This file decides only on Windows.
  if ($env:OS -ne 'Windows_NT') {
    Write-AllowResult
  }

  $raw = ''
  foreach ($item in $input) {
    if ($raw.Length -gt 0) {
      $raw += "`n"
    }
    $raw += [string]$item
  }

  if ([string]::IsNullOrWhiteSpace($raw)) {
    $stream = [Console]::OpenStandardInput()
    $memory = New-Object System.IO.MemoryStream
    $buffer = New-Object byte[] 4096
    while (($count = $stream.Read($buffer, 0, $buffer.Length)) -gt 0) {
      $memory.Write($buffer, 0, $count)
    }
    $bytes = $memory.ToArray()
    $start = 0
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
      $start = 3
    }
    if ($bytes.Length -gt $start) {
      $utf8 = New-Object System.Text.UTF8Encoding $false
      $raw = $utf8.GetString($bytes, $start, $bytes.Length - $start)
    }
  }

  if ([string]::IsNullOrWhiteSpace($raw)) {
    Write-AllowResult
  }

  $raw = $raw.TrimStart([char]0xFEFF).Trim()
  $payload = $raw | ConvertFrom-Json
  $cmd = ''
  if ($null -ne $payload.PSObject.Properties['command'] -and $null -ne $payload.command) {
    $cmd = [string]$payload.command
  }

  $cwd = '.'
  if ($null -ne $payload.PSObject.Properties['cwd'] -and -not [string]::IsNullOrWhiteSpace([string]$payload.cwd)) {
    $cwd = [string]$payload.cwd
  }

  $isPush = [regex]::IsMatch($cmd, '(^|[;&|\s])git\s+push(\s|$)')
  if (-not $isPush) {
    Write-AllowResult
  }

  # A force push is blocked on every branch.
  if ([regex]::IsMatch($cmd, '--force|(\s)-f(\s|$)')) {
    Write-DenyResult
  }

  # A destination named main is blocked. Example: "origin main" or "HEAD:main".
  if ([regex]::IsMatch($cmd, '([\s:/+])main(\s|$)')) {
    Write-DenyResult
  }

  # "git push" with no refspec uses the current branch.
  # "git push -u origin feature/x" has a remote and a refspec, so it is allowed.
  $hasRefspec = [regex]::IsMatch($cmd, 'git\s+push(\s+(-[^\s]+|--[A-Za-z0-9-]+))*\s+[^\s-][^\s]+\s+[^\s-]')
  if (-not $hasRefspec) {
    $branch = ''
    try {
      $branch = (& git -C $cwd --no-pager branch --show-current 2>$null | Out-String).Trim()
    } catch {
      $branch = ''
    }
    if ($branch -ceq 'main') {
      Write-DenyResult
    }
  }

  Write-AllowResult
} catch {
  Write-AllowResult
}
