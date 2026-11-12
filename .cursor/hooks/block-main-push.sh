#!/bin/bash
# Block a push to main and a force push.
# macOS and Linux run this file.
# Windows runs the PowerShell file.
# If this file cannot read the command, it allows the command.
# Windows Git Bash lets the PowerShell file decide, so the message is not shown twice.
case "$(uname -s 2>/dev/null || true)" in
  MINGW*|MSYS*|CYGWIN*)
    echo '{"permission":"allow"}'
    exit 0
    ;;
esac
input=$(cat)
# Drop a UTF-8 BOM if the input has one.
bom=$(printf '\357\273\277')
case "$input" in
  "$bom"*) input=${input#"$bom"} ;;
esac
field() { printf '%s' "$input" | sed -nE "s/.*\"$1\"[[:space:]]*:[[:space:]]*\"(([^\"\\\\]|\\\\.)*)\".*/\\1/p"; }
cmd=$(field command)
cwd=$(field cwd)
deny() {
  echo '{"permission":"deny","user_message":"Blocked by team policy: do not push to main and do not force push. Push a feature branch and open a pull request.","agent_message":"This command was blocked by a project hook. Do not push to main or force push. Push the feature branch and open a pull request instead."}'
  exit 0
}
if printf '%s' "$cmd" | grep -Eq '(^|[;&|[:space:]])git[[:space:]]+push([[:space:]]|$)'; then
  if printf '%s' "$cmd" | grep -Eq -- '--force|([[:space:]])-f([[:space:]]|$)'; then
    deny
  fi
  # A token or refspec destination named main, such as "origin main" or "HEAD:main".
  if printf '%s' "$cmd" | grep -Eq '([[:space:]:/+])main([[:space:]]|$)'; then
    deny
  fi
  # "git push" or "git push origin" uses the current branch. Deny that on main.
  # A command with a remote and a refspec, such as "git push -u origin feature/x", is allowed.
  if ! printf '%s' "$cmd" | grep -Eq 'git[[:space:]]+push([[:space:]]+(-[^[:space:]]+|--[A-Za-z0-9-]+))*[[:space:]]+[^[:space:]-][^[:space:]]+[[:space:]]+[^[:space:]-]'; then
    branch=$(git -C "${cwd:-.}" branch --show-current 2>/dev/null || true)
    if [ "$branch" = "main" ]; then
      deny
    fi
  fi
fi
echo '{"permission":"allow"}'
exit 0
