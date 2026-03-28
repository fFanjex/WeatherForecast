const form = document.getElementById('login-form');
const msg = document.getElementById('login-error');

const togglePass = document.getElementById('togglePass');
const passInput = document.getElementById('password');

togglePass.addEventListener('click', () => {
    const isPassword = passInput.type === 'password';
    passInput.type = isPassword ? 'text' : 'password';
    togglePass.textContent = isPassword ? 'Скрыть' : 'Показать';
});

function validateInput(input) {
    if (!input.checkValidity()) {
        input.classList.add('error');
        return false;
    }
    input.classList.remove('error');
    return true;
}

form.addEventListener('input', (e) => {
    if (e.target.matches('input')) {
        validateInput(e.target);
    }
    msg.textContent = '';
    msg.className = 'msg';
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const inputs = Array.from(form.querySelectorAll('input'));
    let valid = true;

    inputs.forEach(input => {
        if (!validateInput(input)) {
            valid = false;
        }
    });

    if (!valid) {
        msg.className = 'msg error';
        msg.textContent = 'Пожалуйста, заполните все поля правильно.';
        return;
    }

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                email: document.getElementById('email').value.trim(),
                password: document.getElementById('password').value
            })
        });

        if (!response.ok) {
            throw new Error('Неверный email или пароль');
        }

        const data = await response.json();

        localStorage.setItem('accessToken', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        window.location.href = '/home';
    } catch (err) {
        msg.className = 'msg error';
        msg.textContent = err.message || 'Ошибка входа';
    }
});