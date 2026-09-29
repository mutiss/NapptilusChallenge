# AI Usage

This document discloses which AI assistants were used while building this project, and for what purpose.

## Claude (via Claude Code)
- Writing unit and instrumented tests: hand-written fakes, tests for the cache interceptors, Room DAO and Compose UI tests
- Generating test tags for UI tests and accessibility
- Generating some composables and previews, such as the empty/error states
- Generating the `README.md` and `AI_USAGE.md` files
- Auditing the codebase and docs before submission, suggesting and applying improvements

## ChatGPT
- Asking about and checking the HTTP caching strategy (OkHttp cache and interceptors)
- Architecture discussions
- Generating an SVG asset for the NapptilusRickMaybe icon

## Review

Everything produced with the help of these tools was reviewed before being included in the project. Nothing was accepted blindly: every suggestion, test or fix was read and understood before committing it.

## Closing note

AI sped up the parts of development that benefit from a second pair of hands, such as tests, boilerplate and brainstorming. The judgment calls stayed with the developer.
