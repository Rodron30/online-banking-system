/* =========================================================
   RODRON BANK — DASHBOARD JAVASCRIPT
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    /* =====================================================
       ATM CARD DETAILS
    ===================================================== */

    const cardToggleBtn =
        document.getElementById("cardToggleBtn");

    const masked =
        document.getElementById("cardNumberMasked");

    const full =
        document.getElementById("cardNumberFull");

    const unavailable =
        document.getElementById("cardNumberUnavailable");

    const label =
        document.getElementById("cardToggleLabel");

    const icon =
        document.getElementById("cardToggleIcon");


    if (cardToggleBtn) {

        cardToggleBtn.addEventListener("click", function () {

            if (!full || !masked) {

                console.warn(
                    "Account number is unavailable."
                );

                return;
            }

            const isHidden =
                full.hasAttribute("hidden");


            if (isHidden) {

                /* SHOW */

                full.removeAttribute("hidden");

                masked.setAttribute(
                    "hidden",
                    ""
                );

                if (unavailable) {

                    unavailable.setAttribute(
                        "hidden",
                        ""
                    );

                }

                if (label) {

                    label.textContent =
                        "Hide Card Details";

                }

                if (icon) {

                    icon.innerHTML = `
                        <path d="
                            M3 3l18 18
                            M10.6 10.6
                            a2 2 0 0 0 2.8 2.8
                        "/>

                        <path d="
                            M9.9 5.1
                            A11 11 0 0 1 12 5
                            c6.5 0 10 7 10 7
                            a18 18 0 0 1-3.1 3.8
                            M6.1 6.1
                            C3.5 8 2 12 2 12
                            s3.5 7 10 7
                            c1 0 2-.2 2.9-.5
                        "/>
                    `;

                }

            } else {

                /* HIDE */

                full.setAttribute(
                    "hidden",
                    ""
                );

                masked.removeAttribute(
                    "hidden"
                );

                if (label) {

                    label.textContent =
                        "Show Card Details";

                }

                if (icon) {

                    icon.innerHTML = `
                        <path d="
                            M2 12
                            s3.5-6 10-6
                            10 6 10 6
                            -3.5 6-10 6
                            S2 12 2 12Z
                        "/>

                        <circle
                            cx="12"
                            cy="12"
                            r="2.5"/>
                    `;

                }

            }

        });

    }


    /* =====================================================
       ACCOUNT DETAILS
    ===================================================== */

    const accountDetailsToggle =
        document.getElementById(
            "accountDetailsToggle"
        );

    const accountDetailsPanel =
        document.getElementById(
            "accountDetailsPanel"
        );

    const accountDetailsCaret =
        document.getElementById(
            "accountDetailsCaret"
        );


    if (
        accountDetailsToggle &&
        accountDetailsPanel &&
        accountDetailsCaret
    ) {

        accountDetailsToggle.addEventListener(
            "click",
            function (event) {

                event.preventDefault();

                const isClosed =
                    accountDetailsPanel.hasAttribute(
                        "hidden"
                    );


                if (isClosed) {

                    accountDetailsPanel.removeAttribute(
                        "hidden"
                    );

                    accountDetailsToggle.setAttribute(
                        "aria-expanded",
                        "true"
                    );

                    accountDetailsCaret.innerHTML = `
                        <path d="m6 9 6 6 6-6"/>
                    `;

                } else {

                    accountDetailsPanel.setAttribute(
                        "hidden",
                        ""
                    );

                    accountDetailsToggle.setAttribute(
                        "aria-expanded",
                        "false"
                    );

                    accountDetailsCaret.innerHTML = `
                        <path d="m9 6 6 6-6 6"/>
                    `;

                }

            }
        );

    }


    /* =====================================================
       COPY ACCOUNT NUMBER
    ===================================================== */

    const copyAccountBtn =
        document.getElementById(
            "copyAccountBtn"
        );


    if (copyAccountBtn) {

        copyAccountBtn.addEventListener(
            "click",
            async function () {

                const element =
                    document.getElementById(
                        "cardNumberFull"
                    );


                if (!element) {

                    console.warn(
                        "Account number is unavailable."
                    );

                    return;

                }


                const rawValue =
                    element.textContent
                        .trim()
                        .replace(/\s/g, "");


                if (!/^\d+$/.test(rawValue)) {

                    console.warn(
                        "Invalid account number."
                    );

                    return;

                }


                try {

                    if (
                        navigator.clipboard &&
                        window.isSecureContext
                    ) {

                        await navigator.clipboard.writeText(
                            rawValue
                        );

                        showCopyFeedback();

                    } else {

                        fallbackCopy(rawValue);

                    }

                } catch (error) {

                    console.warn(
                        "Clipboard access failed.",
                        error
                    );

                    fallbackCopy(rawValue);

                }

            }
        );

    }


    /* =====================================================
       COPY FEEDBACK
    ===================================================== */

    function showCopyFeedback() {

        const button =
            document.getElementById(
                "copyAccountBtn"
            );


        if (!button) {
            return;
        }


        const originalHTML =
            button.innerHTML;


        button.innerHTML = `
            <svg viewBox="0 0 24 24" aria-hidden="true">
                <path d="M5 12l4 4L19 6"/>
            </svg>
        `;


        button.setAttribute(
            "aria-label",
            "Account number copied"
        );


        setTimeout(function () {

            button.innerHTML =
                originalHTML;

            button.setAttribute(
                "aria-label",
                "Copy account number"
            );

        }, 1500);

    }


    /* =====================================================
       FALLBACK COPY
    ===================================================== */

    function fallbackCopy(value) {

        const textarea =
            document.createElement(
                "textarea"
            );


        textarea.value = value;

        textarea.setAttribute(
            "readonly",
            ""
        );

        textarea.style.position =
            "fixed";

        textarea.style.opacity =
            "0";


        document.body.appendChild(
            textarea
        );


        textarea.select();


        try {

            document.execCommand(
                "copy"
            );

            showCopyFeedback();

        } catch (error) {

            console.warn(
                "Unable to copy account number.",
                error
            );

        }


        document.body.removeChild(
            textarea
        );

    }


    /* =====================================================
       MOBILE SIDEBAR / HAMBURGER
       IMPORTANT:
       Button ID = sidebarToggleBtn
       Sidebar ID = appSidebar
       Backdrop ID = sidebarBackdrop
    ===================================================== */

    const sidebar =
        document.getElementById(
            "appSidebar"
        );

    const sidebarBackdrop =
        document.getElementById(
            "sidebarBackdrop"
        );

    const sidebarToggle =
        document.getElementById(
            "sidebarToggleBtn"
        );


    function openSidebar() {

        if (!sidebar) {
            return;
        }


        sidebar.classList.add(
            "open"
        );


        if (sidebarBackdrop) {

            sidebarBackdrop.classList.add(
                "open"
            );

        }


        if (sidebarToggle) {

            sidebarToggle.setAttribute(
                "aria-expanded",
                "true"
            );

            sidebarToggle.setAttribute(
                "aria-label",
                "Close navigation"
            );

        }


        document.body.classList.add(
            "sidebar-open"
        );

    }


    function closeSidebar() {

        if (!sidebar) {
            return;
        }


        sidebar.classList.remove(
            "open"
        );


        if (sidebarBackdrop) {

            sidebarBackdrop.classList.remove(
                "open"
            );

        }


        if (sidebarToggle) {

            sidebarToggle.setAttribute(
                "aria-expanded",
                "false"
            );

            sidebarToggle.setAttribute(
                "aria-label",
                "Open navigation"
            );

        }


        document.body.classList.remove(
            "sidebar-open"
        );

    }


    /* =====================================================
       HAMBURGER CLICK
    ===================================================== */

    if (sidebarToggle) {

        sidebarToggle.addEventListener(
            "click",
            function (event) {

                event.preventDefault();

                event.stopPropagation();


                if (
                    sidebar &&
                    sidebar.classList.contains(
                        "open"
                    )
                ) {

                    closeSidebar();

                } else {

                    openSidebar();

                }

            }
        );

    }


    /* =====================================================
       BACKDROP CLICK
    ===================================================== */

    if (sidebarBackdrop) {

        sidebarBackdrop.addEventListener(
            "click",
            function () {

                closeSidebar();

            }
        );

    }


    /* =====================================================
       CLOSE SIDEBAR AFTER NAVIGATION
    ===================================================== */

    if (sidebar) {

        const navigationItems =
            sidebar.querySelectorAll(
                "a.sidebar-link, .sidebar-brand, .sidebar-logout-btn"
            );


        navigationItems.forEach(
            function (item) {

                item.addEventListener(
                    "click",
                    function () {

                        closeSidebar();

                    }
                );

            }
        );

    }


    /* =====================================================
       ESCAPE KEY
    ===================================================== */

    document.addEventListener(
        "keydown",
        function (event) {

            if (event.key === "Escape") {

                closeSidebar();

            }

        }
    );


    /* =====================================================
       CLOSE SIDEBAR WHEN RETURNING TO DESKTOP
    ===================================================== */

    window.addEventListener(
        "resize",
        function () {

            if (window.innerWidth > 900) {

                closeSidebar();

            }

        }
    );

});

