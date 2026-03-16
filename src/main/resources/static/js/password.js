$("#passwordForm").off("submit").on("submit", function(e){

e.preventDefault();

const btn = $("#saveBtn");

/* prevent double click */
if(btn.prop("disabled")){
return;
}

btn.prop("disabled", true);

const data = {

accountName: $("#accountName").val(),
website: $("#website").val(),
username: $("#username").val(),
password: $("#password").val(),
category: $("#category").val(),
notes: $("#notes").val()

};

axios.post(
API_BASE + "/add-password",
data,
getAuthHeader()
)

.then(function(response){

$("#msg").text("Password saved successfully");

setTimeout(function(){

window.location.href="dashboard.html";

},800);

})

.catch(function(err){

console.log("Add Password Error:", err);

$("#msg").text("Failed to save password");

btn.prop("disabled", false);

});

});


function goDashboard(){

window.location.href="dashboard.html";

}

function checkStrength(){

const password =
document.getElementById("password").value;

let strength = "Weak";

const hasUpper =
/[A-Z]/.test(password);

const hasLower =
/[a-z]/.test(password);

const hasNumber =
/[0-9]/.test(password);

const hasSymbol =
/[@$!%*?&#]/.test(password);

/* VERY STRONG */

if(
password.length >= 12 &&
hasUpper &&
hasLower &&
hasNumber &&
hasSymbol
){

strength = "Very Strong";

}

/* STRONG */

else if(
password.length >= 8 &&
hasUpper &&
hasNumber &&
hasSymbol
){

strength = "Strong";

}

/* MEDIUM */

else if(
password.length >= 6 &&
(hasUpper || hasNumber)
){

strength = "Medium";

}

/* WEAK */

else{

strength = "Weak";

}

document.getElementById("strength").value = strength;

const strengthBox =
document.getElementById("strength");

strengthBox.value = strength;

if(strength==="Weak") strengthBox.style.color="red";
if(strength==="Medium") strengthBox.style.color="orange";
if(strength==="Strong") strengthBox.style.color="green";
if(strength==="Very Strong") strengthBox.style.color="darkgreen";

}