const API_URL = "http://localhost:8080/smart-parking/api";

// ================= REGISTER =================
async function registerUser(event) {
    event.preventDefault();

    const fullName = document.getElementById("fullName").value;
    const email = document.getElementById("email").value;
    const phone = document.getElementById("phone").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    if (password !== confirmPassword) {
        alert("Passwords do not match!");
        return;
    }

    try {
        const response = await fetch(`${API_URL}/register`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                fullName: fullName,
                email: email,
                phone: phone,
                password: password
            })
        });

        const result = await response.text();

        if (response.ok) {
            alert("Registration successful!");
            window.location.href = "login.html";
        } else {
            alert("Registration failed: " + result);
        }

    } catch (error) {
        console.error(error);
        alert("Cannot connect to Java backend.");
    }
}


// ================= LOGIN =================
async function loginUser(event) {
    event.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    try {
        const response = await fetch(`${API_URL}/login`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: new URLSearchParams({
                email: email,
                password: password
            })
        });

        const result = await response.text();

        if (response.ok) {
            localStorage.setItem("user", result);
            alert("Login successful!");
            window.location.href = "dashboard.html";
        } else {
            alert("Login failed: " + result);
        }

    } catch (error) {
        console.error(error);
        alert("Cannot connect to Java backend.");
    }
}


// ================= GET PARKING SLOTS =================
async function loadParkingSlots() {

    try {
        const response = await fetch(`${API_URL}/slots`);

        const slots = await response.json();

        console.log("Parking Slots:", slots);

        const container = document.getElementById("parkingSlots");

        if (!container) {
            return;
        }

        container.innerHTML = "";

        slots.forEach(slot => {

            const button = document.createElement("button");

            button.innerText =
                slot.slotNumber + " - " + slot.status;

            button.className =
                slot.status === "AVAILABLE"
                    ? "available"
                    : "occupied";

            button.disabled =
                slot.status !== "AVAILABLE";

            button.onclick = function () {
                selectSlot(slot.slotNumber);
            };

            container.appendChild(button);
        });

    } catch (error) {
        console.error(error);
        alert("Cannot connect to Java backend.");
    }
}


// ================= SELECT SLOT =================
function selectSlot(slotNumber) {

    localStorage.setItem("selectedSlot", slotNumber);

    alert("Selected Parking Slot: " + slotNumber);

    window.location.href = "booking.html";
}


// ================= LOGOUT =================
function logout() {

    localStorage.removeItem("user");
    localStorage.removeItem("selectedSlot");

    window.location.href = "login.html";
}


// ================= PAGE LOAD =================
document.addEventListener("DOMContentLoaded", function () {

    if (document.getElementById("parkingSlots")) {
        loadParkingSlots();
    }

});