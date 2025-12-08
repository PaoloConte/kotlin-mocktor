package io.paoloconte.mocktor

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.paoloconte.mocktor.MockEngine.INITIAL_STATE
import io.paoloconte.mocktor.MockEngine.noMatchStatusCode
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class MockEngineStateTest {

    @Test
    fun `test state transitions and matching`() = runBlocking {
        MockEngine.clear()
        
        // Handler 1: Matches GET /login only in STARTED state, transitions to LOGGED_IN
        MockEngine.get("/login") {
            withState(INITIAL_STATE)
            response {
                status(HttpStatusCode.OK)
                body("Login successful")
                setState("LOGGED_IN")
            }
        }
        
        // Handler 2: Matches GET /profile only in LOGGED_IN state
        MockEngine.get("/profile") {
            withState("LOGGED_IN")
            response {
                status(HttpStatusCode.OK)
                body("User Profile")
            }
        }
        
        // Handler 3: Matches GET /logout only in LOGGED_IN state, transitions to STARTED
        MockEngine.get("/logout") {
            withState("LOGGED_IN")
            response {
                status(HttpStatusCode.OK)
                body("Logout successful")
                setState(INITIAL_STATE)
            }
        }
        
        val client = HttpClient(MockEngine)
        
        assertEquals(INITIAL_STATE, MockEngine.state)
        
        // 1. request /profile should fail (404) because state is INITIAL
        val profileResponse1 = client.get("http://localhost/profile")
        assertEquals(noMatchStatusCode, profileResponse1.status)
        
        // 2. request /login should succeed
        val loginResponse = client.get("http://localhost/login")
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        assertEquals("LOGGED_IN", MockEngine.state)
        
        // 3. request /profile should now succeed
        val profileResponse2 = client.get("http://localhost/profile")
        assertEquals(HttpStatusCode.OK, profileResponse2.status)
        
        // 4. request /login should now fail (404) because state is LOGGED_IN
        val loginResponse2 = client.get("http://localhost/login")
        assertEquals(noMatchStatusCode, loginResponse2.status)
        
        // 5. request /logout should succeed and reset state to INITIAL
        val logoutResponse = client.get("http://localhost/logout")
        assertEquals(HttpStatusCode.OK, logoutResponse.status)
        assertEquals(INITIAL_STATE, MockEngine.state)
    }
}
