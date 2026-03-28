const adviceText = sessionStorage.getItem('exampleAdvice') || '';
const shortClothingDescription = sessionStorage.getItem('exampleShortClothingDescription') || '';

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

document.addEventListener('DOMContentLoaded', async () => {
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
        setTimeout(() => {
            span.style.opacity = '1';
        }, idx * 70);
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

    document.getElementById('advice-text').textContent =
        adviceText || 'Совет по одежде не найден';

    document.getElementById('short-clothes-text').textContent =
        shortClothingDescription || 'Список вещей не найден';

    const sliderStage = document.getElementById('slider-stage');
    const dotsContainer = document.getElementById('slider-dots');
    const prevBtn = document.getElementById('prev-slide');
    const nextBtn = document.getElementById('next-slide');
    const reloadBtn = document.getElementById('reload-images');
    const galleryStatus = document.getElementById('gallery-status');
    const backBtnTop = document.getElementById('back-btn-top');
    const backBtnBottom = document.getElementById('back-btn-bottom');

    let slides = [];
    let dots = [];
    let currentIndex = 0;

    function refreshCollections() {
        slides = Array.from(document.querySelectorAll('.slide'));
        dots = Array.from(document.querySelectorAll('.dot'));
    }

    function showSlide(index) {
        if (!slides.length || !dots.length) return;

        slides.forEach(slide => slide.classList.remove('active'));
        dots.forEach(dot => dot.classList.remove('active'));

        slides[index].classList.add('active');
        dots[index].classList.add('active');
        currentIndex = index;
    }

    function renderError(message) {
        sliderStage.innerHTML = `
        <div class="slide active">
          <div class="placeholder">
            <div class="placeholder-icon">❌</div>
            <div class="placeholder-title">Ошибка</div>
            <div class="placeholder-text">${message}</div>
          </div>
        </div>
      `;
        dotsContainer.innerHTML = '';
        galleryStatus.textContent = '';
        refreshCollections();
    }

    function renderEmpty() {
        sliderStage.innerHTML = `
        <div class="slide active">
          <div class="placeholder">
            <div class="placeholder-icon">⚠️</div>
            <div class="placeholder-title">Нет изображений</div>
            <div class="placeholder-text">Сервис не вернул ни одной картинки</div>
          </div>
        </div>
      `;
        dotsContainer.innerHTML = '';
        galleryStatus.textContent = '';
        refreshCollections();
    }

    function escapeHtml(value) {
        return String(value ?? '')
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;')
            .replaceAll('"', '&quot;')
            .replaceAll("'", '&#39;');
    }

    function renderSlides(items) {
        if (!items || items.length === 0) {
            renderEmpty();
            return;
        }

        sliderStage.innerHTML = '';
        dotsContainer.innerHTML = '';

        items.forEach((item, index) => {
            const clothingItem = escapeHtml(item.clothingItem || 'Без названия');
            const image = item.image || '';

            const slide = document.createElement('div');
            slide.className = 'slide' + (index === 0 ? ' active' : '');

            if (image) {
                slide.innerHTML = `
            <div class="generated-card">
              <div class="generated-image-wrap">
                <img class="generated-image" src="${image}" alt="${clothingItem}">
              </div>
              <div class="generated-info">
                <div class="generated-title">${clothingItem}</div>
              </div>
            </div>
          `;
            } else {
                slide.innerHTML = `
            <div class="placeholder">
              <div class="placeholder-icon">⚠️</div>
              <div class="placeholder-title">${clothingItem}</div>
              <div class="placeholder-text">Для этого элемента изображение не было получено.</div>
            </div>
          `;
            }

            sliderStage.appendChild(slide);

            const dot = document.createElement('button');
            dot.className = 'dot' + (index === 0 ? ' active' : '');
            dot.type = 'button';
            dot.dataset.index = String(index);
            dot.setAttribute('aria-label', 'Слайд ' + (index + 1));
            dot.addEventListener('click', () => showSlide(index));
            dotsContainer.appendChild(dot);
        });

        galleryStatus.textContent = `Сгенерировано изображений: ${items.length}`;
        refreshCollections();
        currentIndex = 0;
    }

    async function loadImages() {
        try {
            if (!shortClothingDescription || !shortClothingDescription.trim()) {
                renderError('Не передан список одежды для генерации.');
                return;
            }

            if (!token) {
                openAuthModal();
                return;
            }

            galleryStatus.textContent = 'Идёт генерация изображений...';

            sliderStage.innerHTML = `
          <div class="slide active">
            <div class="placeholder">
              <div class="placeholder-icon">⏳</div>
              <div class="placeholder-title">Загрузка</div>
              <div class="placeholder-text">Генерируем изображения одежды...</div>
            </div>
          </div>
        `;
            dotsContainer.innerHTML = '';
            refreshCollections();

            const response = await fetch('/api/advice/generate-images', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    shortClothingDescription: shortClothingDescription
                })
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(`Ошибка ${response.status}: ${text}`);
            }

            const data = await response.json();
            renderSlides(data.items || []);
        } catch (e) {
            renderError(e.message || 'Не удалось загрузить изображения');
        }
    }

    prevBtn.addEventListener('click', () => {
        if (!slides.length) return;
        const newIndex = currentIndex === 0 ? slides.length - 1 : currentIndex - 1;
        showSlide(newIndex);
    });

    nextBtn.addEventListener('click', () => {
        if (!slides.length) return;
        const newIndex = currentIndex === slides.length - 1 ? 0 : currentIndex + 1;
        showSlide(newIndex);
    });

    reloadBtn.addEventListener('click', () => {
        if (!token) {
            openAuthModal();
            return;
        }
        loadImages();
    });

    backBtnTop.addEventListener('click', () => {
        window.location.href = '/home';
    });

    backBtnBottom.addEventListener('click', () => {
        window.location.href = '/home';
    });

    await loadImages();
});