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
        sessionStorage.removeItem('examplePlacesAdvice');

        window.location.href = '/login';
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    const token = localStorage.getItem('accessToken');

    const authBox = document.getElementById('auth-box');
    const authModal = document.getElementById('auth-modal');
    const closeModalBtn = document.getElementById('auth-modal-close');

    const placesGrid = document.getElementById('places-grid');
    const placesStatus = document.getElementById('places-status');

    const cityText = document.getElementById('city-text');
    const weatherText = document.getElementById('weather-text');
    const tempText = document.getElementById('temp-text');

    const titleSpans = document.querySelectorAll('#animated-title span');

    const openAuthModal = () => {
        if (authModal) authModal.classList.add('show');
    };

    const closeAuthModal = () => {
        if (authModal) authModal.classList.remove('show');
    };

    titleSpans.forEach((span, idx) => {
        span.style.opacity = '0';
        span.style.transition = 'opacity 0.4s ease';

        setTimeout(() => {
            span.style.opacity = '1';
        }, idx * 70);
    });

    if (closeModalBtn) {
        closeModalBtn.addEventListener('click', closeAuthModal);
    }

    if (authModal) {
        authModal.addEventListener('click', (e) => {
            if (e.target === authModal) closeAuthModal();
        });
    }

    if (token) {
        authBox.innerHTML = `
            <form id="logout-form">
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

        openAuthModal();
        return;
    }

    function getWeatherData() {
        const params = new URLSearchParams(window.location.search);

        return {
            city: params.get('city') || '',
            country: params.get('country') || '',
            temperature: params.get('temperature') || '',
            humidity: params.get('humidity') || '',
            windSpeed: params.get('windSpeed') || '',
            weatherDescription: params.get('weatherDescription') || ''
        };
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function renderLoading() {
        placesGrid.innerHTML = `
            <div class="placeholder">
                <div class="placeholder-icon">⏳</div>
                <div class="placeholder-title">Загрузка</div>
                <div class="placeholder-text">Ищем лучшие места с учётом погоды...</div>
            </div>
        `;

        if (placesStatus) {
            placesStatus.textContent = 'Подбираем рекомендации...';
        }
    }

    function renderError(message) {
        placesGrid.innerHTML = `
            <div class="placeholder">
                <div class="placeholder-icon">❌</div>
                <div class="placeholder-title">Ошибка</div>
                <div class="placeholder-text">${escapeHtml(message)}</div>
            </div>
        `;

        if (placesStatus) {
            placesStatus.textContent = '';
        }
    }

    function renderEmpty() {
        placesGrid.innerHTML = `
            <div class="placeholder">
                <div class="placeholder-icon">⚠️</div>
                <div class="placeholder-title">Нет рекомендаций</div>
                <div class="placeholder-text">Сервис не вернул места для прогулки.</div>
            </div>
        `;

        if (placesStatus) {
            placesStatus.textContent = '';
        }
    }

    function renderPlaces(items) {
        if (!items || items.length === 0) {
            renderEmpty();
            return;
        }

        const weather = getWeatherData();
        const city = weather.city || '';
        const country = weather.country || '';

        placesGrid.innerHTML = '';

        items.slice(0, 5).forEach((place, index) => {
            const rawName = place.name || 'Место без названия';

            const name = escapeHtml(rawName);
            const description = escapeHtml(place.description || 'Описание пока недоступно.');
            const reason = escapeHtml(place.reason || 'Это место может подойти для прогулки при текущей погоде.');

            const mapQuery = encodeURIComponent(`${rawName} ${city} ${country}`);
            const mapUrl = `https://yandex.ru/maps/?text=${mapQuery}`;

            const card = document.createElement('article');
            card.className = 'place-card no-image';

            card.innerHTML = `
                <div class="place-number-static">${index + 1}</div>

                <div class="place-content">
                    <h2 class="place-title">${name}</h2>

                    <p class="place-description">${description}</p>

                    <div class="reason">
                        <div class="reason-title">Почему стоит пойти именно сейчас</div>
                        <p>${reason}</p>
                    </div>

                    <a href="${mapUrl}" target="_blank" rel="noopener noreferrer" class="map-btn">
                        <span>📍</span>
                        <span>Открыть на карте</span>
                    </a>
                </div>
            `;

            placesGrid.appendChild(card);
        });

        if (placesStatus) {
            placesStatus.textContent = `Найдено мест: ${Math.min(items.length, 5)}`;
        }
    }

    async function loadPlaces() {
        try {
            const weather = getWeatherData();

            if (!weather.city.trim()) {
                renderError('Город не передан. Вернитесь назад и выберите город заново.');
                return;
            }

            cityText.textContent = weather.city || 'городе';
            weatherText.textContent = weather.weatherDescription || 'погода не указана';
            tempText.textContent = weather.temperature || '--';

            renderLoading();

            const response = await fetch('/api/places/advice', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(weather)
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(`Ошибка ${response.status}: ${text || 'не удалось загрузить рекомендации'}`);
            }

            const data = await response.json();

            sessionStorage.setItem('examplePlacesAdvice', JSON.stringify(data));

            renderPlaces(data.places || []);
        } catch (error) {
            renderError(error.message || 'Не удалось загрузить места для прогулки.');
        }
    }

    await loadPlaces();
});