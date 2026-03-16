function generatePassword() {

const length = parseInt(document.getElementById("length").value);

const upper = document.getElementById("uppercase").checked;
const numbers = document.getElementById("numbers").checked;
const symbols = document.getElementById("symbols").checked;

let chars = "abcdefghijklmnopqrstuvwxyz";

if (upper) {
chars += "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
}

if (numbers) {
chars += "0123456789";
}

if (symbols) {
chars += "!@#$%^&*()_+";
}

let password = "";

for (let i = 0; i < length; i++) {
password += chars.charAt(Math.floor(Math.random() * chars.length));
}

document.getElementById("generatedPassword").value = password;
}


function copyPassword() {

const input = document.getElementById("generatedPassword");

input.select();
document.execCommand("copy");

alert("Password copied!");

}

function saveGeneratedPassword(){

const generatedPassword =
document.getElementById("generatedPassword").value;

if(!generatedPassword){

alert("Generate a password first");

return;

}

localStorage.setItem("generatedPassword", generatedPassword);

window.location.href = "add-password.html";

}