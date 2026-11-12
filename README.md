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

## Demo files

- `AGENTS.md`
- `.cursor/rules/project.mdc`
- `.cursor/agents/verifier.md`
- `.cursor/hooks.json`
- `.cursor/hooks/block-main-push.sh`
- `.cursor/hooks/block-main-push.ps1`
- `docs/tickets/INS-142.md`

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
