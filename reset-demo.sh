#!/usr/bin/env bash
# Reset the demo to a clean main branch.
# -y skips the question. --keep-remote keeps the GitHub branches.

set -u

skip_prompt=0
keep_remote=0

while [ $# -gt 0 ]; do
  case "$1" in
    -y)
      skip_prompt=1
      ;;
    --keep-remote)
      keep_remote=1
      ;;
    *)
      printf '%s\n' "Unknown option: $1" >&2
      printf '%s\n' "Use -y to skip the question. Use --keep-remote to keep GitHub branches." >&2
      exit 1
      ;;
  esac
  shift
done

script_dir=$(dirname "$0")
script_dir=$(CDPATH= cd -- "$script_dir" && pwd)
cd "$script_dir" || {
  printf '%s\n' "Could not open the script folder." >&2
  exit 1
}

if ! command -v git >/dev/null 2>&1; then
  printf '%s\n' "Git is not on the PATH. Install Git, then run the reset again." >&2
  exit 1
fi

if ! git --no-pager rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  printf '%s\n' "This folder is not a git repo. Open the demo folder and run the reset again." >&2
  exit 1
fi

printf '%s\n' "This resets the demo in:"
pwd
printf '\n'
printf '%s\n' "It will do this work:"
printf '%s\n' "- Stop the demo app if Java listens on port 8080."
printf '%s\n' "- Throw away edits that you did not commit."
printf '%s\n' "- Delete new files that git does not track."
printf '%s\n' "- Keep ignored files, such as target/ and IDE files."
printf '%s\n' "- Switch to the main branch and match main on GitHub."
printf '%s\n' "- Delete local branches whose names start with feature/ins-142."
if [ "$keep_remote" -eq 1 ]; then
  printf '%s\n' "- Keep branches on GitHub whose names start with feature/ins-142."
else
  printf '%s\n' "- Delete branches on GitHub whose names start with feature/ins-142."
  printf '%s\n' "  This also closes its open pull request on GitHub."
fi
printf '\n'

if [ "$skip_prompt" -ne 1 ]; then
  printf '%s' 'Type yes to reset the demo: '
  answer=""
  IFS= read -r answer || true
  answer=$(printf '%s' "$answer" | tr '[:upper:]' '[:lower:]' | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')
  if [ "$answer" != "yes" ]; then
    printf '%s\n' "Reset cancelled. Nothing was changed."
    exit 0
  fi
fi

stop_demo_app() {
  if ! command -v lsof >/dev/null 2>&1; then
    return 0
  fi

  pids=$(lsof -nP -iTCP:8080 -sTCP:LISTEN -t 2>/dev/null || true)
  seen=" "
  for pid in $pids; do
    case "$pid" in
      ''|*[!0-9]*)
        continue
        ;;
    esac
    if [ "$pid" -le 1 ]; then
      continue
    fi
    case "$seen" in
      *" $pid "*)
        continue
        ;;
    esac
    seen="$seen$pid "

    name=""
    if [ -r "/proc/$pid/comm" ]; then
      name=$(cat "/proc/$pid/comm" 2>/dev/null || true)
    else
      name=$(ps -p "$pid" -o comm= 2>/dev/null || true)
    fi
    base=${name##*/}
    base=$(printf '%s' "$base" | tr -d '[:space:]')
    if [ "$base" = "java" ] || [ "$base" = "java.exe" ]; then
      kill -9 "$pid" 2>/dev/null || true
      printf '%s\n' "Stopped the demo app on port 8080."
    fi
  done
}

run_git() {
  message=$1
  shift
  git --no-pager "$@"
  code=$?
  if [ "$code" -ne 0 ]; then
    printf '%s\n' "$message" >&2
    exit 1
  fi
}

stop_demo_app

printf '%s\n' "Fetching from GitHub."
run_git "Git could not fetch from origin. Check your network and sign-in, then run the reset again." fetch origin --prune

printf '%s\n' "Switching to the main branch."
run_git "Git could not switch to main. The demo was not reset." checkout -f main

printf '%s\n' "Matching main on GitHub."
run_git "Git could not make main match origin/main. The demo was not reset." reset --hard origin/main

current=$(git --no-pager rev-parse --abbrev-ref HEAD)
code=$?
if [ "$code" -ne 0 ] || [ "$current" != "main" ]; then
  printf '%s\n' "Git is not on the main branch. The demo was not fully reset." >&2
  exit 1
fi

printf '%s\n' "Removing new files that git does not track."
run_git "Git could not remove untracked files. The demo was not reset." clean -fd

printf '%s\n' "Deleting local branches that start with feature/ins-142."
branch_list=$(git --no-pager for-each-ref --format='%(refname:short)' 'refs/heads/feature/ins-142*')
code=$?
if [ "$code" -ne 0 ]; then
  printf '%s\n' "Git could not list local branches. Stop here and look at the git message above." >&2
  exit 1
fi

saw_local=0
old_ifs=$IFS
IFS=$'\n'
for branch in $branch_list; do
  if [ -z "$branch" ]; then
    continue
  fi
  saw_local=1
  delete_out=$(git --no-pager branch -D "$branch" 2>&1)
  code=$?
  if [ "$code" -ne 0 ]; then
    still=$(git --no-pager branch --list "$branch" 2>&1)
    still_code=$?
    if [ "$still_code" -ne 0 ] || [ -n "$still" ]; then
      printf '%s\n' "$delete_out" >&2
      printf '%s\n' "Git could not delete local branch $branch." >&2
      exit 1
    fi
    printf '%s\n' "Local branch $branch is already gone."
  else
    printf '%s\n' "Deleted local branch $branch."
  fi
done
IFS=$old_ifs

if [ "$saw_local" -eq 0 ]; then
  printf '%s\n' "No local branches start with feature/ins-142."
fi

if [ "$keep_remote" -eq 1 ]; then
  printf '%s\n' "Keeping branches on GitHub."
else
  printf '%s\n' "Deleting branches on GitHub that start with feature/ins-142."
  remote_list=$(git --no-pager ls-remote --heads origin 'refs/heads/feature/ins-142*')
  code=$?
  if [ "$code" -ne 0 ]; then
    printf '%s\n' "Git could not list branches on GitHub. Your files are already back on main. Remote branches were not deleted." >&2
    exit 1
  fi

  saw_remote=0
  old_ifs=$IFS
  IFS=$'\n'
  for line in $remote_list; do
    if [ -z "$line" ]; then
      continue
    fi
    case "$line" in
      *refs/heads/*) ;;
      *) continue ;;
    esac
    branch=${line#*refs/heads/}
    case "$branch" in
      feature/ins-142*) ;;
      *) continue ;;
    esac
    saw_remote=1
    printf '%s\n' "Deleting remote branch $branch."
    printf '%s\n' "This also closes its open pull request on GitHub."
    delete_out=$(git --no-pager push origin --delete "$branch" 2>&1)
    code=$?
    if [ "$code" -ne 0 ]; then
      still=$(git --no-pager ls-remote --heads origin "refs/heads/$branch" 2>&1)
      still_code=$?
      if [ "$still_code" -ne 0 ] || [ -n "$still" ]; then
        printf '%s\n' "$delete_out" >&2
        printf '%s\n' "Git could not delete remote branch $branch. Your files are already back on main. That remote branch may still be open." >&2
        exit 1
      fi
      printf '%s\n' "Remote branch $branch is already gone."
    fi
  done
  IFS=$old_ifs

  if [ "$saw_remote" -eq 0 ]; then
    printf '%s\n' "No branches on GitHub start with feature/ins-142."
  fi
fi

printf '\033[32m%s\033[0m\n' "Demo reset. You are on main with the original app. Start the app with ./mvnw spring-boot:run, then open a New Chat."
exit 0
