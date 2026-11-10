# Facility Inspection Tracker

This app is a demo. It uses sample data only. It does not use a real agency, a real person, or a real facility.

Staff can view facilities and inspections. They can open an inspection and edit the findings and the safety checks.

## What you need

Java 21.

## Run the app

1. Open a terminal in this folder.
2. Run `./mvnw spring-boot:run`
3. Open http://localhost:8080

## Run the tests

Run `./mvnw test`

## Format the Java code

Run `./mvnw spring-javaformat:apply`

## Demo files

- `AGENTS.md`
- `.cursor/rules/project.mdc`
- `.cursor/agents/verifier.md`
- `.cursor/hooks.json`
- `.cursor/hooks/block-main-push.sh`
- `docs/tickets/INS-142.md`

## Hook self-test

Pipe a sample command to the hook. The script prints JSON.

This push to main must be denied:

```
echo '{"command":"git push origin HEAD:main","cwd":".","sandbox":false}' | .cursor/hooks/block-main-push.sh
```

This feature-branch push must be allowed:

```
echo '{"command":"git push -u origin feature/x","cwd":".","sandbox":false}' | .cursor/hooks/block-main-push.sh
```

A deny result contains `"permission":"deny"`. An allow result is `{"permission":"allow"}`.
