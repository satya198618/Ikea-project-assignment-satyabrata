# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor to a consistent approach. Currently we have three patterns:
- Product: Uses PanacheRepository (repository pattern)
- Store: Uses Active Record (PanacheEntity with static methods)
- Warehouse: Uses Hexagonal Architecture (domain model + ports/adapters)

For maintenance, I'd standardize on Warehouse's hexagonal approach for complex entities because it:
- Separates business logic from infrastructure (easier to test - see 94% coverage in use cases)
- Enforces domain rules through use cases (prevents invalid states)
- Makes database changes independent of business logic

For simpler entities like Product and Store, I'd keep the existing patterns but add an abstraction layer to avoid direct repository coupling in resources. This maintains flexibility without over-engineering.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
OpenAPI-first (Warehouse):
Pros: Contract-first guarantees API consistency, auto-generates models and interfaces, catches breaking changes early, serves as living documentation
Cons: More setup, requires code generation in build pipeline, can feel rigid for rapid prototyping

Code-first (Product/Store):
Pros: Faster initial development, more flexible for changes, less build complexity
Cons: API contract is implicit, harder to maintain consistency, manual documentation effort, no compile-time contract validation

My choice: OpenAPI-first for all public/external APIs. The Warehouse demonstrates this well - the yaml contract makes integration easier and prevents accidental breaking changes. For internal/simple APIs, code-first is acceptable but I'd still generate OpenAPI docs from code annotations rather than maintaining them manually.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
Priority 1 - Unit tests for business logic (already done): Test use cases with mocked dependencies. This gives 94% coverage of core domain logic and catches most bugs early. Fast execution (<5s) enables running on every commit.

Priority 2 - Integration tests for critical paths: Test end-to-end flows like warehouse creation → archive → replacement with real DB (H2). This catches database mapping issues and transaction problems (like the Store event synchronization).

Priority 3 - Contract tests for APIs: Especially for OpenAPI endpoints, validate request/response against the spec.

To maintain coverage over time:
- Enforce 80% JaCoCo thresholds in CI/CD (already configured)
- Exclude only infrastructure code (DTOs, generated code) from coverage
- Make tests fail-fast and descriptive so developers fix rather than skip them
- Review test quality during code reviews, not just coverage percentages
```