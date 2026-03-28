const token = localStorage.getItem('accessToken');

const mapStatus = document.getElementById('map-status');
const cityEl = document.getElementById('city-name');
const userIdEl = document.getElementById('user-id');
const authBox = document.getElementById('auth-box');

const defaultLon = 37.618423;
const defaultLat = 55.751244;

const marker = new ol.Feature({
    geometry: new ol.geom.Point(ol.proj.fromLonLat([defaultLon, defaultLat]))
});

marker.setStyle(new ol.style.Style({
    image: new ol.style.Circle({
        radius: 7,
        fill: new ol.style.Fill({ color: '#4F7CFF' }),
        stroke: new ol.style.Stroke({ color: '#ffffff', width: 2 })
    })
}));

const vectorLayer = new ol.layer.Vector({
    source: new ol.source.Vector({ features: [marker] })
});

const map = new ol.Map({
    target: 'home-map',
    layers: [
        new ol.layer.Tile({ source: new ol.source.OSM() }),
        vectorLayer
    ],
    view: new ol.View({
        center: ol.proj.fromLonLat([defaultLon, defaultLat]),
        zoom: 10
    }),
    controls: []
});

const setPoint = (lat, lon, zoom = 13) => {
    const coords = ol.proj.fromLonLat([lon, lat]);
    marker.getGeometry().setCoordinates(coords);
    map.getView().animate({ center: coords, zoom, duration: 600 });
};

function setupAuthModal() {
    const authModal = document.getElementById('auth-modal');
    const closeBtn = document.getElementById('auth-modal-close');
    const factBtn = document.getElementById('fact-btn');
    const clothesBtn = document.getElementById('clothes-btn');

    const openAuthModal = () => authModal.classList.add('show');
    const closeAuthModal = () => authModal.classList.remove('show');

    closeBtn?.addEventListener('click', closeAuthModal);

    authModal.addEventListener('click', (e) => {
        if (e.target === authModal) {
            closeAuthModal();
        }
    });
    if (!token) {
        factBtn?.addEventListener('click', (e) => {
            e.preventDefault();
            openAuthModal();
        });

        clothesBtn?.addEventListener('click', (e) => {
            e.preventDefault();
            openAuthModal();
        });
    } else {
        factBtn?.addEventListener('click', () => {
            window.location.href = '/interesting-fact';
        });

        clothesBtn?.addEventListener('click', () => {
            window.location.href = '/clothing-advice';
        });
    }

    return { openAuthModal };
}

async function reverseGeocode(lat, lon) {
    const url = `https://nominatim.openstreetmap.org/reverse?format=json&lat=${encodeURIComponent(lat)}&lon=${encodeURIComponent(lon)}&zoom=10&addressdetails=1`;

    const response = await fetch(url, {
        headers: {
            "Accept": "application/json"
        }
    });

    if (!response.ok) {
        throw new Error('Ошибка reverse geocoding');
    }

    const data = await response.json();
    const address = data.address || {};

    const city =
        address.city ||
        address.town ||
        address.village ||
        address.hamlet ||
        address.municipality ||
        address.county ||
        'Неизвестный город';

    return {
        city,
        displayName: data.display_name || city
    };
}

function getCurrentPosition() {
    return new Promise((resolve, reject) => {
        if (!navigator.geolocation) {
            reject(new Error('Geolocation не поддерживается браузером'));
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => resolve(position),
            (error) => reject(error),
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0
            }
        );
    });
}

async function loadProfile() {
    const response = await fetch('/api/home', {
        method: 'GET',
        headers: token ? {
            'Authorization': 'Bearer ' + token,
            'Content-Type': 'application/json'
        } : {
            'Content-Type': 'application/json'
        }
    });

    if (!response.ok) {
        throw new Error('Ошибка загрузки профиля');
    }

    return await response.json();
}

function renderAuthBox() {
    if (token) {
        authBox.innerHTML = `
                <form id="logout" novalidate>
                    <button class="logout-btn" type="submit" aria-label="Выйти">
                        <span class="logout-ico" aria-hidden="true">
                            <svg viewBox="0 0 24 24" fill="none">
                                <path d="M10 7V6a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-6a2 2 0 0 1-2-2v-1"
                                      stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                                <path d="M14 12H3m0 0 3-3M3 12l3 3"
                                      stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                        </span>
                        <span>Выход</span>
                    </button>
                </form>
            `;

        document.getElementById('logout').addEventListener('submit', async function (e) {
            e.preventDefault();

            try {
                await fetch('/api/auth/logout', {
                    method: 'POST'
                });
            } catch (e) {
                console.error(e);
            }

            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            window.location.href = '/login';
        });
    } else {
        authBox.innerHTML = `
                <a href="/login" class="logout-btn" style="text-decoration:none;">
                    <span class="logout-ico" aria-hidden="true">🔐</span>
                    <span>Войти</span>
                </a>
            `;
    }
}

async function initPage() {
    try {
        const data = await loadProfile();

        userIdEl.textContent = data.username || 'Гость';
        cityEl.classList.remove('skeleton');
        cityEl.textContent = 'Определяем…';
        mapStatus.textContent = 'Определяем местоположение по GPS…';
        try {
            const position = await getCurrentPosition();

            const lat = position.coords.latitude;
            const lon = position.coords.longitude;

            setPoint(lat, lon, 13);
            mapStatus.textContent = 'Координаты получены по GPS';

            try {
                const geo = await reverseGeocode(lat, lon);
                cityEl.textContent = geo.city;
                mapStatus.textContent = `Ваше местоположение: ${geo.city}`;
            } catch (geoError) {
                console.error(geoError);
                cityEl.textContent = `Широта: ${lat.toFixed(4)}, Долгота: ${lon.toFixed(4)}`;
                mapStatus.textContent = 'GPS определён, но город не удалось распознать';
            }
        } catch (gpsError) {
            console.error(gpsError);
            cityEl.textContent = 'Геолокация недоступна';
            mapStatus.textContent = 'Нет доступа к геолокации — показана Москва';
            setPoint(defaultLat, defaultLon, 10);
        }
    } catch (e) {
        console.error(e);
        userIdEl.textContent = token ? 'Ошибка' : 'Гость';
        cityEl.classList.remove('skeleton');
        cityEl.textContent = 'Ошибка загрузки';
        mapStatus.textContent = 'Ошибка — показана Москва';
        setPoint(defaultLat, defaultLon, 10);
    }
}

renderAuthBox();
setupAuthModal();
initPage();