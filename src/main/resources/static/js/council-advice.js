const toast = document.getElementById('toast');
const toastTitle = document.getElementById('toast-title');
const toastText = document.getElementById('toast-text');

const showToast = (title, text, ok = true) => {
    toastTitle.textContent = title;
    toastText.textContent = text;
    toast.classList.remove('show');
    toast.classList.remove('bad');
    if (!ok) toast.classList.add('bad');
    requestAnimationFrame(() => toast.classList.add('show'));
    clearTimeout(window.__toastT);
    window.__toastT = setTimeout(() => toast.classList.remove('show'), 2600);
};

async function performLogout() {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'same-origin'
        });
    } catch (error) {
        console.error('Ошибка при выходе:', error);
    } finally {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        sessionStorage.removeItem('exampleAdvice');
        sessionStorage.removeItem('exampleShortClothingDescription');
        sessionStorage.removeItem('exampleImagePrompt');
        window.location.href = '/login';
    }
}

$(document).ready(function() {
    const token = localStorage.getItem('accessToken');
    const authBox = document.getElementById('auth-box');

    const authModal = document.getElementById('auth-modal');
    const closeModalBtn = document.getElementById('auth-modal-close');

    const openAuthModal = () => authModal.classList.add('show');
    const closeAuthModal = () => authModal.classList.remove('show');

    const titleSpans = document.querySelectorAll('#animated-title span');
    titleSpans.forEach((span, idx) => {
        span.style.opacity = '0';
        span.style.transition = 'opacity 0.4s ease';
        setTimeout(() => span.style.opacity = '1', idx * 70);
    });

    closeModalBtn.addEventListener('click', closeAuthModal);
    authModal.addEventListener('click', (e) => {
        if (e.target === authModal) {
            closeAuthModal();
        }
    });

    if (token) {
        authBox.innerHTML = `
                <form id="logout-form" class="logout-form">
                    <button type="submit" class="chip danger">
                        <span class="chip-ico">🚪</span>
                        <span>Выйти</span>
                    </button>
                </form>
            `;

        $("#logout-form").on("submit", async function(e) {
            e.preventDefault();
            await performLogout();
        });

        $("#logout-tile-btn").on("click", async function() {
            await performLogout();
        });
    } else {
        authBox.innerHTML = `
                <a href="/login" class="chip">
                    <span class="chip-ico">🔐</span>
                    <span>Войти</span>
                </a>
            `;

        $("#logout-tile-btn").on("click", function() {
            window.location.href = '/login';
        });
    }

    $("#save-advice-btn").click(function() {
        if (!token) {
            openAuthModal();
            return;
        }

        const adviceText = $(".advice-text").text();

        $.ajax({
            url: "/api/advice/save",
            type: "POST",
            headers: { "Authorization": "Bearer " + token },
            data: { adviceText: adviceText },
            success: function(response) {
                showToast("Сохранено ✅", String(response || "Совет добавлен"));
            },
            error: function(xhr) {
                showToast("Ошибка ❌", String(xhr.responseText || "Не удалось сохранить"), false);
            }
        });
    });

    $("#show-example-btn").click(function() {
        if (!token) {
            openAuthModal();
            return;
        }

        sessionStorage.setItem('exampleAdvice', adviceTextValue);
        sessionStorage.setItem('exampleShortClothingDescription', shortClothingDescriptionValue);
        sessionStorage.setItem('exampleImagePrompt', imagePromptValue);
        window.location.href = '/example-clothes';
    });
});