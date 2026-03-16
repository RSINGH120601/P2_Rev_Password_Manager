function toggle2FA(){

const isChecked = document.getElementById("twoFactorToggle").checked;

axios.put(API_BASE + "/toggle-2fa", {}, getAuthHeader())

.then(function(response){

alert(`2FA is now ${response.data.data ? 'ENABLED' : 'DISABLED'}`);
loadProfile();

})

.catch(function(err){

console.log(err);
alert("Failed to update 2FA");

});

}

function loadProfile() {
    axios.get(API_BASE + "/profile", getAuthHeader())
        .then(function(response) {
            if(response.data.success) {
                const p = response.data.data;
                document.getElementById("name").value = p.name || "";
                document.getElementById("email").value = p.email || "";
                document.getElementById("phone").value = p.phone || "";
                document.getElementById("twoFactorToggle").checked = p.twoFactorEnabled;

                // Store originals for cancellation
                window.originalProfileData = p;
            }
        })
        .catch(function(err){
            console.log("Error loading profile", err);
        });
}

function enableEdit() {
    document.getElementById("name").removeAttribute("readonly");
    document.getElementById("email").removeAttribute("readonly");
    document.getElementById("phone").removeAttribute("readonly");
    
    document.getElementById("editBtn").style.display = "none";
    document.getElementById("saveBtn").style.display = "block";
    document.getElementById("cancelBtn").style.display = "block";
}

function cancelEdit() {
    document.getElementById("name").setAttribute("readonly", true);
    document.getElementById("email").setAttribute("readonly", true);
    document.getElementById("phone").setAttribute("readonly", true);
    
    document.getElementById("editBtn").style.display = "block";
    document.getElementById("saveBtn").style.display = "none";
    document.getElementById("cancelBtn").style.display = "none";

    // Restore original values
    if (window.originalProfileData) {
        document.getElementById("name").value = window.originalProfileData.name || "";
        document.getElementById("email").value = window.originalProfileData.email || "";
        document.getElementById("phone").value = window.originalProfileData.phone || "";
    }
}

document.addEventListener("DOMContentLoaded", loadProfile);