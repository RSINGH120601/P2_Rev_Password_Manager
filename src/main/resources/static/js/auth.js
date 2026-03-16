/**
 * Intercepts the login form submission to authenticate the user.
 * Why: Prevents default HTML form submission. Uses Axios to send credentials to the backend,
 * processes the JWT token on success, and handles the 2FA redirect if Two-Factor is enabled.
 */
$("#loginForm").submit(function (e) {

    e.preventDefault();

    const username = $("#username").val();
    const password = $("#password").val();

    axios.post(API_BASE + "/login", {

        username: username,
        masterPassword: password

    })

    .then(function (response) {

        const res = response.data;

        console.log("Login Response:", res);

        if (res.message === "2FA_REQUIRED") {

            localStorage.setItem("tempUser", username);

            window.location.href = "verify2fa.html?username=" + encodeURIComponent(username);
        }

        else if (res.success) {

            localStorage.setItem("token", res.data);
            localStorage.setItem("username", username);

            window.location.href = "dashboard.html";
        }

        else {

            $("#message").text(res.message);
        }

    })

    .catch(function (error) {

        console.log("Login Error:", error);

        if (error.response) {
            $("#message").text(error.response.data.message);
        } else {
            $("#message").text("Server error");
        }

    });

});