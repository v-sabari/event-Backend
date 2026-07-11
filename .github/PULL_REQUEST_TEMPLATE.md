## Description

<!-- What does this PR do? Why is it needed? -->

## Related Issue(s)

<!-- Closes #123 -->

## Type of Change

- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Database migration
- [ ] Documentation update
- [ ] Refactor / chore

## How Has This Been Tested?

<!-- Describe manual/automated testing: endpoints hit, roles used, migrations run, etc. -->

## Checklist

- [ ] `./mvnw clean compile` succeeds
- [ ] `./mvnw test` passes
- [ ] New/changed endpoints have correct `@PreAuthorize` role checks
- [ ] New/changed request DTOs have `@Valid` Bean Validation annotations
- [ ] New mutating actions call `AuditLogService`
- [ ] Any schema change is a new Flyway migration file (no edits to already-applied migrations)
- [ ] I've updated documentation (README/CHANGELOG) if needed

## Additional Notes

<!-- Anything reviewers should pay special attention to -->
