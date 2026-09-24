# Backend

Spring Boot REST API for PrepareForTraining Management. See `../docs` for requirements, API contracts, database design, and implementation plan.

## Start here

1. Copy `src/main/resources/application-example.yml` to `application-local.yml` and configure PostgreSQL.
2. Create the database `prepare_for_training`.
3. Run `mvn spring-boot:run`.
4. Test `GET http://localhost:8080/api/v1/health`.
5. Follow `../docs/IMPLEMENTATION_PLAN.md` in order.

Do not commit passwords or JWT secrets. Use environment variables or `application-local.yml`.
