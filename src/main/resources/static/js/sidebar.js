/* =========================================================
   RODRON BANK — SIDEBAR JAVASCRIPT
   ========================================================= */

document.addEventListener("DOMContentLoaded", function () {

    const toggle =
        document.querySelector(
            ".sidebar-toggle-btn"
        );

    const sidebar =
        document.querySelector(
            ".sidebar"
        );

    const backdrop =
        document.querySelector(
            ".sidebar-backdrop"
        );


    /* =====================================================
       CHECK ELEMENTS
    ===================================================== */

    if (!toggle || !sidebar) {

        console.warn(
            "Sidebar elements not found."
        );

        return;
    }



    /* =====================================================
       OPEN SIDEBAR
    ===================================================== */

    function openSidebar() {

        sidebar.classList.add(
            "sidebar-open"
        );


        if (backdrop) {

            backdrop.classList.add(
                "sidebar-backdrop-open"
            );

        }


        toggle.setAttribute(
            "aria-expanded",
            "true"
        );


        toggle.setAttribute(
            "aria-label",
            "Close navigation menu"
        );


        document.body.classList.add(
            "sidebar-is-open"
        );

    }



    /* =====================================================
       CLOSE SIDEBAR
    ===================================================== */

    function closeSidebar() {

        sidebar.classList.remove(
            "sidebar-open"
        );


        if (backdrop) {

            backdrop.classList.remove(
                "sidebar-backdrop-open"
            );

        }


        toggle.setAttribute(
            "aria-expanded",
            "false"
        );


        toggle.setAttribute(
            "aria-label",
            "Open navigation menu"
        );


        document.body.classList.remove(
            "sidebar-is-open"
        );

    }



    /* =====================================================
       HAMBURGER
    ===================================================== */

    toggle.addEventListener(
        "click",
        function (event) {

            event.preventDefault();

            event.stopPropagation();


            if (
                sidebar.classList.contains(
                    "sidebar-open"
                )
            ) {

                closeSidebar();

            }

            else {

                openSidebar();

            }

        }
    );



    /* =====================================================
       BACKDROP
    ===================================================== */

    if (backdrop) {

        backdrop.addEventListener(
            "click",
            function () {

                closeSidebar();

            }
        );

    }



    /* =====================================================
       MENU LINKS
    ===================================================== */

    const sidebarLinks =
        sidebar.querySelectorAll(
            ".sidebar-link"
        );


    sidebarLinks.forEach(
        function (link) {

            link.addEventListener(
                "click",
                function () {

                    if (
                        window.innerWidth <= 900
                    ) {

                        closeSidebar();

                    }

                }
            );

        }
    );



    /* =====================================================
       ESC KEY
    ===================================================== */

    document.addEventListener(
        "keydown",
        function (event) {

            if (
                event.key === "Escape" ||
                event.key === "Esc"
            ) {

                closeSidebar();

            }

        }
    );



    /* =====================================================
       RESIZE
    ===================================================== */

    let resizeTimer;


    window.addEventListener(
        "resize",
        function () {

            clearTimeout(
                resizeTimer
            );


            resizeTimer = setTimeout(
                function () {

                    if (
                        window.innerWidth > 900
                    ) {

                        closeSidebar();

                    }

                },
                100
            );

        }
    );



    /* =====================================================
       INITIAL STATE
    ===================================================== */

    toggle.setAttribute(
        "aria-expanded",
        "false"
    );


    toggle.setAttribute(
        "aria-label",
        "Open navigation menu"
    );

});