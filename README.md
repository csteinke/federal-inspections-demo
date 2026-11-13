# Facility Inspection Tracker

This app is a demo. It uses sample data only. It does not use a real agency, a real person, or a real facility.

Staff can view facilities and inspections. They can open an inspection and edit the findings and the safety checks.

## What you need

Java 21.

## Run the app

1. Open a terminal in this folder.
2. On macOS or Linux, run `./mvnw spring-boot:run`. On Windows, run `.\mvnw.cmd spring-boot:run`.
3. Open http://localhost:8080

## Run the tests

On macOS or Linux, run `./mvnw test`.

On Windows, run `.\mvnw.cmd test`.

## Format the Java code

On macOS or Linux, run `./mvnw spring-javaformat:apply`.

On Windows, run `.\mvnw.cmd spring-javaformat:apply`.

## Demo run of show

Use this order. Work in Cursor on Windows. Run the commands in PowerShell from this folder.

1. Explain the old check. Ask Cursor this question:

```
Explain @src/main/java/org/springframework/samples/inspection/model/ChkGate.java#sc in plain words. What would break if I change it?
```

2. Plan INS-142 in Plan mode. Ask Cursor to plan `docs/tickets/INS-142.md`. Do not edit files in this step.
3. Build the ticket on your machine. The agent works on a feature branch. Then run:

```
.\mvnw.cmd spring-javaformat:apply
.\mvnw.cmd test
```

4. Check the branch against the ticket. Ask the verifier to compare the branch with `docs/tickets/INS-142.md`.
5. A push to main is blocked. Ask the agent to push the branch to main. The project hook blocks that push.
6. Open a pull request. Ask the agent to push the feature branch and open a pull request against `main`.
7. Bugbot reviews the pull request. The review rules are in `.cursor/BUGBOT.md`.
8. Merge the pull request on GitHub.
9. Put the folder back on the start tag:

```
.\reset-demo.cmd
```

## Reset the demo

Run this before you rehearse again. It puts this folder back on the `demo-start` tag. That tag is the clean app, before a rehearsal adds the ticket work.

On Windows, run `.\reset-demo.cmd`.

On macOS or Linux, run `./reset-demo.sh`.

The command tells you what it will do. Then it asks you to type yes.

It stops the demo app if Java listens on port 8080. It deletes these things:

- Edits you did not commit.
- New files that git does not track. It keeps ignored files, such as `target/` and IDE files.
- Local branches whose names start with `feature/ins-142`.
- Those same branches on GitHub. Deleting one of those branches also closes its open pull request.

If GitHub main is not the `demo-start` tag, the reset moves main back to that tag. A merged rehearsal pull request is removed from main.

Other branches stay.

If you change the demo setup on purpose, move the tag with `git tag -f demo-start` and `git push -f origin demo-start`.

## Demo files

- `AGENTS.md`
- `.cursor/rules/project.mdc`
- `.cursor/BUGBOT.md`
- `.cursor/agents/verifier.md`
- `.cursor/hooks.json`
- `.cursor/hooks/block-main-push.sh`
- `.cursor/hooks/block-main-push.ps1`
- `docs/tickets/INS-142.md`
- `reset-demo.cmd`
- `reset-demo.ps1`
- `reset-demo.sh`

## Hook self-test

Pipe a sample command to the hook. The script prints JSON.

On macOS or Linux, this push to main must be denied:

```
echo '{"command":"git push origin HEAD:main","cwd":".","sandbox":false}' | bash .cursor/hooks/block-main-push.sh
```

On macOS or Linux, this feature-branch push must be allowed:

```
echo '{"command":"git push -u origin feature/x","cwd":".","sandbox":false}' | bash .cursor/hooks/block-main-push.sh
```

On Windows, use PowerShell:

```
'{"command":"git push origin HEAD:main","cwd":".","sandbox":false}' | powershell.exe -NoProfile -NonInteractive -ExecutionPolicy Bypass -File .cursor/hooks/block-main-push.ps1
```

```
'{"command":"git push -u origin feature/x","cwd":".","sandbox":false}' | powershell.exe -NoProfile -NonInteractive -ExecutionPolicy Bypass -File .cursor/hooks/block-main-push.ps1
```

A deny result contains `"permission":"deny"`. An allow result is `{"permission":"allow"}`.

If the hook cannot read the command, it allows the command.
