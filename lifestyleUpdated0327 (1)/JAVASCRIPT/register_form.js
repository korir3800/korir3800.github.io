document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("registration-form");
    
    // For the Success message
    const successMessage = document.createElement("p");
    successMessage.classList.add("success-message");
    successMessage.style.display = "none"; 
    form.parentNode.appendChild(successMessage); 

    // to Input elements
    const nameInput = document.getElementById("name");
    const emailInput = document.getElementById("email");
    const passwordInput = document.getElementById("password");
    const confirmPasswordInput = document.getElementById("confirm-password");
    const phoneInput = document.getElementById("phone");
    const passwordToggle = document.getElementById("toggle-password");

    // to Show error message function
    function showError(input, message) {
        let errorSpan = input.parentNode.querySelector(".error-message");

        if (!errorSpan) {
            errorSpan = document.createElement("span");
            errorSpan.classList.add("error-message");
            input.parentNode.appendChild(errorSpan);
        }

        errorSpan.textContent = message;
    }

    function clearError(input) {
        let errorSpan = input.parentNode.querySelector(".error-message");
        if (errorSpan) {
            errorSpan.textContent = "";
        }
    }

    // this is to Reset form function
    function resetForm() {
        form.reset();
        successMessage.style.display = "none"; 

        // To Clear all error messages
        document.querySelectorAll(".error-message").forEach(error => error.innerText = "");

        // for Reseting password field type
        passwordInput.type = "password";
        confirmPasswordInput.type = "password";
        passwordToggle.innerText = "Show";
    }

    // the Form to submit event listener
    form.addEventListener("submit", function (event) {
        event.preventDefault();
        let isValid = true;

        // to Check for empty fields and display a required message
        if (nameInput.value.trim() === "") {
            showError(nameInput, "This field is required");
            isValid = false;
        } else {
            clearError(nameInput);
        }

        if (emailInput.value.trim() === "") {
            showError(emailInput, "This field is required");
            isValid = false;
        } else {
            const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
            if (!emailPattern.test(emailInput.value)) {
                showError(emailInput, "Enter a valid email");
                isValid = false;
            } else {
                clearError(emailInput);
            }
        }

        if (passwordInput.value.trim() === "") {
            showError(passwordInput, "This field is required");
            isValid = false;
        } else if (passwordInput.value.length < 8) {
            showError(passwordInput, "Password must be at least 8 characters long");
            isValid = false;
        } else {
            clearError(passwordInput);
        }

        if (confirmPasswordInput.value.trim() === "") {
            showError(confirmPasswordInput, "This field is required");
            isValid = false;
        } else if (confirmPasswordInput.value !== passwordInput.value) {
            showError(confirmPasswordInput, "Passwords do not match");
            isValid = false;
        } else {
            clearError(confirmPasswordInput);
        }

        if (phoneInput.value.trim() === "") {
            showError(phoneInput, "This field is required");
            isValid = false;
        } else {
            const phonePattern = /^\d{3}-\d{3}-\d{4}$/;
            if (!phonePattern.test(phoneInput.value)) {
                showError(phoneInput, "Phone number must be in 123-456-7890 format");
                isValid = false;
            } else {
                clearError(phoneInput);
            }
        }

        if (isValid) {
            successMessage.textContent = "Registration Successful!";
            successMessage.style.display = "block"; 
            setTimeout(() => {
                successMessage.style.transition = "opacity 1s ease, transform 1s ease"; 
                successMessage.style.opacity = "1"; 
                successMessage.style.transform = "scale(1)"; 
            }, 10); // Small delay to ensure display is applied before animation
            form.reset();
        } else {
            // If not valid, hide the success message with animation
            successMessage.style.transition = "opacity 1s ease, transform 1s ease"; 
            successMessage.style.opacity = "0";
            successMessage.style.transform = "scale(0.5)"; 
            setTimeout(() => {
                successMessage.style.display = "none"; // 
            }, 1000); // Delay to match the duration of the animation
        }
    });

    // For Toggle password visibility
    passwordToggle.addEventListener("click", function () {
        if (passwordInput.type === "password") {
            passwordInput.type = "text";
            confirmPasswordInput.type = "text";
            passwordToggle.textContent = "Hide";
        } else {
            passwordInput.type = "password";
            confirmPasswordInput.type = "password";
            passwordToggle.textContent = "Show";
        }
    });

    // to Auto-format phone number
    phoneInput.addEventListener("input", function () {
        let numbers = phoneInput.value.replace(/\D/g, ""); 
        let formattedNumber = "";

        if (numbers.length > 0) {
            formattedNumber += numbers.substring(0, 3);
        }
        if (numbers.length > 3) {
            formattedNumber += "-" + numbers.substring(3, 6);
        }
        if (numbers.length > 6) {
            formattedNumber += "-" + numbers.substring(6, 10);
        }

        phoneInput.value = formattedNumber;
    });

    // to Attach reset function to reset button
    const resetButton = document.querySelector('button[type="reset"]');
    if (resetButton) {
        resetButton.addEventListener("click", function () {
            resetForm();
        });
    }
});