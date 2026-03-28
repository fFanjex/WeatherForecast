const form = document.getElementById('register-form');
const msg = document.getElementById('register-msg');
const togglePass = document.getElementById('togglePass');
const passInput = document.getElementById('password');

togglePass.addEventListener('click', () => {
    const isPassword = passInput.type === 'password';
    passInput.type = isPassword ? 'text' : 'password';
    togglePass.textContent = isPassword ? 'Скрыть' : 'Показать';
});

function validateInput(input) {
    input.setCustomValidity('');

    if (!input.checkValidity()) {
        input.classList.add('error');
        return false;
    }
    input.classList.remove('error');
    return true;
}

form.addEventListener('input', (e) => {
    const el = e.target;
    if (el.matches('input, select')) validateInput(el);
    msg.textContent = '';
    msg.className = 'msg';
});

form.addEventListener('submit', async (e) => {
    e.preventDefault();

    const fields = Array.from(form.querySelectorAll('input, select'));
    let valid = true;

    fields.forEach(f => {
        if (!validateInput(f)) valid = false;
    });

    if (!valid) {
        msg.className = 'msg error';
        msg.textContent = 'Пожалуйста, заполните все поля правильно.';
        return;
    }

    const body = {
        username: form.username.value.trim(),
        email: form.email.value.trim(),
        password: form.password.value.trim(),
        sex: form.sex.value
    };

    msg.className = 'msg info';
    msg.textContent = 'Отправка...';

    try {
        const res = await fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.ok) {
            msg.className = 'msg success';
            msg.textContent = 'Успешно! Перенаправление...';
            setTimeout(() => window.location.href = '/login', 1200);
        } else {
            const text = await res.text();
            throw new Error(text || 'Не удалось зарегистрироваться');
        }
    } catch (err) {
        msg.className = 'msg error';
        msg.textContent = 'Ошибка: ' + err.message;
    }
});