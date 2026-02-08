// Translations
const translations = {
    ru: {
        'title': 'Flagent - Feature Management Platform | Open Source Feature Flags',
        'nav.home': 'Главная',
        'nav.features': 'Возможности',
        'nav.quickstart': 'Quick Start',
        'nav.api': 'API',
        'nav.docs': 'Документация',
        'nav.community': 'Сообщество',
        'nav.get-started': 'Начать',
        'hero.badge': 'Первая Kotlin-Native платформа для feature flags',
        'hero.title1': 'Feature Management Platform',
        'hero.title2': 'Open Source',
        'hero.tagline': 'Типобезопасные feature flags, A/B тесты и динамическая конфигурация. Релизьте безопасно, экспериментируйте уверенно.',
        'hero.cta.get-started': 'Начать →',
        'hero.docs': 'Документация',
        'hero.subtitle': 'Flagent - это open source сервис на Kotlin/Ktor для управления feature flags, A/B тестирования и динамической конфигурации. Безопасные релизы, эксперименты и контроль функциональности для современных команд.',
        'hero.github': 'Star on GitHub',
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
        'sdks.kotlin.link': 'View All SDKs →',
        'sdks.js.desc': 'Node.js и браузерный SDK на TypeScript',
        'sdks.js.link': 'View All SDKs →',
        'sdks.swift.desc': 'Native Swift SDK для iOS и macOS приложений',
        'sdks.swift.link': 'View All SDKs →',
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
        'footer.license': 'Open source под Apache 2.0',
        'footer.home': 'Главная',
        'footer.quickstart': 'Quick Start',
        'footer.sdk': 'SDKs',
        'footer.swagger': 'Swagger UI',
        'why.title': 'Why Flagent?',
        'why.subtitle': 'The first Kotlin-native feature flag platform. Built for type safety, performance, and modern teams.',
        'why.kotlin.title': 'Kotlin-Native',
        'why.kotlin.desc': 'Type-safe SDKs, coroutine-first API, Ktor ecosystem. No more stringly-typed flags.',
        'why.performance.title': 'High Performance',
        'why.performance.desc': 'Sub-millisecond evaluation, EvalCache, 10k+ req/s. Client-side evaluation in Kotlin & Go Enhanced.',
        'why.enterprise.title': 'Production-Ready',
        'why.enterprise.desc': 'PostgreSQL, MySQL, SQLite. Kafka/Kinesis/PubSub. Docker, Admin UI, 70+ test files.',
        'cta.title': 'Get started in 5 minutes',
        'cta.subtitle': 'Run Flagent with Docker. No database setup required. Start experimenting with feature flags today.',
        // SDK page
        'sdk.title': 'Flagent SDKs - Client Libraries',
        'sdk.header.title': 'Flagent SDK Clients',
        'sdk.header.subtitle': 'Client libraries (SDK) for Flagent API - feature flagging, A/B testing and dynamic configuration microservice',
        'overview.title': 'SDK Structure',
        'overview.desc': 'Flagent предоставляет несколько типов SDK для различных платформ и случаев использования. Все SDK генерируются из OpenAPI спецификации и совместимы с Flagent API версии 1.0.x.',
        'overview.base.title': 'Base SDK',
        'overview.base.desc': 'Генерируется автоматически из OpenAPI спецификации. Предоставляет низкоуровневый доступ к API с типизированными моделями.',
        'overview.enhanced.title': 'Enhanced SDK',
        'overview.enhanced.desc': 'Нативная реализация с кэшированием, удобным API и поддержкой offline режима.',
        'overview.debug.title': 'Debug UI',
        'overview.debug.desc': 'Опциональные библиотеки для визуальной отладки в процессе разработки.',
        'base.title': 'Base SDKs',
        'base.subtitle': 'Автоматически сгенерированные SDK из OpenAPI спецификации',
        'sdk.status.available': 'Available',
        'sdk.info.requires': 'Requires:',
        'sdk.info.platforms': 'Platforms:',
        'sdk.info.installation': 'Installation:',
        'sdk.link.docs': 'Documentation',
        'enhanced.title': 'Enhanced SDKs',
        'enhanced.subtitle': 'Улучшенные SDK с кэшированием, удобным API и поддержкой offline режима',
        'enhanced.cache.title': 'Caching Layer',
        'enhanced.cache.desc': 'Автоматическое кэширование evaluations для повышения производительности и работы в offline режиме',
        'enhanced.api.title': 'Convenient API',
        'enhanced.api.desc': 'Удобный высокоуровневый API с методами для работы с флагами и экспериментами',
        'enhanced.offline.title': 'Offline Support',
        'enhanced.offline.desc': 'Поддержка работы без интернета с автоматической синхронизацией при восстановлении связи',
        'enhanced.kotlin.desc': 'Улучшенная версия Kotlin SDK с кэшированием и удобным API',
        'enhanced.js.desc': 'Улучшенная версия JavaScript SDK с кэшированием и удобным API',
        'enhanced.swift.desc': 'Улучшенная версия Swift SDK с кэшированием и удобным API',
        'debug.title': 'Debug UI Libraries',
        'debug.subtitle': 'Опциональные библиотеки для визуальной отладки в процессе разработки',
        'debug.visual.title': 'Visual Debugging',
        'debug.visual.desc': 'Визуальный интерфейс для просмотра всех флагов, их состояния и evaluations',
        'debug.override.title': 'Local Overrides',
        'debug.override.desc': 'Возможность локально переопределять значения флагов для тестирования',
        'debug.logs.title': 'Evaluation Logs',
        'debug.logs.desc': 'Просмотр логов evaluations для отладки и понимания поведения флагов',
        'debug.kotlin.desc': 'Compose Debug UI для Android приложений',
        'debug.js.desc': 'React Debug UI для веб-приложений',
        'debug.swift.desc': 'SwiftUI Debug UI для iOS приложений',
        'compatibility.title': 'API Compatibility',
        'compatibility.desc': 'Все SDK генерируются из OpenAPI спецификации и совместимы с Flagent API версии 1.0.x. OpenAPI спецификация доступна по адресу http://localhost:18000/api/v1/openapi.yaml когда сервер запущен.',
        // Quick Start page
        'quickstart.title': 'Flagent Quick Start Guide',
        'quickstart.header.title': 'Quick Start Guide',
        'quickstart.header.subtitle': 'Запустите Flagent за 5 минут и начните использовать feature flags в вашем приложении',
        'docker.title': 'Option 1: Docker (Recommended)',
        'docker.desc': 'Самый быстрый способ запустить Flagent - использовать Docker образ. Не требует установки зависимостей или настройки базы данных.',
        'docker.step1.title': 'Step 1: Pull Docker Image',
        'docker.step2.title': 'Step 2: Run Container',
        'docker.step2.note': 'По умолчанию Flagent использует SQLite в памяти, что идеально для тестирования.',
        'docker.step3.title': 'Step 3: Open Flagent UI',
        'docker.step3.desc': 'Откройте браузер и перейдите на:',
        'docker.step4.title': 'Step 4: Default Credentials',
        'docker.step4.desc': 'Если аутентификация включена, используйте:',
        'docker.step4.username': 'Email:',
        'docker.step4.password': 'Password:',
        'docker.step5.title': 'Step 5: Verify Installation',
        'docker.step5.desc': 'Проверьте, что сервер работает:',
        'docker.step5.note': 'Должен вернуться JSON с информацией о состоянии сервера.',
        'compose.title': 'Option 2: Docker Compose',
        'compose.desc': 'Для полной настройки с PostgreSQL используйте Docker Compose. Это обеспечивает персистентность данных и готовность к production.',
        'compose.step1.title': 'Step 1: Clone Repository',
        'compose.step2.title': 'Step 2: Start Services',
        'compose.step2.note': 'Это запустит Flagent и PostgreSQL в отдельных контейнерах.',
        'compose.step3.title': 'Step 3: Access Services',
        'compose.step3.ui': 'Flagent UI:',
        'compose.step3.api': 'API:',
        'compose.step3.swagger': 'Swagger UI:',
        'source.title': 'Option 3: From Source',
        'source.desc': 'Соберите и запустите Flagent из исходного кода для разработки и кастомизации.',
        'source.step1.title': 'Step 1: Prerequisites',
        'source.step1.jdk': 'Для компиляции и запуска Kotlin кода',
        'source.step1.gradle': 'Система сборки проекта',
        'source.step1.db': 'PostgreSQL, MySQL или SQLite',
        'source.step2.title': 'Step 2: Clone Repository',
        'source.step3.title': 'Step 3: Build Project',
        'source.step3.note': 'Это соберет все модули и запустит тесты.',
        'source.step4.title': 'Step 4: Configure Environment',
        'source.step4.desc': 'Установите переменные окружения для подключения к базе данных:',
        'source.step4.sqlite': 'SQLite (for testing)',
        'source.step4.postgres': 'PostgreSQL',
        'source.step4.mysql': 'MySQL',
        'source.step5.title': 'Step 5: Run Backend',
        'source.step5.note': 'Сервер запустится на http://localhost:18000',
        'next.title': 'Next Steps',
        'next.desc': 'После успешного запуска Flagent вы можете:',
        'next.step1': 'Create Your First Flag',
        'next.step1.desc': 'Используйте UI или API для создания первого feature flag',
        'next.step1.link': 'Open UI',
        'next.step2': 'Try Evaluation API',
        'next.step2.desc': 'Протестируйте evaluation API через curl или Swagger UI',
        'next.step3': 'Connect SDK',
        'next.step3.desc': 'Интегрируйте Flagent SDK в ваше приложение',
        'next.step3.link': 'View SDKs',
        'next.step4': 'Explore Documentation',
        'next.step4.desc': 'Изучите полную документацию для продвинутых возможностей',
        'next.step4.link': 'View Docs',
        'troubleshooting.title': 'Troubleshooting',
        'troubleshooting.port.title': 'Port Already in Use',
        'troubleshooting.port.desc': 'Если порт 18000 уже занят, измените его через переменную окружения:',
        'troubleshooting.db.title': 'Database Connection Issues',
        'troubleshooting.db.desc': 'Убедитесь, что строка подключения к БД правильная и база данных доступна. Проверьте логи контейнера для деталей.',
        'troubleshooting.auth.title': 'Authentication Issues',
        'troubleshooting.auth.desc': 'Если включена аутентификация, используйте правильные credentials. По умолчанию: admin@local / admin. Проверьте настройки через переменные окружения.'
    },
    en: {
        'title': 'Flagent - Feature Management Platform | Open Source Feature Flags',
        'nav.home': 'Home',
        'nav.features': 'Features',
        'nav.quickstart': 'Quick Start',
        'nav.api': 'API',
        'nav.docs': 'Docs',
        'nav.community': 'Community',
        'nav.get-started': 'Get Started',
        'hero.badge': 'First Kotlin-Native Feature Flag Platform',
        'hero.title1': 'Feature Management Platform',
        'hero.title2': 'Open Source',
        'hero.tagline': 'Type-safe feature flags, A/B testing, and dynamic configuration. Ship safely, experiment confidently.',
        'hero.cta.get-started': 'Get Started →',
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
        'sdks.kotlin.link': 'View All SDKs →',
        'sdks.js.desc': 'Node.js and browser SDK in TypeScript',
        'sdks.js.link': 'View All SDKs →',
        'sdks.swift.desc': 'Native Swift SDK for iOS and macOS applications',
        'sdks.swift.link': 'View All SDKs →',
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
        'footer.license': 'Open source under Apache 2.0',
        'footer.home': 'Home',
        'footer.quickstart': 'Quick Start',
        'footer.sdk': 'SDKs',
        'footer.swagger': 'Swagger UI',
        'why.title': 'Why Flagent?',
        'why.subtitle': 'The first Kotlin-native feature flag platform. Built for type safety, performance, and modern teams.',
        'why.kotlin.title': 'Kotlin-Native',
        'why.kotlin.desc': 'Type-safe SDKs, coroutine-first API, Ktor ecosystem. No more stringly-typed flags.',
        'why.performance.title': 'High Performance',
        'why.performance.desc': 'Sub-millisecond evaluation, EvalCache, 10k+ req/s. Client-side evaluation in Kotlin & Go Enhanced.',
        'why.enterprise.title': 'Production-Ready',
        'why.enterprise.desc': 'PostgreSQL, MySQL, SQLite. Kafka/Kinesis/PubSub. Docker, Admin UI, 70+ test files.',
        'cta.title': 'Get started in 5 minutes',
        'cta.subtitle': 'Run Flagent with Docker. No database setup required. Start experimenting with feature flags today.',
        // SDK page
        'sdk.title': 'Flagent SDKs - Client Libraries',
        'sdk.header.title': 'Flagent SDK Clients',
        'sdk.header.subtitle': 'Client libraries (SDK) for Flagent API - feature flagging, A/B testing and dynamic configuration microservice',
        'overview.title': 'SDK Structure',
        'overview.desc': 'Flagent provides several types of SDKs for different platforms and use cases. All SDKs are generated from OpenAPI specification and are compatible with Flagent API version 1.0.x.',
        'overview.base.title': 'Base SDK',
        'overview.base.desc': 'Automatically generated from OpenAPI specification. Provides low-level API access with typed models.',
        'overview.enhanced.title': 'Enhanced SDK',
        'overview.enhanced.desc': 'Native implementation with caching, convenient API, and offline support.',
        'overview.debug.title': 'Debug UI',
        'overview.debug.desc': 'Optional libraries for visual debugging during development.',
        'base.title': 'Base SDKs',
        'base.subtitle': 'Automatically generated SDKs from OpenAPI specification',
        'sdk.status.available': 'Available',
        'sdk.info.requires': 'Requires:',
        'sdk.info.platforms': 'Platforms:',
        'sdk.info.installation': 'Installation:',
        'sdk.link.docs': 'Documentation',
        'enhanced.title': 'Enhanced SDKs',
        'enhanced.subtitle': 'Enhanced SDKs with caching, convenient API, and offline support',
        'enhanced.cache.title': 'Caching Layer',
        'enhanced.cache.desc': 'Automatic caching of evaluations for improved performance and offline operation',
        'enhanced.api.title': 'Convenient API',
        'enhanced.api.desc': 'Convenient high-level API with methods for working with flags and experiments',
        'enhanced.offline.title': 'Offline Support',
        'enhanced.offline.desc': 'Support for offline operation with automatic synchronization when connection is restored',
        'enhanced.kotlin.desc': 'Enhanced Kotlin SDK with caching and convenient API',
        'enhanced.js.desc': 'Enhanced JavaScript SDK with caching and convenient API',
        'enhanced.swift.desc': 'Enhanced Swift SDK with caching and convenient API',
        'debug.title': 'Debug UI Libraries',
        'debug.subtitle': 'Optional libraries for visual debugging during development',
        'debug.visual.title': 'Visual Debugging',
        'debug.visual.desc': 'Visual interface for viewing all flags, their state, and evaluations',
        'debug.override.title': 'Local Overrides',
        'debug.override.desc': 'Ability to locally override flag values for testing',
        'debug.logs.title': 'Evaluation Logs',
        'debug.logs.desc': 'View evaluation logs for debugging and understanding flag behavior',
        'debug.kotlin.desc': 'Compose Debug UI for Android applications',
        'debug.js.desc': 'React Debug UI for web applications',
        'debug.swift.desc': 'SwiftUI Debug UI for iOS applications',
        'compatibility.title': 'API Compatibility',
        'compatibility.desc': 'All SDKs are generated from OpenAPI specification and are compatible with Flagent API version 1.0.x. OpenAPI specification is available at http://localhost:18000/api/v1/openapi.yaml when server is running.',
        // Quick Start page
        'quickstart.title': 'Flagent Quick Start Guide',
        'quickstart.header.title': 'Quick Start Guide',
        'quickstart.header.subtitle': 'Get Flagent running in 5 minutes and start using feature flags in your application',
        'docker.title': 'Option 1: Docker (Recommended)',
        'docker.desc': 'The fastest way to run Flagent is using Docker image. No dependencies installation or database setup required.',
        'docker.step1.title': 'Step 1: Pull Docker Image',
        'docker.step2.title': 'Step 2: Run Container',
        'docker.step2.note': 'By default, Flagent uses in-memory SQLite, which is perfect for testing.',
        'docker.step3.title': 'Step 3: Open Flagent UI',
        'docker.step3.desc': 'Open your browser and navigate to:',
        'docker.step4.title': 'Step 4: Default Credentials',
        'docker.step4.desc': 'If authentication is enabled, use:',
        'docker.step4.username': 'Email:',
        'docker.step4.password': 'Password:',
        'docker.step5.title': 'Step 5: Verify Installation',
        'docker.step5.desc': 'Verify that the server is running:',
        'docker.step5.note': 'Should return JSON with server status information.',
        'compose.title': 'Option 2: Docker Compose',
        'compose.desc': 'For a complete setup with PostgreSQL, use Docker Compose. This provides data persistence and production readiness.',
        'compose.step1.title': 'Step 1: Clone Repository',
        'compose.step2.title': 'Step 2: Start Services',
        'compose.step2.note': 'This will start Flagent and PostgreSQL in separate containers.',
        'compose.step3.title': 'Step 3: Access Services',
        'compose.step3.ui': 'Flagent UI:',
        'compose.step3.api': 'API:',
        'compose.step3.swagger': 'Swagger UI:',
        'source.title': 'Option 3: From Source',
        'source.desc': 'Build and run Flagent from source code for development and customization.',
        'source.step1.title': 'Step 1: Prerequisites',
        'source.step1.jdk': 'For compiling and running Kotlin code',
        'source.step1.gradle': 'Project build system',
        'source.step1.db': 'PostgreSQL, MySQL, or SQLite',
        'source.step2.title': 'Step 2: Clone Repository',
        'source.step3.title': 'Step 3: Build Project',
        'source.step3.note': 'This will build all modules and run tests.',
        'source.step4.title': 'Step 4: Configure Environment',
        'source.step4.desc': 'Set environment variables for database connection:',
        'source.step4.sqlite': 'SQLite (for testing)',
        'source.step4.postgres': 'PostgreSQL',
        'source.step4.mysql': 'MySQL',
        'source.step5.title': 'Step 5: Run Backend',
        'source.step5.note': 'Server will start on http://localhost:18000',
        'next.title': 'Next Steps',
        'next.desc': 'After successfully starting Flagent, you can:',
        'next.step1': 'Create Your First Flag',
        'next.step1.desc': 'Use UI or API to create your first feature flag',
        'next.step1.link': 'Open UI',
        'next.step2': 'Try Evaluation API',
        'next.step2.desc': 'Test evaluation API via curl or Swagger UI',
        'next.step3': 'Connect SDK',
        'next.step3.desc': 'Integrate Flagent SDK into your application',
        'next.step3.link': 'View SDKs',
        'next.step4': 'Explore Documentation',
        'next.step4.desc': 'Explore full documentation for advanced features',
        'next.step4.link': 'View Docs',
        'troubleshooting.title': 'Troubleshooting',
        'troubleshooting.port.title': 'Port Already in Use',
        'troubleshooting.port.desc': 'If port 18000 is already in use, change it via environment variable:',
        'troubleshooting.db.title': 'Database Connection Issues',
        'troubleshooting.db.desc': 'Make sure the database connection string is correct and database is accessible. Check container logs for details.',
        'troubleshooting.auth.title': 'Authentication Issues',
        'troubleshooting.auth.desc': 'If authentication is enabled, use correct credentials. Default: admin@local / admin. Check settings via environment variables.'
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
    document.querySelectorAll('.feature-card, .api-card, .sdk-card, .why-card').forEach(card => {
        card.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        card.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        cardObserver.observe(card);
    });
});
