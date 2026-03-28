document.addEventListener('DOMContentLoaded', () => {
    const titleSpans = document.querySelectorAll('#animated-title span');
    titleSpans.forEach((span, idx) => {
        span.style.opacity = '0';
        span.style.transition = 'opacity 0.4s ease';
        setTimeout(() => span.style.opacity = '1', idx * 70);
    });

    const input = document.getElementById('city-input');
    const clearBtn = document.getElementById('clear-btn');
    const hint = document.getElementById('input-hint');
    const authBox = document.getElementById('auth-box');
    const savedCityBtn = document.getElementById('saved-city-btn');
    const authModal = document.getElementById('auth-modal');
    const closeModalBtn = document.getElementById('close-modal-btn');

    const token = localStorage.getItem('accessToken');

    const sync = () => {
        const has = input.value.trim().length > 0;
        clearBtn.style.display = has ? 'grid' : 'none';
        hint.style.display = has ? 'block' : 'none';
    };

    const openAuthModal = () => authModal.classList.add('show');
    const closeAuthModal = () => authModal.classList.remove('show');

    if (token) {
        authBox.innerHTML = `
                <form id="logout-form" class="logout-form">
                    <button type="submit" class="chip danger">
                        <span class="chip-ico">🚪</span>
                        <span>Выйти</span>
                    </button>
                </form>
            `;

        document.getElementById('logout-form').addEventListener('submit', async (e) => {
            e.preventDefault();

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
                window.location.href = '/login';
            }
        });

        savedCityBtn.addEventListener('click', () => {
            window.location.href = '/weather/saved/data';
        });
    } else {
        authBox.innerHTML = `
                <a href="/login" class="chip">
                    <span class="chip-ico">🔐</span>
                    <span>Войти</span>
                </a>
            `;

        savedCityBtn.addEventListener('click', openAuthModal);
    }

    input.addEventListener('input', sync);
    clearBtn.addEventListener('click', () => {
        input.value = '';
        sync();
        input.focus();
    });

    closeModalBtn.addEventListener('click', closeAuthModal);
    authModal.addEventListener('click', (e) => {
        if (e.target === authModal) {
            closeAuthModal();
        }
    });

    sync();
});