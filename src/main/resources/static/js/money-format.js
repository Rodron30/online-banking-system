/* =========================================================
   RODRON BANK — MONEY FORMAT
   Automatic comma formatting for:
   Deposit
   Withdraw
   Transfer
   ========================================================= */

document.addEventListener("DOMContentLoaded", () => {

    const moneyInputs = document.querySelectorAll(".money-input");

    moneyInputs.forEach((input) => {

        /* -------------------------------------------------
           FORMAT WHILE TYPING
           Example:
           2        → 2
           25       → 25
           250      → 250
           2500     → 2,500
           25000    → 25,000
           250000   → 250,000
           ------------------------------------------------- */

        input.addEventListener("input", () => {

            let value = input.value;

            // Remove commas and anything except numbers
            value = value.replace(/,/g, "");
            value = value.replace(/[^\d]/g, "");

            // Prevent empty/invalid values
            if (value === "") {
                input.value = "";
                return;
            }

            // Remove unnecessary leading zeroes
            value = value.replace(/^0+(?=\d)/, "");

            // Add commas automatically
            input.value = Number(value).toLocaleString("en-US");
        });


        /* -------------------------------------------------
           FORMAT EXISTING VALUE
           ------------------------------------------------- */

        if (input.value) {

            let value = input.value
                .replace(/,/g, "")
                .replace(/[^\d]/g, "");

            if (value !== "") {
                input.value = Number(value).toLocaleString("en-US");
            }
        }
    });


    /* -----------------------------------------------------
       REMOVE COMMAS BEFORE SUBMIT
       
       Backend should receive:
       25000

       NOT:
       25,000
       ----------------------------------------------------- */

    document.querySelectorAll("form").forEach((form) => {

        form.addEventListener("submit", () => {

            form.querySelectorAll(".money-input").forEach((input) => {

                input.value = input.value.replace(/,/g, "");

            });

        });

    });

});