# Project rules

## Plan

- Explore first. Find every file that reads or writes the area.
- Include schema, data, templates, messages, and tests.
- If the ticket is unclear, stop and ask before you plan.
- Do not edit files while you plan.

## Build

- Create a branch named `feature/<ticket-id-lowercase>-<short-name>`.
- Example: `feature/ins-142-inspection-status`.
- On macOS or Linux, after each step, run `./mvnw spring-javaformat:apply`, then `./mvnw test`.
- On Windows, after each step, run `.\mvnw.cmd spring-javaformat:apply`, then `.\mvnw.cmd test`.
- On macOS or Linux, start the app with `./mvnw spring-boot:run`.
- On Windows, start the app with `.\mvnw.cmd spring-boot:run`.
- Fix failures.
- If the same test fails twice for the same reason, stop and explain it in plain words.

## Git

- Commit messages use `<TICKET>: <summary>`.
- A pull request description comes from the ticket and lists the tests added.
- Open pull requests with `gh pr create` against `main`.
- Never push to main.
