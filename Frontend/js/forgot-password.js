function loadQuestions(){

const username = document.getElementById("username").value;

axios.get(API_BASE + "/security-questions/" + username)

.then(function(response){

const questions = response.data.data;

document.getElementById("questionSection").style.display="block";

document.getElementById("q1").innerText = questions[0].questionText;
document.getElementById("q2").innerText = questions[1].questionText;
document.getElementById("q3").innerText = questions[2].questionText;

})

.catch(function(){

alert("User not found");

});

}


function resetPassword(){

const a1 = document.getElementById("a1").value.trim();
const a2 = document.getElementById("a2").value.trim();
const a3 = document.getElementById("a3").value.trim();
const newPassword = document.getElementById("newPassword").value;

if (!a1 || !a2 || !a3) {
    document.getElementById("msg").innerText = "All security questions must be answered!";
    return;
}

if (!newPassword) {
    document.getElementById("msg").innerText = "Please provide a new password.";
    return;
}

const data={

username: document.getElementById("username").value,

answers:[a1, a2, a3],

newPassword: newPassword

};

axios.post(API_BASE + "/recover-password", data)

.then(function(){

alert("Password reset successful");

window.location.href="login.html";

})

.catch(function(err){

    if (err.response && err.response.data && err.response.data.message) {
        document.getElementById("msg").innerText = err.response.data.message;
    } else {
        document.getElementById("msg").innerText = "Verification failed";
    }

});

}