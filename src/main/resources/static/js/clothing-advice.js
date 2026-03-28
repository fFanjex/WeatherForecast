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
    const authBox = document.getElementById('auth-box');

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
            await performLogout();
        });
    } else {
        authBox.innerHTML = `
                <a href="/login" class="chip">
                    <span class="chip-ico">🔐</span>
                    <span>Войти</span>
                </a>
            `;
    }
});