#!/bin/bash
# Blocks a git push to main and any force push.
# Bash, sed -E, and grep -E only, so it runs on macOS and Linux.
# Cursor sends JSON on stdin, for example {"command":"git push origin main","cwd":"...","sandbox":false}
input=$(cat)
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
