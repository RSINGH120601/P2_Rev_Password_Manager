let currentSort = "id";
/**
 * Logs the user out of the application.
 * Why: Clears the JWT token from local storage to ensure the session is securely terminated.
 */
function logout(){

localStorage.removeItem("token");

window.location.href = "login.html";

}
/**
 * Redirects the user to the Add Password page.
 * Why: Simple navigation helper for UX consistency.
 */
function goAddPassword(){

window.location.href = "add-password.html";

}
/**
 * Fetches and renders the user's saved passwords in the table.
 * Why: The core function of the dashboard. Retrieves paginated, sorted data from the backend
 * and dynamically injects HTML rows. It also recalculates dashboard statistics (totals, weak counts).
 */
function loadPasswords(){

axios.get(API_BASE + "/passwords?page=0&size=10&sortBy=" + currentSort, getAuthHeader())

.then(function(response){

const data = response.data.data.content;

let rows = "";

let total = data.length;
let favorites = 0;
let weak = 0;

data.forEach(function(p){

if(p.favorite){
favorites++;
}

if(p.password && p.password.length < 8){
weak++;
}

rows += `

<tr>

<td>${p.accountName}</td>
<td>${p.website}</td>
<td>${p.username}</td>
<td>
<span id="pwd-${p.id}">
********
</span>
<button class="btn btn-sm btn-info"
onclick="viewPassword(${p.id}, '${p.password}')">
View
</button>
</td>
<td>${p.category}</td>

<td class="text-center">
<span class="favorite-star ${p.favorite ? 'active' : ''}" onclick="toggleFavorite(${p.id})">★</span>
</td>

<td>

<button class="btn btn-primary btn-sm"
onclick="editPassword(${p.id})">
Edit
</button>

<button class="btn btn-danger btn-sm"
onclick="deletePassword(${p.id})">
Delete
</button>

</td>

</tr>

`;

});

$("#passwordTable").html(rows);

/* update statistics */

document.getElementById("totalPasswords").innerText = total;
document.getElementById("favoritePasswords").innerText = favorites;
document.getElementById("weakPasswords").innerText = weak;

if (weak > 0) {
    document.getElementById("weakPasswordAlert").style.display = "block";
    document.getElementById("weakCountDisplay").innerText = weak;
} else {
    document.getElementById("weakPasswordAlert").style.display = "none";
}

})

.catch(function(err){

console.error("Error loading passwords:", err);
if (err.response && err.response.data && err.response.data.message) {
    alert("Backend Error filtering passwords: " + err.response.data.message);
} else {
    alert("Failed to load passwords from backend.");
}

});

}
/**
 * Deletes a password by ID.
 * Why: Sends an API request to remove credentials. Includes a confirmation prompt 
 * to prevent accidental data loss.
 */
function deletePassword(id){

if(confirm("Are you sure you want to delete this password?")) {
    axios.delete(
    API_BASE + "/delete-password/" + id,
    getAuthHeader()
    )

    .then(function(){

    loadPasswords();

    })

    .catch(function(err){

    console.log("Delete Error:", err);

    });
}

}
/**
 * Redirects to the Edit Password page for a specific credential entry.
 * Why: Temporarily stores the target ID in localStorage to pass context to the edit page.
 */
function editPassword(id){

localStorage.setItem("editPasswordId", id);

window.location.href = "edit-password.html";

}
/**
 * Toggles the "star" favorite status for a password.
 * Why: Quick-action UX function that updates the backend and immediately reloads 
 * the dashboard to reflect the new state.
 */
function toggleFavorite(id){

axios.put(
API_BASE + "/toggle-favorite/" + id,
{},
getAuthHeader()
)

.then(function(){

loadPasswords();

})

.catch(function(err){

console.log("Favorite Toggle Error:", err);

});

}

loadPasswords();

/**
 * Filters the displayed password rows locally based on a search term.
 * Why: Provides instantaneous client-side feedback without hitting the backend,
 * enhancing user experience when looking for a specific site or username.
 */
function searchPasswords(){

const keyword =
document.getElementById("searchBox").value.toLowerCase();

const rows =
document.querySelectorAll("#passwordTable tr");

rows.forEach(function(row){

const text = row.innerText.toLowerCase();

if(text.includes(keyword)){
row.style.display="";
}
else{
row.style.display="none";
}

});

}

/**
 * Redirects to the Password Generator utility page.
 * Why: Navigation convenience to help users create strong passwords directly from their vault.
 */
function goGenerator(){

window.location.href="generate-password.html";

}

/**
 * Filters the displayed password rows locally based on a specific category.
 * Why: Speeds up UI interaction by hiding non-matching rows instantly instead of
 * requiring a round-trip to the server.
 */
function filterCategory(){
const category =
document.getElementById("categoryFilter").value.toLowerCase();
const rows =
document.querySelectorAll("#passwordTable tr");
rows.forEach(function(row){
if(category === ""){
row.style.display="";
return;
}
if(row.innerText.toLowerCase().includes(category)){
row.style.display="";
}else{
row.style.display="none";
}
});
}


/**
 * Re-fetches the passwords sorted by the selected criteria (e.g., id, accountName).
 * Why: Relies on the backend to accurately sort the paginated dataset globally across
 * the entire user vault rather than just the visible page.
 */
function sortPasswords(){
currentSort =
document.getElementById("sortFilter").value;
loadPasswords();
}


/**
 * Reveals an encrypted password by requesting the user to re-verify their Master Password.
 * Why: Implements "Zero-Knowledge" rendering on the client side. Passwords stay hidden
 * until explicitly requested, protecting them from shoulder-surfing or unattended sessions.
 */
function viewPassword(id, password){

const master =
prompt("Enter Master Password to view this password");

if(master===null){
return;
}

axios.post(
API_BASE + "/login",
{
username: localStorage.getItem("username"),
masterPassword: master
}
)

.then(function(response){

if(response.data.success){

document.getElementById("pwd-"+id).innerText = password;

}else{

alert("Invalid master password");

}

})

.catch(function(){

alert("Verification failed");

});

}


/**
 * Redirects to the Security Audit report page.
 * Why: Allows users to view their calculated security score and inspect weak/reused passwords.
 */
function goAudit(){

window.location.href="security-audit.html";

}


/**
 * Fetches all of the user's decrypted passwords and downloads them as a JSON file.
 * Why: Ensures users have control of their own data and can create offline backups.
 */
function exportVault(){

axios.get(
API_BASE + "/passwords?page=0&size=100",
getAuthHeader()
)

.then(function(response){

const data = response.data.data.content;

const file =
new Blob([JSON.stringify(data, null, 2)],
{type:"application/json"});

const url = URL.createObjectURL(file);

const a = document.createElement("a");

a.href = url;
a.download = "password_backup.json";

a.click();

});

}

/**
 * Imports a previously exported JSON backup file.
 * Why: Parses the file locally, then sends the payload to the backend to bulk-restore
 * the encrypted vault. Ensures seamless device migration.
 */
function importVault(event){

const file = event.target.files[0];

if(!file){
return;
}

const reader = new FileReader();

reader.onload = function(e){

const data = JSON.parse(e.target.result);

data.forEach(function(p){

axios.post(
API_BASE + "/add-password",
{
accountName: p.accountName,
website: p.website,
username: p.username,
password: p.password,
category: p.category,
notes: p.notes
},
getAuthHeader()
);

});

alert("Vault imported successfully");

loadPasswords();

};

reader.readAsText(file);

}