const API_BASE = "http://localhost:8084/auth";

function getAuthHeader(){

    const token = localStorage.getItem("token");

    return {
        headers:{
            Authorization: "Bearer " + token,
            "Content-Type": "application/json"
        }
    };

}