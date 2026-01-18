// Translations
const translations = {
    ru: {
        'title': 'Flagent - Feature Management Platform | Open Source Feature Flags',
        'nav.features': 'Возможности',
        'nav.api': 'API',
        'nav.docs': 'Документация',
        'nav.community': 'Сообщество',
        'nav.get-started': 'Начать',
        'hero.title1': 'Feature Management Platform',
        'hero.title2': 'Open Source',
        'hero.subtitle': 'Flagent - это open source сервис на Kotlin/Ktor для управления feature flags, A/B тестирования и динамической конфигурации. Безопасные релизы, эксперименты и контроль функциональности для современных команд.',
        'hero.github': 'Star on GitHub',
        'hero.docs': 'Документация',
        'hero.stats.open-source': '100%',
        'hero.stats.open-source-label': 'Open Source',
        'hero.stats.stack': 'Современный стек',
        'hero.stats.license': 'Лицензия',
        'features.title': 'Возможности',
        'features.subtitle': 'Flagent предоставляет полный набор возможностей для управления feature flags, A/B тестирования и динамической конфигурации.',
        'features.flags.title': 'Feature Flags',
        'features.flags.desc': 'Управление feature flags с возможностью мгновенного включения/отключения функциональности',
        'features.ab.title': 'A/B Testing',
        'features.ab.desc': 'Проведение экспериментов и A/B тестов с сегментацией пользователей и распределением вариантов',
        'features.config.title': 'Dynamic Configuration',
        'features.config.desc': 'Динамическая конфигурация без перезапуска приложения через API',
        'features.targeting.title': 'Targeting & Segmentation',
        'features.targeting.desc': 'Тонкая сегментация пользователей с правилами на основе контекста и ограничений',
        'features.databases.title': 'Multiple Databases',
        'features.databases.desc': 'Поддержка PostgreSQL, MySQL, SQLite для гибкости развертывания',
        'features.auth.title': 'Authentication',
        'features.auth.desc': 'JWT, Basic Auth, Header, Cookie аутентификация для безопасности',
        'features.recording.title': 'Data Recording',
        'features.recording.desc': 'Экспорт данных о evaluations в Kafka, AWS Kinesis, Google Pub/Sub',
        'features.ui.title': 'Modern UI',
        'features.ui.desc': 'Современный веб-интерфейс на Compose for Web для управления флагами',
        'api.title': 'API Documentation',
        'api.subtitle': 'Flagent предоставляет REST API для управления флагами и их оценки. Доступна интерактивная Swagger документация.',
        'api.swagger.title': 'Swagger UI',
        'api.swagger.desc': 'Интерактивная документация API доступна по адресу /docs когда сервер запущен',
        'api.swagger.link': 'Open Swagger UI →',
        'api.swagger.note': '(требуется запущенный сервер на localhost:18000)',
        'api.openapi.title': 'OpenAPI Specification',
        'api.openapi.desc': 'OpenAPI 3.0 спецификация доступна в YAML и JSON форматах',
        'api.openapi.yaml': 'OpenAPI YAML',
        'api.openapi.server': 'Server YAML',
        'api.endpoints.title': 'API Endpoints',
        'api.endpoints.desc': 'Полная документация всех endpoints доступна в разделе API',
        'api.endpoints.link': 'View Endpoints →',
        'quickstart.title': 'Quick Start',
        'quickstart.subtitle': 'Запустите Flagent за 5 минут с помощью Docker',
        'quickstart.default.title': 'Default Configuration',
        'quickstart.default.port': 'Port: 18000',
        'quickstart.default.db': 'Database: SQLite (in-memory)',
        'quickstart.default.ui': 'UI available at: http://localhost:18000',
        'quickstart.default.api': 'API at: http://localhost:18000/api/v1',
        'quickstart.more': 'Learn More',
        'sdks.title': 'Client SDKs',
        'sdks.subtitle': 'Flagent предоставляет официальные SDK для различных платформ',
        'sdks.kotlin.desc': 'Официальный Kotlin SDK для JVM приложений',
        'sdks.kotlin.link': 'View SDK →',
        'sdks.js.desc': 'Node.js и браузерный SDK на TypeScript',
        'sdks.js.link': 'View SDK →',
        'sdks.swift.desc': 'Native Swift SDK для iOS и macOS приложений',
        'sdks.swift.link': 'View SDK →',
        'community.title': 'Join the Community',
        'community.subtitle': 'Flagent - это open source проект. Вклад, вопросы и обратная связь приветствуются!',
        'community.github': 'View on GitHub',
        'community.docs': 'Documentation',
        'community.issues': 'Report Issues',
        'community.discussions': 'Discussions',
        'community.contributing': 'Contributing',
        'footer.description': 'Open source feature flagging, A/B testing, and dynamic configuration microservice в Kotlin/Ktor.',
        'footer.product': 'Продукт',
        'footer.docs': 'Документация',
        'footer.features': 'Возможности',
        'footer.deployment': 'Развертывание',
        'footer.api': 'API Reference',
        'footer.resources': 'Ресурсы',
        'footer.issues': 'Issues',
        'footer.contributing': 'Contributing',
        'footer.openapi': 'OpenAPI Spec',
        'footer.learn': 'Узнать больше',
        'footer.overview': 'Обзор',
        'footer.architecture': 'Архитектура',
        'footer.examples': 'Примеры',
        'footer.roadmap': 'Roadmap',
        'footer.license': 'Open source под Apache 2.0'
    },
    en: {
        'title': 'Flagent - Feature Management Platform | Open Source Feature Flags',
        'nav.features': 'Features',
        'nav.api': 'API',
        'nav.docs': 'Docs',
        'nav.community': 'Community',
        'nav.get-started': 'Get Started',
        'hero.title1': 'Feature Management Platform',
        'hero.title2': 'Open Source',
        'hero.subtitle': 'Flagent is an open source Kotlin/Ktor service for feature flagging, A/B testing, and dynamic configuration. Safe releases, experiments, and feature control for modern teams.',
        'hero.github': 'Star on GitHub',
        'hero.docs': 'View Docs',
        'hero.stats.open-source': '100%',
        'hero.stats.open-source-label': 'Open Source',
        'hero.stats.stack': 'Modern Stack',
        'hero.stats.license': 'License',
        'features.title': 'Features',
        'features.subtitle': 'Flagent provides a complete set of features for feature flag management, A/B testing, and dynamic configuration.',
        'features.flags.title': 'Feature Flags',
        'features.flags.desc': 'Feature flag management with instant enable/disable functionality',
        'features.ab.title': 'A/B Testing',
        'features.ab.desc': 'Run experiments and A/B tests with user segmentation and variant distribution',
        'features.config.title': 'Dynamic Configuration',
        'features.config.desc': 'Dynamic configuration without application restart via API',
        'features.targeting.title': 'Targeting & Segmentation',
        'features.targeting.desc': 'Fine-grained user segmentation with rules based on context and constraints',
        'features.databases.title': 'Multiple Databases',
        'features.databases.desc': 'PostgreSQL, MySQL, SQLite support for deployment flexibility',
        'features.auth.title': 'Authentication',
        'features.auth.desc': 'JWT, Basic Auth, Header, Cookie authentication for security',
        'features.recording.title': 'Data Recording',
        'features.recording.desc': 'Export evaluation data to Kafka, AWS Kinesis, Google Pub/Sub',
        'features.ui.title': 'Modern UI',
        'features.ui.desc': 'Modern web interface built with Compose for Web for flag management',
        'api.title': 'API Documentation',
        'api.subtitle': 'Flagent provides REST API for flag management and evaluation. Interactive Swagger documentation is available.',
        'api.swagger.title': 'Swagger UI',
        'api.swagger.desc': 'Interactive API documentation available at /docs when server is running',
        'api.swagger.link': 'Open Swagger UI →',
        'api.swagger.note': '(requires running server on localhost:18000)',
        'api.openapi.title': 'OpenAPI Specification',
        'api.openapi.desc': 'OpenAPI 3.0 specification available in YAML and JSON formats',
        'api.openapi.yaml': 'OpenAPI YAML',
        'api.openapi.server': 'Server YAML',
        'api.endpoints.title': 'API Endpoints',
        'api.endpoints.desc': 'Complete documentation of all endpoints available in API section',
        'api.endpoints.link': 'View Endpoints →',
        'quickstart.title': 'Quick Start',
        'quickstart.subtitle': 'Get Flagent running in 5 minutes with Docker',
        'quickstart.default.title': 'Default Configuration',
        'quickstart.default.port': 'Port: 18000',
        'quickstart.default.db': 'Database: SQLite (in-memory)',
        'quickstart.default.ui': 'UI available at: http://localhost:18000',
        'quickstart.default.api': 'API at: http://localhost:18000/api/v1',
        'quickstart.more': 'Learn More',
        'sdks.title': 'Client SDKs',
        'sdks.subtitle': 'Flagent provides official SDKs for various platforms',
        'sdks.kotlin.desc': 'Official Kotlin SDK for JVM applications',
        'sdks.kotlin.link': 'View SDK →',
        'sdks.js.desc': 'Node.js and browser SDK in TypeScript',
        'sdks.js.link': 'View SDK →',
        'sdks.swift.desc': 'Native Swift SDK for iOS and macOS applications',
        'sdks.swift.link': 'View SDK →',
        'community.title': 'Join the Community',
        'community.subtitle': 'Flagent is an open source project. Contributions, questions, and feedback are welcome!',
        'community.github': 'View on GitHub',
        'community.docs': 'Documentation',
        'community.issues': 'Report Issues',
        'community.discussions': 'Discussions',
        'community.contributing': 'Contributing',
        'footer.description': 'Open source feature flagging, A/B testing, and dynamic configuration microservice in Kotlin/Ktor.',
        'footer.product': 'Product',
        'footer.docs': 'Documentation',
        'footer.features': 'Features',
        'footer.deployment': 'Deployment',
        'footer.api': 'API Reference',
        'footer.resources': 'Resources',
        'footer.issues': 'Issues',
        'footer.contributing': 'Contributing',
        'footer.openapi': 'OpenAPI Spec',
        'footer.learn': 'Learn More',
        'footer.overview': 'Overview',
        'footer.architecture': 'Architecture',
        'footer.examples': 'Examples',
        'footer.roadmap': 'Roadmap',
        'footer.license': 'Open source under Apache 2.0'
    }
};

// Get current language from localStorage or default to 'ru'
let currentLang = localStorage.getItem('flagent-lang') || 'ru';

// Function to change language
function changeLanguage(lang) {
    currentLang = lang;
    localStorage.setItem('flagent-lang', lang);
    document.documentElement.lang = lang;
    
    // Update all elements with data-i18n attribute
    document.querySelectorAll('[data-i18n]').forEach(element => {
        const key = element.getAttribute('data-i18n');
        if (translations[lang] && translations[lang][key]) {
            element.textContent = translations[lang][key];
        }
    });
    
    // Update title
    const titleKey = document.querySelector('title').getAttribute('data-i18n');
    if (titleKey && translations[lang] && translations[lang][titleKey]) {
        document.title = translations[lang][titleKey];
    }
    
    // Update language buttons
    document.querySelectorAll('.lang-btn').forEach(btn => {
        if (btn.getAttribute('data-lang') === lang) {
            btn.classList.add('active');
        } else {
            btn.classList.remove('active');
        }
    });
}

// Initialize language on page load
document.addEventListener('DOMContentLoaded', function() {
    // Set initial language
    changeLanguage(currentLang);
    
    // Add click handlers to language buttons
    document.querySelectorAll('.lang-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const lang = this.getAttribute('data-lang');
            changeLanguage(lang);
        });
    });
    
    // Smooth scroll for anchor links
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            const href = this.getAttribute('href');
            if (href !== '#' && href !== '') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                }
            }
        });
    });

    // Navbar background on scroll
    let lastScroll = 0;
    const navbar = document.querySelector('.navbar');
    
    window.addEventListener('scroll', () => {
        const currentScroll = window.pageYOffset;
        
        if (currentScroll > 50) {
            navbar.style.background = 'rgba(255, 255, 255, 0.98)';
            navbar.style.boxShadow = '0 4px 6px -1px rgba(0, 0, 0, 0.1)';
        } else {
            navbar.style.background = 'rgba(255, 255, 255, 0.95)';
            navbar.style.boxShadow = 'none';
        }
        
        lastScroll = currentScroll;
    });

    // Add fade-in animation to cards on scroll
    const cardObserver = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting) {
                setTimeout(() => {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }, index * 100);
                cardObserver.unobserve(entry.target);
            }
        });
    }, {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    });

    // Observe cards
    document.querySelectorAll('.feature-card, .api-card, .sdk-card').forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        cardObserver.observe(card);
    });
});
