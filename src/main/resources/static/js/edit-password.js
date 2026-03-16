const id = localStorage.getItem("editPasswordId");

let realPassword = "";

$(document).ready(function(){
    if(!id){
        window.location.href="dashboard.html";
        return;
    }

    axios.get(API_BASE + "/passwords/" + id, getAuthHeader())
    .then(function(res){
        const p = res.data.data;
        $("#accountName").val(p.accountName);
        $("#website").val(p.website);
        $("#username").val(p.username);
        
        realPassword = p.password;
        // Mask the password initially
        $("#password").val("********");
        $("#password").attr("readonly", true);

        $("#category").val(p.category);
        $("#notes").val(p.notes);
    })
    .catch(function(err){
        console.log("Error loading password", err);
    });
});

$("#updateForm").submit(function(e){

e.preventDefault();

let submitPassword = $("#password").val();
// If they never unlocked it, submit the real unedited password
if(submitPassword === "********") {
    submitPassword = realPassword;
}

const data={

id:id,
accountName:$("#accountName").val(),
website:$("#website").val(),
username:$("#username").val(),
password:submitPassword,
category:$("#category").val(),
notes:$("#notes").val()

};

axios.put(API_BASE+"/update-password",data,getAuthHeader())

.then(function(){

window.location.href="dashboard.html";

});

});

function goBack(){

window.location.href="dashboard.html";

}

function toggleVisibility(id) {
    const input = document.getElementById(id);
    
    // If it's already text (unlocked), just toggle it back to password
    if (input.type === "text") {
        input.type = "password";
        return;
    }

    // Attempting to unlock
    const master = prompt("Enter Master Password to view or edit this password");
    if(!master) return;

    axios.post(API_BASE + "/login", {
        username: localStorage.getItem("username"),
        masterPassword: master
    })
    .then(function(res) {
        if(res.data.success) {
            input.value = realPassword;
            input.type = "text";
            input.removeAttribute("readonly"); // Unlock for editing
        } else {
            alert("Invalid master password");
        }
    })
    .catch(function(err) {
        console.log("Validation failed", err);
        alert("Verification failed");
    });
}