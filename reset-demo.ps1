# Reset the demo to a clean main branch.
# This file runs on Windows PowerShell 5.1.
# -Yes skips the question. -KeepRemote keeps the GitHub branches.

param(
  [switch]$Yes,
  [switch]$KeepRemote
)

# Do not set ErrorActionPreference to Stop.
# On Windows PowerShell 5.1, Stop makes git error text end the script.
$ErrorActionPreference = 'Continue'
try {
  $nativePref = Get-Variable -Name 'PSNativeCommandUseErrorActionPreference' -ErrorAction SilentlyContinue
  if ($null -ne $nativePref) {
    $PSNativeCommandUseErrorActionPreference = $false
  }
} catch {
  # Windows PowerShell 5.1 does not have this setting.
}

$script:GitExit = 0
$script:GitLines = @()

if ($args.Count -gt 0) {
  Write-Host "Unknown option: $($args -join ' ')." -ForegroundColor Red
  Write-Host "Use -Yes to skip the question. Use -KeepRemote to keep GitHub branches."
  exit 1
}

function Write-GitOutput {
  param(
    $Raw,
    [switch]$Quiet
  )

  $script:GitLines = @()
  if ($null -eq $Raw) {
    return
  }

  foreach ($line in @($Raw)) {
    if ($null -eq $line) {
      continue
    }
    $text = [string]$line
    if ($line -is [System.Management.Automation.ErrorRecord]) {
      if ($null -ne $line.Exception -and -not [string]::IsNullOrWhiteSpace([string]$line.Exception.Message)) {
        $text = [string]$line.Exception.Message
      }
    }
    $script:GitLines += $text
    if (-not $Quiet) {
      Write-Host $text
    }
  }
}

function Invoke-Git {
  param(
    [Parameter(Mandatory = $true)]
    [string[]]$GitArguments,
    [switch]$Quiet
  )

  $raw = & git --no-pager @GitArguments 2>&1
  $script:GitExit = $LASTEXITCODE
  Write-GitOutput -Raw $raw -Quiet:$Quiet
}

function Invoke-GitOrStop {
  param(
    [Parameter(Mandatory = $true)]
    [string]$FailureMessage,
    [Parameter(Mandatory = $true)]
    [string[]]$GitArguments
  )

  Invoke-Git -GitArguments $GitArguments
  if ($script:GitExit -ne 0) {
    Write-Host $FailureMessage -ForegroundColor Red
    exit 1
  }
}

function Test-GitLinesPresent {
  foreach ($checkLine in @($script:GitLines)) {
    if (-not [string]::IsNullOrWhiteSpace($checkLine)) {
      return $true
    }
  }
  return $false
}

function Stop-DemoOnPort {
  if (-not (Get-Command Get-NetTCPConnection -ErrorAction SilentlyContinue)) {
    return
  }

  $connections = @()
  try {
    $found = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue
    if ($null -ne $found) {
      $connections = @($found)
    }
  } catch {
    return
  }

  $seen = @{}
  foreach ($conn in $connections) {
    if ($null -eq $conn) {
      continue
    }

    $procId = 0
    $gotPid = $false
    try {
      $procId = [int]$conn.OwningProcess
      $gotPid = $true
    } catch {
      $gotPid = $false
    }
    if (-not $gotPid) {
      continue
    }
    if ($procId -le 0) {
      continue
    }

    $key = "$procId"
    if ($seen.ContainsKey($key)) {
      continue
    }
    $seen[$key] = $true

    $proc = Get-Process -Id $procId -ErrorAction SilentlyContinue
    if ($null -eq $proc) {
      continue
    }
    if ($proc.ProcessName -ne 'java') {
      continue
    }

    Stop-Process -Id $procId -Force -ErrorAction SilentlyContinue
    $still = Get-Process -Id $procId -ErrorAction SilentlyContinue
    if ($null -eq $still) {
      Write-Host "Stopped the demo app on port 8080."
    }
  }
}

Set-Location -LiteralPath $PSScriptRoot

if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
  Write-Host "Git is not on the PATH. Install Git for Windows, then run the reset again." -ForegroundColor Red
  exit 1
}

$repoCheck = & git --no-pager rev-parse --is-inside-work-tree 2>&1
$repoCode = $LASTEXITCODE
if ($repoCode -ne 0) {
  Write-Host "This folder is not a git repo. Open the demo folder and run the reset again." -ForegroundColor Red
  exit 1
}

Write-Host "This resets the demo in:"
Write-Host (Get-Location).Path
Write-Host ""
Write-Host "It will do this work:"
Write-Host "- Stop the demo app if Java listens on port 8080."
Write-Host "- Throw away edits that you did not commit."
Write-Host "- Delete new files that git does not track."
Write-Host "- Keep ignored files, such as target/ and IDE files."
Write-Host "- Switch to the main branch and match main on GitHub."
Write-Host "- Delete local branches whose names start with feature/ins-142."
if ($KeepRemote) {
  Write-Host "- Keep branches on GitHub whose names start with feature/ins-142."
} else {
  Write-Host "- Delete branches on GitHub whose names start with feature/ins-142."
  Write-Host "  This also closes its open pull request on GitHub."
}
Write-Host ""

if (-not $Yes) {
  $answer = Read-Host "Type yes to reset the demo"
  if ($null -eq $answer -or $answer.Trim() -ne 'yes') {
    Write-Host "Reset cancelled. Nothing was changed."
    exit 0
  }
}

Stop-DemoOnPort

Write-Host "Fetching from GitHub."
Invoke-GitOrStop -FailureMessage "Git could not fetch from origin. Check your network and sign-in, then run the reset again." -GitArguments @('fetch', 'origin', '--prune')

Write-Host "Switching to the main branch."
Invoke-GitOrStop -FailureMessage "Git could not switch to main. The demo was not reset." -GitArguments @('checkout', '-f', 'main')

Write-Host "Matching main on GitHub."
Invoke-GitOrStop -FailureMessage "Git could not make main match origin/main. The demo was not reset." -GitArguments @('reset', '--hard', 'origin/main')

Invoke-Git -Quiet -GitArguments @('rev-parse', '--abbrev-ref', 'HEAD')
$onMain = $false
if ($script:GitExit -eq 0) {
  foreach ($checkLine in @($script:GitLines)) {
    if ($checkLine.Trim() -eq 'main') {
      $onMain = $true
    }
  }
}
if (-not $onMain) {
  Write-Host "Git is not on the main branch. The demo was not fully reset." -ForegroundColor Red
  exit 1
}

Write-Host "Removing new files that git does not track."
Invoke-GitOrStop -FailureMessage "Git could not remove untracked files. The demo was not reset." -GitArguments @('clean', '-fd')

Write-Host "Deleting local branches that start with feature/ins-142."
Invoke-Git -Quiet -GitArguments @('for-each-ref', '--format=%(refname:short)', 'refs/heads/feature/ins-142*')
if ($script:GitExit -ne 0) {
  foreach ($checkLine in @($script:GitLines)) {
    if (-not [string]::IsNullOrWhiteSpace($checkLine)) {
      Write-Host $checkLine
    }
  }
  Write-Host "Git could not list local branches. Stop here and look at the git message above." -ForegroundColor Red
  exit 1
}

$localBranches = @($script:GitLines)
$sawLocal = $false
foreach ($branch in $localBranches) {
  if ([string]::IsNullOrWhiteSpace($branch)) {
    continue
  }
  $sawLocal = $true
  Invoke-Git -Quiet -GitArguments @('branch', '-D', $branch)
  if ($script:GitExit -ne 0) {
    $deleteLines = @($script:GitLines)
    Invoke-Git -Quiet -GitArguments @('branch', '--list', $branch)
    $stillThere = $false
    if ($script:GitExit -ne 0) {
      $stillThere = $true
    } elseif (Test-GitLinesPresent) {
      $stillThere = $true
    }
    if ($stillThere) {
      foreach ($deleteLine in $deleteLines) {
        if (-not [string]::IsNullOrWhiteSpace($deleteLine)) {
          Write-Host $deleteLine
        }
      }
      Write-Host "Git could not delete local branch $branch." -ForegroundColor Red
      exit 1
    }
    Write-Host "Local branch $branch is already gone."
  } else {
    Write-Host "Deleted local branch $branch."
  }
}
if (-not $sawLocal) {
  Write-Host "No local branches start with feature/ins-142."
}

if ($KeepRemote) {
  Write-Host "Keeping branches on GitHub."
} else {
  Write-Host "Deleting branches on GitHub that start with feature/ins-142."
  Invoke-Git -Quiet -GitArguments @('ls-remote', '--heads', 'origin', 'refs/heads/feature/ins-142*')
  if ($script:GitExit -ne 0) {
    foreach ($checkLine in @($script:GitLines)) {
      if (-not [string]::IsNullOrWhiteSpace($checkLine)) {
        Write-Host $checkLine
      }
    }
    Write-Host "Git could not list branches on GitHub. Your files are already back on main. Remote branches were not deleted." -ForegroundColor Red
    exit 1
  }

  $remoteLines = @($script:GitLines)
  $sawRemote = $false
  foreach ($line in $remoteLines) {
    if ([string]::IsNullOrWhiteSpace($line)) {
      continue
    }
    $marker = 'refs/heads/'
    $index = $line.IndexOf($marker)
    if ($index -lt 0) {
      continue
    }
    $branch = $line.Substring($index + $marker.Length).Trim()
    if ([string]::IsNullOrWhiteSpace($branch)) {
      continue
    }
    if (-not $branch.StartsWith('feature/ins-142')) {
      continue
    }

    $sawRemote = $true
    Write-Host "Deleting remote branch $branch."
    Write-Host "This also closes its open pull request on GitHub."
    Invoke-Git -Quiet -GitArguments @('push', 'origin', '--delete', $branch)
    if ($script:GitExit -ne 0) {
      $deleteLines = @($script:GitLines)
      Invoke-Git -Quiet -GitArguments @('ls-remote', '--heads', 'origin', "refs/heads/$branch")
      $stillThere = $false
      if ($script:GitExit -ne 0) {
        $stillThere = $true
      } elseif (Test-GitLinesPresent) {
        $stillThere = $true
      }
      if ($stillThere) {
        foreach ($deleteLine in $deleteLines) {
          if (-not [string]::IsNullOrWhiteSpace($deleteLine)) {
            Write-Host $deleteLine
          }
        }
        Write-Host "Git could not delete remote branch $branch. Your files are already back on main. That remote branch may still be open." -ForegroundColor Red
        exit 1
      }
      Write-Host "Remote branch $branch is already gone."
    }
  }
  if (-not $sawRemote) {
    Write-Host "No branches on GitHub start with feature/ins-142."
  }
}

Write-Host "Demo reset. You are on main with the original app. Start the app with .\mvnw.cmd spring-boot:run, then open a New Chat." -ForegroundColor Green
exit 0
