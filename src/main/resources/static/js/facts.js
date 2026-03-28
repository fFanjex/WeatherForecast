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

document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem('accessToken');

    const titleSpans = document.querySelectorAll('#animated-title span');
    const authBox = document.getElementById('auth-box');
    const logoutTileBtn = document.getElementById('logout-tile-btn');
    const clothingLinkBtn = document.getElementById('clothing-link-btn');

    const authModal = document.getElementById('auth-modal');
    const closeModalBtn = document.getElementById('auth-modal-close');

    const openAuthModal = () => authModal.classList.add('show');
    const closeAuthModal = () => authModal.classList.remove('show');

    titleSpans.forEach((span, idx) => {
        span.style.opacity = '0';
        span.style.transition = 'opacity 0.4s ease';
        setTimeout(() => span.style.opacity = '1', idx * 70);
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

        const logoutForm = document.getElementById('logout-form');

        logoutForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            await performLogout();
        });

        logoutTileBtn.addEventListener('click', async () => {
            await performLogout();
        });

        clothingLinkBtn.addEventListener('click', () => {
            window.location.href = '/clothing-advice';
        });
    } else {
        authBox.innerHTML = `
                <a href="/login" class="chip">
                    <span class="chip-ico">🔐</span>
                    <span>Войти</span>
                </a>
            `;

        logoutTileBtn.addEventListener('click', () => {
            window.location.href = '/login';
        });

        clothingLinkBtn.addEventListener('click', openAuthModal);
    }

    closeModalBtn.addEventListener('click', closeAuthModal);
    authModal.addEventListener('click', (e) => {
        if (e.target === authModal) {
            closeAuthModal();
        }
    });

    const factItems = document.querySelectorAll(".fact");
    factItems.forEach((item, index) => {
        item.style.opacity = 0;
        item.style.transform = "translateY(10px)";
        setTimeout(() => {
            item.style.transition = "all 0.6s ease";
            item.style.opacity = 1;
            item.style.transform = "translateY(0)";
        }, index * 90);
    });

    const links = document.querySelectorAll(".page-btn");
    links.forEach(link => {
        link.addEventListener("click", (e) => {
            if (link.classList.contains("disabled")) return;
            e.preventDefault();
            document.body.style.transition = "opacity 0.35s ease";
            document.body.style.opacity = "0";
            setTimeout(() => window.location.href = link.href, 350);
        });
    });
});