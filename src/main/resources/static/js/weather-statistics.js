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

document.addEventListener('DOMContentLoaded', () => {
    const token = localStorage.getItem('accessToken');

    const titleSpans = document.querySelectorAll('#animated-title span');
    const authBox = document.getElementById('auth-box');
    const logoutTileBtn = document.getElementById('logout-tile-btn');

    const logoutFormOld = document.getElementById('logout-form');
    if (logoutFormOld) {
        logoutFormOld.remove();
    }

    const adviceForm = document.getElementById('advice-form');
    const saveCityForm = document.getElementById('save-city-form');

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
    } else {
        authBox.innerHTML = `
                <a href="/login" class="chip">
                    <span class="chip-ico">🔐</span>
                    <span>Войти</span>
                </a>
            `;

        adviceForm.addEventListener('submit', (e) => {
            e.preventDefault();
            openAuthModal();
        });

        saveCityForm.addEventListener('submit', (e) => {
            e.preventDefault();
            openAuthModal();
        });

        logoutTileBtn.addEventListener('click', () => {
            window.location.href = '/login';
        });
    }

    closeModalBtn.addEventListener('click', closeAuthModal);
    authModal.addEventListener('click', (e) => {
        if (e.target === authModal) {
            closeAuthModal();
        }
    });
});