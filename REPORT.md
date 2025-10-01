# Lab 2 Web Server -- Project Report

## Description of Changes

**- Customize the Whitelabel Error Page:**  
Created an `error.html` file in `src/main/resources/templates`. The page displays a red gradient background with a centered black container that shows the error type and its description, replacing the default plain Spring Boot error page.  

**- Add a New Endpoint:**  
Added the `TimeComponent.kt` file in `src/main/kotlin/es/unizar/webeng/lab2`, which contains all necessary components for the new endpoint: DTO for response representation, provider interface, service implementation and REST Controller. This endpoint exposes the current server time in JSON format and has been documented with Swagger annotations for clarity and usability.  

**- Enable HTTP/2 and SSL Support:**  
Configured the server to support **HTTP/2** and **SSL**.  
A self-signed certificate was generated for local testing (not uploaded to GitHub for security reasons). This ensures encrypted communication and modern protocol support.  

**- Enable HTTP Response Compression:**  
Activated response compression for multiple MIME types (`application/json`, `text/html`, `text/plain`), with a minimum size threshold of **1 KB**. This reduces payload sizes and improves application performance.  

**- Implement Swagger/OpenAPI Documentation:**  
Integrated **Springdoc OpenAPI** to auto-generate Swagger documentation.  
All REST controllers are documented with summaries, descriptions, response schemas, and examples. A **bearer authentication scheme** was also added to the spec, along with proper exclusion of management endpoints (`/actuator`).  

**- Testing:**  
Comprehensive tests were added in `src/test/kotlin` to validate each feature:  
- Custom error page rendering.  
- Time endpoint availability and correctness.  
- Response compression behavior (large vs. small payloads, SSE exclusion).  
- Swagger/OpenAPI specification integrity (presence of `/time`, examples and bearer scheme). 


## Technical Decisions

- **Swagger / OpenAPI:**: 
Given the experience and successful use of Swagger/OpenAPI in previous projects, this standard was adopted once again for API documentation.Chosen to auto-generate OpenAPI JSON (`/v3/api-docs`) and provide Swagger UI (`/swagger-ui/index.html`) with minimal configuration and annotation-based control.

- **OpenAPI configuration bean (`OpenApiConfig.kt`)**: 
Added a centralized configuration to:
  - Declare a global HTTP Bearer security scheme (`bearerAuth`)
  - Exclude management endpoints (`/actuator/**`).

- **Testing approach — Integration tests**:
Note that there is an `application.yml` under `src/test/resources`, to ensure a correct configuration for running the tests.
  - Tests start the embedded server (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) because `server.compression` and `springdoc` depend on the real server behavior.
  - `TestRestTemplate` used for OpenAPI / Swagger checks; lightweight `HttpURLConnection` used for low-level header/body checks of compression behavior.
  - Only a MVC test is implemented to ensure that the `/time` endpoint works.

- **Compression configuration**:
  Configuration placed under `server.compression` in both `application.yml`.
  - `SseEmitter` used for `/test/sse` with `produces = text/event-stream` so streaming responses are naturally `text/event-stream` and excluded from compression by server configuration.

- **Self-signed certificate**:  
  A self-signed certificate has been generated for local testing (not included in the GitHub repository for security reasons). The server is configured to enable **HTTP/2** and **SSL** when running via `./gradlew bootRun`.  
  To run the application locally with HTTPS, you need to create a self-signed certificate on your machine and provide its details in an `application-secrets.properties` file.


## Learning Outcomes

- **Testing strategies**: 
Acquired hands-on experience deciding when to use integration tests (embedded server) vs. controller-only tests (`MockMvc`). Explored `HttpURLConnection` for low-level header/body inspection and applied TestRestTemplate for endpoint-level checks.

- **HTTP compression**: 
Understood how `server.compression` behaves in Spring Boot and designed assertions that adapt to those variations. Practiced the proper way of exposing SSE with SseEmitter and recognized the need to exclude streaming media types from compression.

- **OpenAPI/Swagger best practices**: 
Gained familiarity with annotating endpoints to include examples, multiple media types (including vendor-specific ones), and configuring global security schemes and server entries so generated docs stay aligned with runtime (HTTP/HTTPS).

- **Configure SSL and HTTP/2**:
Developed the ability to generate a self-signed certificate (public-private key) and set up a PKCS12 keystore for HTTPS in Spring Boot. Also applied good practices like isolating sensitive information in an `application-secrets.properties` file and keeping secrets out of version control.

- **Customize error page:**
Learned how to replace the default Spring Boot error pages with a custom `error.html` and how to use Thymeleafs error variables in that error page.


## AI Disclosure
### AI Tools Used
- ChatGPT

### AI-Assisted Work

**- The AI generated:**
  - Test implementations: Compression tests, Swagger/OpenAPI tests, error page integration tests.
  - Documentation: KDoc comments and annotations for Swagger.
  - Initial model for `error.html` page.

**- Percentage estimate:** ~50% AI-assisted.  
  - Code examples, test scaffolding and documentation were produced with AI help.
  - The remaining ~50% was manual: reviewing all the AI-generated content, selecting where to place files, adapting imports, running the build/tests, debugging environment-specific test failures and making small fixes.

**- AI-Generated Code Modifications:**
  - Content corrections in KDoc blocks (clarified summaries).
  - Import reordering, formatting adjustments and minor refactorings to satisfy project linting and style rules.
  - Hardening and completion of generated tests (additional assertions, fixtures and teardown/cleanup).
  - Created the test keystore locally and configured the SSL test properties accordingly.
  - When tests failed, manual debugging and targeted code/test tweaks were applied.

### Original Work

- **Project integration & validation**:
  - Running the Gradle build and iterating on failing tests.
  - Creating and placing the PKCS12 keystore in `src/test/resources` (local, not pushed to Git).
  - Confirming runtime behavior with `curl` and reading application logs to diagnose test failures.
  - Selecting the final, production/test-safe `application.yml` values.
  - Implemented the application features and wrote the majority of the `src/main/` code.

- **Learning process**:
  - Read and interpreted Spring Boot logs and test failures to determine the root cause of issues.
  - Decided how to harden tests so they pass reliably in a specific environment.
  - Manually validated the OpenAPI JSON output and cross-checked Swagger UI behavior in a browser.

  This learning was reinforced by actively integrating and adapting AI-driven recommendations into the project.
