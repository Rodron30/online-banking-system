document.addEventListener("DOMContentLoaded", function () {

    const bellBtn =
        document.getElementById("notifBellBtn");

    const dropdown =
        document.getElementById("notifDropdown");

    const badge =
        document.getElementById("notifBadge");

    const list =
        document.getElementById("notifList");

    const markAllBtn =
        document.getElementById("notifMarkAllBtn");


    if (!bellBtn || !dropdown) {
        console.warn("Notification elements not found.");
        return;
    }


    /* =====================================================
       CSRF
    ===================================================== */

    const csrfMeta =
        document.querySelector('meta[name="_csrf"]');

    const csrfHeaderMeta =
        document.querySelector('meta[name="_csrf_header"]');


    function postHeaders() {

        const headers = {};

        if (
            csrfMeta &&
            csrfHeaderMeta
        ) {

            headers[
                csrfHeaderMeta.content
            ] = csrfMeta.content;

        }

        return headers;
    }


    /* =====================================================
       ICONS
    ===================================================== */

    const typeIcons = {

        DEPOSIT: "⬆",

        WITHDRAWAL: "⬇",

        TRANSFER_SENT: "↗",

        TRANSFER_RECEIVED: "↙",

        PIN_CHANGED: "🔒",

        ACCOUNT_FROZEN: "❄",

        ACCOUNT_ACTIVATED: "✓",

        ACCOUNT_DELETED: "×"

    };


    /* =====================================================
       OPEN
    ===================================================== */

    function openNotifications() {

        dropdown.hidden = false;

        requestAnimationFrame(function () {

            dropdown.classList.add(
                "notif-dropdown-open"
            );

        });

        bellBtn.setAttribute(
            "aria-expanded",
            "true"
        );

        loadNotifications();
    }


    /* =====================================================
       CLOSE
    ===================================================== */

    function closeNotifications() {

        dropdown.classList.remove(
            "notif-dropdown-open"
        );

        dropdown.hidden = true;

        bellBtn.setAttribute(
            "aria-expanded",
            "false"
        );

    }


    /* =====================================================
       TOGGLE
    ===================================================== */

    function toggleNotifications(event) {

        event.preventDefault();

        event.stopPropagation();


        if (
            dropdown.hidden ||
            !dropdown.classList.contains(
                "notif-dropdown-open"
            )
        ) {

            openNotifications();

        } else {

            closeNotifications();

        }

    }


    /* =====================================================
       ESCAPE
    ===================================================== */

    function handleEscape(event) {

        if (event.key === "Escape") {

            closeNotifications();

        }

    }


    /* =====================================================
       UNREAD COUNT
    ===================================================== */

    function refreshUnreadCount() {

        fetch(
            "/api/notifications/unread-count",
            {
                credentials: "same-origin"
            }
        )

        .then(function (response) {

            if (!response.ok) {
                return { count: 0 };
            }

            return response.json();

        })

        .then(function (data) {

            const count =
                Number(data.count || 0);


            if (count > 0) {

                badge.hidden = false;

                badge.textContent =
                    count > 9
                        ? "9+"
                        : String(count);

            } else {

                badge.hidden = true;

            }

        })

        .catch(function () {

            badge.hidden = true;

        });

    }


    /* =====================================================
       ESCAPE HTML
    ===================================================== */

    function escapeHtml(value) {

        const div =
            document.createElement("div");

        div.textContent =
            value == null
                ? ""
                : String(value);

        return div.innerHTML;

    }


    /* =====================================================
       LOAD NOTIFICATIONS
    ===================================================== */

    function loadNotifications() {

        if (!list) {
            return;
        }


        list.innerHTML =
            '<div class="notif-empty">Loading...</div>';


        fetch(
            "/api/notifications",
            {
                credentials: "same-origin"
            }
        )

        .then(function (response) {

            if (!response.ok) {
                return [];
            }

            return response.json();

        })

        .then(function (items) {

            if (
                !items ||
                items.length === 0
            ) {

                list.innerHTML =
                    '<div class="notif-empty">' +
                    'No notifications yet.' +
                    '</div>';

                return;

            }


            list.innerHTML =
                items.map(function (notification) {

                    const icon =
                        typeIcons[
                            notification.type
                        ] || "🔔";


                    const unreadClass =
                        notification.isRead
                            ? ""
                            : " notif-item-unread";


                    return (

                        '<div class="notif-item' +
                        unreadClass +
                        '" data-id="' +
                        escapeHtml(notification.id) +
                        '">' +

                            '<span class="notif-item-icon">' +
                                icon +
                            '</span>' +

                            '<div class="notif-item-body">' +

                                '<p class="notif-item-message">' +
                                    escapeHtml(
                                        notification.message
                                    ) +
                                '</p>' +

                                '<p class="notif-item-time">' +
                                    escapeHtml(
                                        notification.createdAt
                                    ) +
                                '</p>' +

                            '</div>' +

                        '</div>'

                    );

                }).join("");

        })

        .catch(function () {

            list.innerHTML =
                '<div class="notif-empty">' +
                'Unable to load notifications.' +
                '</div>';

        });

    }


    /* =====================================================
       BELL CLICK
    ===================================================== */

    bellBtn.addEventListener(
        "click",
        toggleNotifications
    );


    /* =====================================================
       NOTIFICATION ITEM CLICK
    ===================================================== */

    if (list) {

        list.addEventListener(
            "click",
            function (event) {

                event.stopPropagation();


                const item =
                    event.target.closest(
                        ".notif-item"
                    );


                if (!item) {
                    return;
                }


                const id =
                    item.getAttribute(
                        "data-id"
                    );


                if (
                    !item.classList.contains(
                        "notif-item-unread"
                    )
                ) {

                    return;

                }


                item.classList.remove(
                    "notif-item-unread"
                );


                fetch(
                    "/api/notifications/" +
                    id +
                    "/read",
                    {
                        method: "POST",
                        credentials: "same-origin",
                        headers: postHeaders()
                    }
                )

                .then(function () {

                    refreshUnreadCount();

                })

                .catch(function () {});

            }
        );

    }


    /* =====================================================
       MARK ALL AS READ
    ===================================================== */

    if (markAllBtn) {

        markAllBtn.addEventListener(
            "click",
            function (event) {

                event.preventDefault();

                event.stopPropagation();


                fetch(
                    "/api/notifications/read-all",
                    {
                        method: "POST",
                        credentials: "same-origin",
                        headers: postHeaders()
                    }
                )

                .then(function () {

                    refreshUnreadCount();

                    loadNotifications();

                })

                .catch(function () {});

            }
        );

    }


    /* =====================================================
       CLICK OUTSIDE
    ===================================================== */

    document.addEventListener(
        "click",
        function (event) {

            if (
                !dropdown.contains(event.target) &&
                !bellBtn.contains(event.target)
            ) {

                closeNotifications();

            }

        }
    );


    /* =====================================================
       ESC
    ===================================================== */

    document.addEventListener(
        "keydown",
        handleEscape
    );


    /* =====================================================
       RESIZE
    ===================================================== */

    window.addEventListener(
        "resize",
        function () {

            closeNotifications();

        }
    );


    /* =====================================================
       INITIAL
    ===================================================== */

    closeNotifications();

    refreshUnreadCount();


    /* =====================================================
       AUTO REFRESH
    ===================================================== */

    setInterval(
        refreshUnreadCount,
        30000
    );

});