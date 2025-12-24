# Mocktor

A Kotlin library for mocking Ktor HTTP client requests in tests. Mocktor provides a declarative DSL for setting up mock responses and matching requests by path, method, headers, and body content.

## Features

- Simple DSL for defining mock HTTP responses
- Support for all HTTP methods (GET, POST, PUT, DELETE, PATCH, HEAD)
- Request body matching with pluggable content matchers
- Built-in JSON and XML content matchers for semantic comparison
- Form URL-encoded body matching
- Query parameter matching
- Request verification (assert requests were made)

## Modules

- **lib** - Core module with MockEngine and basic request matching
- **lib-json-matcher** - JSON content matcher using kotlinx.serialization
- **lib-xml-matcher** - XML content matcher using XMLUnit

## Installation

Add the dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    testImplementation("io.paoloconte:mocktor:1.4.0")

    // Optional: JSON body matching
    testImplementation("io.paoloconte:mocktor-json:1.4.0")

    // Optional: XML body matching
    testImplementation("io.paoloconte:mocktor-xml:1.4.0")
}
```

## Usage

### Basic Setup

Create an HTTP client using the `MockEngineFactory`:

```kotlin
import io.ktor.client.*
import io.paoloconte.mocktor.MockEngineFactory
import io.paoloconte.mocktor.MockEngine

val client = HttpClient(MockEngineFactory)
```

### Defining Mock Responses

```kotlin
// Mock a GET request
MockEngine.get("/api/users") {
    response {
        status(HttpStatusCode.OK)
        body("""{"users": []}""")
    }
}

// Mock a POST request
MockEngine.post("/api/users") {
    response {
        status(HttpStatusCode.Created)
        body("""{"id": 1}""")
    }
}
```

### Matching Request Body

Match exact request body content:

```kotlin
MockEngine.post("/api/users") {
    request {
        body("""{"name": "John"}""")
    }
    response {
        status(HttpStatusCode.Created)
    }
}
```

### Query Parameter Matching

Match requests by query parameters:

```kotlin
MockEngine.get("/api/users") {
    request {
        queryParams {
            param("page", "1")
            param("limit", "10")
        }
    }
    response {
        status(HttpStatusCode.OK)
        body("""{"users": [], "page": 1}""")
    }
}

// Matches: GET /api/users?page=1&limit=10
// Matches: GET /api/users?limit=10&page=1  (order doesn't matter)
```

Ignore extra query parameters:

```kotlin
MockEngine.get("/api/users") {
    request {
        queryParams(ignoreUnknownParams = true) {
            param("page", "1")
        }
    }
    response {
        status(HttpStatusCode.OK)
    }
}

// Matches: GET /api/users?page=1&anyOtherParam=value
```

Ignore specific parameters (e.g., timestamps):

```kotlin
MockEngine.get("/api/users") {
    request {
        queryParams {
            param("page", "1")
            param("timestamp", "ignored")
            ignoreParam("timestamp")
        }
    }
    response {
        status(HttpStatusCode.OK)
    }
}
```

### Form URL-Encoded Body Matching

Match form data in POST requests:

```kotlin
MockEngine.post("/api/login") {
    request {
        formBody {
            param("username", "john")
            param("password", "secret")
        }
    }
    response {
        status(HttpStatusCode.OK)
    }
}

// Matches requests with body: username=john&password=secret
// Parameter order doesn't matter
```

Ignore extra form fields:

```kotlin
MockEngine.post("/api/login") {
    request {
        formBody(ignoreUnknownKeys = true) {
            param("username", "john")
        }
    }
    response {
        status(HttpStatusCode.OK)
    }
}

// Matches even if request has additional fields
```

Ignore specific fields:

```kotlin
MockEngine.post("/api/data") {
    request {
        formBody {
            param("name", "test")
            param("timestamp", "ignored")
            ignoreField("timestamp")
        }
    }
    response {
        status(HttpStatusCode.OK)
    }
}
```

### Custom Request Matching

Use `matching` for custom request validation:

```kotlin
MockEngine.get("/api/users") {
    request {
        matching { request ->
            request.url.parameters["id"] == "123"
        }
    }
    response {
        status(HttpStatusCode.OK)
        body("""{"id": 123, "name": "John"}""")
    }
}

// Match based on request body content
MockEngine.post("/api/users") {
    request {
        matching { request ->
            val body = request.bodyAsString()
            body.contains("\"role\":\"admin\"")
        }
    }
    response {
        status(HttpStatusCode.OK)
    }
}
```

### JSON Content Matching

Use the JSON matcher for semantic JSON comparison (ignores key ordering and whitespace):

```kotlin
import io.paoloconte.mocktor.json.jsonBody

MockEngine.post("/api/users") {
    request {
        jsonBody("""{"name": "John", "age": 30}""")
    }
    response {
        status(HttpStatusCode.Created)
    }
}

// This request will match even with different key order:
client.post("/api/users") {
    setBody("""{"age": 30, "name": "John"}""")
}
```

### XML Content Matching

Use the XML matcher for semantic XML comparison (ignores whitespace and comments):

```kotlin
import io.paoloconte.mocktor.xml.xmlBody

MockEngine.post("/api/data") {
    request {
        xmlBody("<root><item>value</item></root>")
    }
    response {
        status(HttpStatusCode.OK)
    }
}

// This request will match even with different formatting:
client.post("/api/data") {
    setBody("""
        <root>
            <item>value</item>
        </root>
    """)
}
```

### Setting Response Content Type

```kotlin
MockEngine.get("/api/data") {
    response {
        status(HttpStatusCode.OK)
        contentType(ContentType.Application.Xml)
        body("<data/>")
    }
}
```

### Loading Body from Resources

Load request or response body from classpath resource files:

```kotlin
MockEngine.post("/api/users") {
    request {
        bodyFromResource("/fixtures/request.json")
    }
    response {
        bodyFromResource("/fixtures/response.json")
    }
}
```

### Loading JSON Body from Resources

Use `jsonBodyFromResource` to load JSON from a resource file and use semantic JSON comparison:

```kotlin
import io.paoloconte.mocktor.json.jsonBodyFromResource

MockEngine.post("/api/users") {
    request {
        jsonBodyFromResource("/fixtures/request.json")
    }
    response {
        status(HttpStatusCode.Created)
    }
}

// Matches even if request has different key order than the resource file
```

### Loading XML Body from Resources

Use `xmlBodyFromResource` to load XML from a resource file and use semantic XML comparison:

```kotlin
import io.paoloconte.mocktor.xml.xmlBodyFromResource

MockEngine.post("/api/data") {
    request {
        xmlBodyFromResource("/fixtures/request.xml")
    }
    response {
        status(HttpStatusCode.OK)
    }
}

// Matches even if request has different whitespace than the resource file
```

### Request Verification

Verify that specific requests were made during a test. All requests are recorded regardless of whether they match a handler.

```kotlin
// Make some requests
client.post("http://localhost/api/bookings")
client.post("http://localhost/api/bookings")

// Verify exact count
MockEngine.verify(count = 2) {
    method(HttpMethod.Post)
    path("/api/bookings")
}

// Verify at least one request was made (no count = at least 1)
MockEngine.verify {
    method(HttpMethod.Get)
    path("/api/users")
}

// Verify no requests were made
MockEngine.verify(count = 0) {
    method(HttpMethod.Delete)
}
```

Verify with headers:

```kotlin
client.get("http://localhost/api/users") {
    header("Authorization", "Bearer token123")
}

MockEngine.verify(count = 1) {
    path("/api/users")
    header("Authorization", "Bearer token123")
}
```

Verify with query parameters:

```kotlin
client.get("http://localhost/api/users?page=1&limit=10")

MockEngine.verify(count = 1) {
    path("/api/users")
    queryParams {
        param("page", "1")
        param("limit", "10")
    }
}
```

Access recorded requests directly:

```kotlin
val requests = MockEngine.requests()
assertEquals(2, requests.size)
assertEquals(HttpMethod.Post, requests[0].method)
assertEquals("/api/users", requests[0].url.encodedPath)
```

### State Management
MockEngine supports state-based request matching. This allows you to simulate stateful interactions (e.g., authentication flows):

```kotlin
// Handler matches only if engine state is "INITIAL_STATE" (default)
MockEngine.post("/login") {
    withState(MockEngine.INITIAL_STATE)
    response {
        status(HttpStatusCode.OK)
        setState("LOGGED_IN") // Transition to new state
    }
}

// Handler matches only if engine state is "LOGGED_IN"
MockEngine.get("/profile") {
    withState("LOGGED_IN")
    response {
        status(HttpStatusCode.OK)
        body("User Profile")
    }
}
```

### Clearing Handlers

Clear all registered handlers and recorded requests between tests:

```kotlin
@AfterTest
fun tearDown() {
    MockEngine.clear()
}
```

### Default Response Status

By default, MockEngine returns a `404 Not Found` when no handler matches a request. You can customize this status code:

```kotlin
// Change the default status code for unmatched requests
MockEngine.noMatchStatusCode = HttpStatusCode.BadRequest
```

## Custom Content Matchers

 Implement the `ContentMatcher` interface to create custom logic for comparing the request body sent by the client against the expected body specified in the mock.

 ```kotlin
 import io.paoloconte.mocktor.ContentMatcher
 import io.paoloconte.mocktor.MatchResult

 // Custom matcher that compares body and expected value case-insensitively
 val caseInsensitiveMatcher = object : ContentMatcher {
     override fun matches(body: ByteArray, expected: ByteArray): MatchResult {
         val bodyString = body.decodeToString().lowercase()
         val expectedString = expected.decodeToString().lowercase()

         return if (bodyString == expectedString)
            MatchResult.Match
         else
            MatchResult.Mismatch("Body mismatch: expected '$expectedString' but got '$bodyString'")
     }
 }

 MockEngine.post("/api/data") {
     request {
         body("HELLO WORLD")  // expected body to compare against
         withContentMatcher(caseInsensitiveMatcher)
     }
     response {
         status(HttpStatusCode.OK)
     }
 }

 // This request will match because "hello world" equals "HELLO WORLD" case-insensitively
 client.post("/api/data") {
     setBody("hello world")
 }
 ```

 Note: If you only need to validate the request body without comparing it to an expected value, use the `matching` lambda instead (see [Custom Request Matching](#custom-request-matching)).

 ## How It Works

 1. Register mock handlers using `MockEngine.get()`, `MockEngine.post()`, etc.
 2. Each handler specifies a path and optional matching criteria
 3. When the HTTP client makes a request, MockEngine records it and iterates through handlers
 4. The first matching handler returns its configured response
 5. If no handler matches, a response with `noMatchStatusCode` (default 404 Not Found) is returned
 6. Use `MockEngine.verify()` to assert that expected requests were made

 ### Debugging Mismatches

 If no handler matches a request, MockEngine returns a response with `noMatchStatusCode` (default 404 Not Found)
 containing a detailed report in the body explaining why each registered handler failed to match, identifying the
 mismatch reason (e.g., incorrect path, method, headers, or body content).
 The same information is also logged via slf4j.

 ## License

 MIT