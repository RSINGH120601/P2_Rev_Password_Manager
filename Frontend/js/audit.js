function loadAudit() {
    axios.get(API_BASE + "/audit", { headers: { Authorization: "Bearer " + localStorage.getItem("token") } })
        .then(function(response) {
            const audit = response.data.data;
            const weak = audit.weakPasswords;
            const reused = audit.reusedPasswords;

            document.getElementById("auditScore").innerText = audit.securityScore;
            document.getElementById("auditWeak").innerText = weak.length;
            document.getElementById("auditReuse").innerText = reused.length;

            let weakHtml = "";
            weak.forEach(p => {
                weakHtml += `<div class="p-2 border-bottom border-secondary">${p.accountName} (${p.username})</div>`;
            });
            document.getElementById("weakList").innerHTML = weakHtml || "<div class='p-2 text-success'>None!</div>";

            let reusedHtml = "";
            reused.forEach(p => {
                reusedHtml += `<div class="p-2 border-bottom border-secondary">${p.accountName} (${p.username})</div>`;
            });
            document.getElementById("reusedList").innerHTML = reusedHtml || "<div class='p-2 text-success'>None!</div>";
            
            // Adjust score color:
            const scoreEl = document.getElementById("auditScore");
            if(audit.securityScore > 80) scoreEl.className = "m-0 display-4 fw-bold text-success";
            else if(audit.securityScore > 50) scoreEl.className = "m-0 display-4 fw-bold text-warning";
            else scoreEl.className = "m-0 display-4 fw-bold text-danger";
        })
        .catch(function(err) {
            console.log("Audit error:", err);
            if(err.response && err.response.status === 401) {
                window.location.href="login.html";
            }
        });
}

function goDashboard() {
    window.location.href = "dashboard.html";
}

loadAudit();
