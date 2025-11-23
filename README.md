# Mocktor

A Kotlin library for mocking Ktor HTTP client requests in tests. Mocktor provides a declarative DSL for setting up mock responses and matching requests by path, method, headers, and body content.

## Features

- Simple DSL for defining mock HTTP responses
- Support for all HTTP methods (GET, POST, PUT, DELETE, PATCH, HEAD)
- Request body matching with pluggable content matchers
- Built-in JSON and XML content matchers for semantic comparison
- Custom request matching with lambda expressions
- Query parameter matching

## Modules

- **lib** - Core module with MockEngine and basic request matching
- **lib-json-matcher** - JSON content matcher using kotlinx.serialization
- **lib-xml-matcher** - XML content matcher using XMLUnit

## Installation

Add the dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    testImplementation("io.paoloconte:mocktor:1.0")

    // Optional: JSON body matching
    testImplementation("io.paoloconte:mocktor-json:1.0")

    // Optional: XML body matching
    testImplementation("io.paoloconte:mocktor-xml:1.0")
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
    respond {
        status(HttpStatusCode.OK)
        body("""{"users": []}""")
    }
}

// Mock a POST request
MockEngine.post("/api/users") {
    respond {
        status(HttpStatusCode.Created)
        body("""{"id": 1}""")
    }
}
```

### Matching Request Body

Match exact request body content:

```kotlin
MockEngine.post("/api/users") {
    body("""{"name": "John"}""")
    respond {
        status(HttpStatusCode.Created)
    }
}
```

### Custom Request Matching

Use `matching` for custom request validation:

```kotlin
MockEngine.get("/api/users") {
    matching { request ->
        request.url.parameters["id"] == "123"
    }
    respond {
        status(HttpStatusCode.OK)
        body("""{"id": 123, "name": "John"}""")
    }
}

// Match based on request body content
MockEngine.post("/api/users") {
    matching { request ->
        val body = request.bodyAsString()
        body.contains("\"role\":\"admin\"")
    }
    respond {
        status(HttpStatusCode.OK)
    }
}
```

### JSON Content Matching

Use the JSON matcher for semantic JSON comparison (ignores key ordering and whitespace):

```kotlin
import io.paoloconte.mocktor.json.jsonBody

MockEngine.post("/api/users") {
    jsonBody("""{"name": "John", "age": 30}""")
    respond {
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
    xmlBody("<root><item>value</item></root>")
    respond {
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
    respond {
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
    bodyFromResource("/fixtures/request.json")
    respond {
        bodyFromResource("/fixtures/response.json")
    }
}
```

### Loading JSON Body from Resources

Use `jsonBodyFromResource` to load JSON from a resource file and use semantic JSON comparison:

```kotlin
import io.paoloconte.mocktor.json.jsonBodyFromResource

MockEngine.post("/api/users") {
    jsonBodyFromResource("/fixtures/request.json")
    respond {
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
    xmlBodyFromResource("/fixtures/request.xml")
    respond {
        status(HttpStatusCode.OK)
    }
}

// Matches even if request has different whitespace than the resource file
```

### Clearing Handlers

Clear all registered handlers between tests:

```kotlin
@AfterTest
fun tearDown() {
    MockEngine.clear()
}
```

## Custom Content Matchers

Implement the `ContentMatcher` interface to create custom body matching logic:

```kotlin
import io.paoloconte.mocktor.ContentMatcher

val customMatcher = object : ContentMatcher {
    override fun matches(body: ByteArray, target: ByteArray): Boolean {
        // Custom matching logic
        return body.decodeToString().contains("expected")
    }
}

MockEngine.post("/api/data") {
    body("expected content")
    withContentMatcher(customMatcher)
    respond {
        status(HttpStatusCode.OK)
    }
}
```

## How It Works

1. Register mock handlers using `MockEngine.get()`, `MockEngine.post()`, etc.
2. Each handler specifies a path and optional matching criteria
3. When the HTTP client makes a request, MockEngine iterates through handlers
4. The first matching handler returns its configured response
5. If no handler matches, a 404 Not Found response is returned

## License

MIT