const API_BASE = "http://localhost:8084/auth";

/**
 * Retrieves the JWT token from localStorage and formats it for HTTP requests.
 * Why: Ensures every authenticated API call securely transports the user's session token.
 */
function getAuthHeader() {

    const token = localStorage.getItem("token");

    return {
        headers: {
            Authorization: "Bearer " + token
        }
    };
}