package flagent.frontend.i18n

/**
 * Localization strings for Flagent Frontend
 */
object LocalizedStrings {
    // Current locale (default: ru)
    var currentLocale: String = "ru"
    
    // Navigation
    val home: String get() = when (currentLocale) {
        "ru" -> "Главная"
        else -> "Home"
    }

    fun flagNumber(id: Int): String = when (currentLocale) {
        "ru" -> "Флаг #$id"
        else -> "Flag #$id"
    }
    
    val api: String get() = "API"
    val docs: String get() = when (currentLocale) {
        "ru" -> "Документация"
        else -> "Docs"
    }
    
    // Flags List
    val featureFlags: String get() = when (currentLocale) {
        "ru" -> "Feature Flags"
        else -> "Feature Flags"
    }
    
    val createNewFlag: String get() = when (currentLocale) {
        "ru" -> "Создать новый флаг"
        else -> "Create New Flag"
    }

    val featureFlagsTooltipTitle: String get() = when (currentLocale) {
        "ru" -> "Feature Flags"
        else -> "Feature Flags"
    }

    val featureFlagsTooltipDescription: String get() = when (currentLocale) {
        "ru" -> "Feature flags (feature toggles) позволяют управлять rollout новых функций, запускать A/B тесты и безопасно откатывать изменения без деплоя нового кода."
        else -> "Feature flags (feature toggles) allow you to control the rollout of new features, run A/B tests, and safely rollback changes without deploying new code."
    }

    val featureFlagsTooltipDetails: String get() = when (currentLocale) {
        "ru" -> "Создайте флаг, введя описание. Система автоматически сгенерирует уникальный ключ. Вы можете включать/выключать флаги, создавать сегменты для таргетинга и настраивать распределения для постепенных rollout."
        else -> "Create a flag by entering a description. The system will generate a unique key automatically. You can enable/disable flags, create segments for targeting, and set up distributions for gradual rollouts."
    }

    val featureFlagsSubtitle: String get() = when (currentLocale) {
        "ru" -> "Создавайте и управляйте feature flags для контроля выкатки и A/B тестов"
        else -> "Create and manage feature flags to control rollouts and A/B tests"
    }
    
    val flagDescription: String get() = when (currentLocale) {
        "ru" -> "Описание флага"
        else -> "Flag description"
    }
    
    val enterFlagDescriptionPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Введите описание флага..."
        else -> "Enter flag description..."
    }

    val createFlag: String get() = when (currentLocale) {
        "ru" -> "Создать флаг"
        else -> "Create Flag"
    }
    val createFirstFlag: String get() = when (currentLocale) {
        "ru" -> "Создать первый флаг"
        else -> "Create your first flag"
    }
    
    val creating: String get() = when (currentLocale) {
        "ru" -> "Создание..."
        else -> "Creating..."
    }

    val saving: String get() = when (currentLocale) {
        "ru" -> "Сохранение..."
        else -> "Saving..."
    }

    val deleting: String get() = when (currentLocale) {
        "ru" -> "Удаление..."
        else -> "Deleting..."
    }

    val confirmDelete: String get() = when (currentLocale) {
        "ru" -> "Подтвердить удаление"
        else -> "Confirm Delete"
    }

    val moreOptions: String get() = when (currentLocale) {
        "ru" -> "Еще варианты..."
        else -> "More options..."
    }

    val createSimpleBooleanFlag: String get() = when (currentLocale) {
        "ru" -> "Создать простой булевый флаг"
        else -> "Create Simple Boolean Flag"
    }

    val createFlagModalTitle: String get() = when (currentLocale) {
        "ru" -> "Выберите тип флага"
        else -> "Choose flag type"
    }

    val createFlagTypeBooleanTitle: String get() = when (currentLocale) {
        "ru" -> "Булевый флаг"
        else -> "Boolean flag"
    }

    val createFlagTypeBooleanDescription: String get() = when (currentLocale) {
        "ru" -> "Включено/выключено. Подходит для kill switch и простых фич."
        else -> "On/off. Best for kill switches and simple feature toggles."
    }

    val createFlagTypeFullFormTitle: String get() = when (currentLocale) {
        "ru" -> "Полная форма"
        else -> "Full form"
    }

    val createFlagTypeFullFormDescription: String get() = when (currentLocale) {
        "ru" -> "Описание и ключ вручную, затем настройка вариантов и сегментов."
        else -> "Enter description and key manually, then configure variants and segments."
    }
    val createFlagTypeAbTestTitle: String get() = when (currentLocale) {
        "ru" -> "A/B тест"
        else -> "A/B test"
    }
    val createFlagTypeAbTestDescription: String get() = when (currentLocale) {
        "ru" -> "Два и более варианта с распределением трафика. Для экспериментов."
        else -> "Two or more variants with traffic split. For experiments."
    }
    val createFlagTypeNumericTitle: String get() = when (currentLocale) {
        "ru" -> "Числовой флаг"
        else -> "Numeric flag"
    }
    val createFlagTypeNumericDescription: String get() = when (currentLocale) {
        "ru" -> "Значение — число. Лимиты, пороги, настройки."
        else -> "Value is a number. Limits, thresholds, config."
    }
    val createFlagTypeStringJsonTitle: String get() = when (currentLocale) {
        "ru" -> "Строка / JSON"
        else -> "String / JSON"
    }
    val createFlagTypeStringJsonDescription: String get() = when (currentLocale) {
        "ru" -> "Текстовое или JSON-значение. Конфиги, payload."
        else -> "Text or JSON value. Configs, payload."
    }

    val createFromTemplate: String get() = when (currentLocale) {
        "ru" -> "Создать из шаблона"
        else -> "Create from Template"
    }
    
    val simpleOnOff: String get() = when (currentLocale) {
        "ru" -> "Простой (вкл/выкл)"
        else -> "Simple (on/off)"
    }
    
    val gradualRollout: String get() = when (currentLocale) {
        "ru" -> "Постепенный rollout"
        else -> "Gradual rollout"
    }
    
    val abTest: String get() = when (currentLocale) {
        "ru" -> "A/B тест"
        else -> "A/B test"
    }
    
    val searchFlags: String get() = when (currentLocale) {
        "ru" -> "Поиск флагов..."
        else -> "Search flags..."
    }

    val searchFlagsDetailedPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Поиск по описанию..."
        else -> "Search by description..."
    }

    val keyExactPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Ключ (точное совпадение)"
        else -> "Key (exact)"
    }

    val allStatus: String get() = when (currentLocale) {
        "ru" -> "Все статусы"
        else -> "All Status"
    }
    
    val allFlags: String get() = when (currentLocale) {
        "ru" -> "Все флаги"
        else -> "All Flags"
    }
    
    val enabledFlags: String get() = when (currentLocale) {
        "ru" -> "Включенные"
        else -> "Enabled"
    }
    
    val disabledFlags: String get() = when (currentLocale) {
        "ru" -> "Выключенные"
        else -> "Disabled"
    }

    val filterByTags: String get() = when (currentLocale) {
        "ru" -> "Фильтр по тегам"
        else -> "Filter by tags"
    }

    val pageOf: String get() = when (currentLocale) {
        "ru" -> "из"
        else -> "of"
    }

    val previous: String get() = when (currentLocale) {
        "ru" -> "Назад"
        else -> "Previous"
    }

    val next: String get() = when (currentLocale) {
        "ru" -> "Вперёд"
        else -> "Next"
    }

    val enableSelected: String get() = when (currentLocale) {
        "ru" -> "Включить выбранные"
        else -> "Enable selected"
    }

    val disableSelected: String get() = when (currentLocale) {
        "ru" -> "Выключить выбранные"
        else -> "Disable selected"
    }

    val clearSelection: String get() = when (currentLocale) {
        "ru" -> "Снять выделение"
        else -> "Clear selection"
    }

    val bulkEnableConfirm: String get() = when (currentLocale) {
        "ru" -> "Включить выбранные флаги?"
        else -> "Enable selected flags?"
    }

    val bulkDisableConfirm: String get() = when (currentLocale) {
        "ru" -> "Выключить выбранные флаги?"
        else -> "Disable selected flags?"
    }

    val flatList: String get() = when (currentLocale) {
        "ru" -> "Плоский список"
        else -> "Flat list"
    }

    val groupByTags: String get() = when (currentLocale) {
        "ru" -> "По тегам"
        else -> "By tags"
    }

    val noTagsGroup: String get() = when (currentLocale) {
        "ru" -> "Без тегов"
        else -> "No tags"
    }

    val savedViews: String get() = when (currentLocale) {
        "ru" -> "Сохранённые представления"
        else -> "Saved views"
    }

    val saveCurrentView: String get() = when (currentLocale) {
        "ru" -> "Сохранить текущее"
        else -> "Save current view"
    }

    val viewName: String get() = when (currentLocale) {
        "ru" -> "Название"
        else -> "View name"
    }

    val quickFilterExperiments: String get() = when (currentLocale) {
        "ru" -> "Эксперименты"
        else -> "Experiments"
    }

    val quickFilterWithSegments: String get() = when (currentLocale) {
        "ru" -> "С сегментами"
        else -> "With segments"
    }

    val exportFilters: String get() = when (currentLocale) {
        "ru" -> "Экспорт фильтров"
        else -> "Export filters"
    }

    val copyShareLink: String get() = when (currentLocale) {
        "ru" -> "Копировать ссылку"
        else -> "Copy share link"
    }

    val filtersCopied: String get() = when (currentLocale) {
        "ru" -> "Фильтры скопированы"
        else -> "Filters copied"
    }
    
    val showDeletedFlags: String get() = when (currentLocale) {
        "ru" -> "Показать архивные флаги"
        else -> "Show archived flags"
    }
    
    val hideDeletedFlags: String get() = when (currentLocale) {
        "ru" -> "Скрыть архивные флаги"
        else -> "Hide archived flags"
    }
    
    val deletedFlags: String get() = when (currentLocale) {
        "ru" -> "Архивные флаги"
        else -> "Archived flags"
    }
    
    val restore: String get() = when (currentLocale) {
        "ru" -> "Восстановить"
        else -> "Restore"
    }
    
    val loading: String get() = when (currentLocale) {
        "ru" -> "Загрузка..."
        else -> "Loading..."
    }
    
    val noFlags: String get() = when (currentLocale) {
        "ru" -> "Флаги не найдены"
        else -> "No flags found"
    }

    val tryAdjustingSearchOrFilters: String get() = when (currentLocale) {
        "ru" -> "Попробуйте изменить запрос поиска или фильтры"
        else -> "Try adjusting your search or filters"
    }
    
    val noDeletedFlags: String get() = when (currentLocale) {
        "ru" -> "Архивных флагов нет"
        else -> "No archived flags"
    }

    val noTags: String get() = when (currentLocale) {
        "ru" -> "Нет тегов"
        else -> "No tags"
    }

    val flagId: String get() = when (currentLocale) {
        "ru" -> "ID флага"
        else -> "Flag ID"
    }

    val tags: String get() = when (currentLocale) {
        "ru" -> "Теги"
        else -> "Tags"
    }

    val lastUpdatedBy: String get() = when (currentLocale) {
        "ru" -> "Кто обновил"
        else -> "Last Updated By"
    }

    val updatedAt: String get() = when (currentLocale) {
        "ru" -> "Когда обновлено"
        else -> "Updated At"
    }

    val updatedAtUtc: String get() = when (currentLocale) {
        "ru" -> "Когда обновлено (UTC)"
        else -> "Updated At (UTC)"
    }

    val status: String get() = when (currentLocale) {
        "ru" -> "Статус"
        else -> "Status"
    }

    val action: String get() = when (currentLocale) {
        "ru" -> "Действие"
        else -> "Action"
    }
    
    val enabled: String get() = when (currentLocale) {
        "ru" -> "Включен"
        else -> "Enabled"
    }
    
    val disabled: String get() = when (currentLocale) {
        "ru" -> "Выключен"
        else -> "Disabled"
    }
    
    val segments: String get() = when (currentLocale) {
        "ru" -> "Сегменты"
        else -> "Segments"
    }
    
    val variants: String get() = when (currentLocale) {
        "ru" -> "Варианты"
        else -> "Variants"
    }
    
    val view: String get() = when (currentLocale) {
        "ru" -> "Просмотр"
        else -> "View"
    }
    
    val edit: String get() = when (currentLocale) {
        "ru" -> "Редактировать"
        else -> "Edit"
    }
    
    val delete: String get() = when (currentLocale) {
        "ru" -> "Удалить"
        else -> "Delete"
    }
    
    val history: String get() = when (currentLocale) {
        "ru" -> "История"
        else -> "History"
    }

    val anomalies: String get() = when (currentLocale) {
        "ru" -> "Аномалии"
        else -> "Anomalies"
    }

    val debug: String get() = when (currentLocale) {
        "ru" -> "Отладка"
        else -> "Debug"
    }
    
    val save: String get() = when (currentLocale) {
        "ru" -> "Сохранить"
        else -> "Save"
    }

    val create: String get() = when (currentLocale) {
        "ru" -> "Создать"
        else -> "Create"
    }
    
    val cancel: String get() = when (currentLocale) {
        "ru" -> "Отмена"
        else -> "Cancel"
    }

    // Editors
    val jsonValid: String get() = when (currentLocale) {
        "ru" -> "JSON валиден"
        else -> "Valid JSON"
    }

    val jsonInvalid: String get() = when (currentLocale) {
        "ru" -> "Невалидный JSON"
        else -> "Invalid JSON"
    }

    fun invalidJsonWithReason(reason: String?): String = when (currentLocale) {
        "ru" -> "Невалидный JSON: ${reason ?: ""}".trimEnd()
        else -> "Invalid JSON: ${reason ?: ""}".trimEnd()
    }

    val jsonHelperText: String get() = when (currentLocale) {
        "ru" -> "Введите пары ключ/значение JSON без внешних фигурных скобок, например: \"key\": \"value\". Валидация выполняется в реальном времени."
        else -> "Enter JSON key/value pairs without outer braces, e.g., \"key\": \"value\". JSON is validated in real-time."
    }

    val markdownEditorLabel: String get() = when (currentLocale) {
        "ru" -> "Markdown редактор:"
        else -> "Markdown Editor:"
    }

    val markdownPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Введите markdown текст..."
        else -> "Enter markdown text..."
    }

    val previewLabel: String get() = when (currentLocale) {
        "ru" -> "Предпросмотр:"
        else -> "Preview:"
    }

    val noContent: String get() = when (currentLocale) {
        "ru" -> "Нет содержимого"
        else -> "No content"
    }
    
    // Flag Editor
    val flagDetails: String get() = when (currentLocale) {
        "ru" -> "Детали флага"
        else -> "Flag Details"
    }

    val backToFlags: String get() = when (currentLocale) {
        "ru" -> "Назад к флагам"
        else -> "Back to Flags"
    }

    val recentFlags: String get() = when (currentLocale) {
        "ru" -> "Недавние флаги"
        else -> "Recent flags"
    }

    val config: String get() = when (currentLocale) {
        "ru" -> "Конфигурация"
        else -> "Config"
    }

    val keyOptional: String get() = when (currentLocale) {
        "ru" -> "Ключ (опционально):"
        else -> "Key (optional):"
    }

    val keyWithColon: String get() = when (currentLocale) {
        "ru" -> "Ключ:"
        else -> "Key:"
    }

    val flagSettings: String get() = when (currentLocale) {
        "ru" -> "Настройки флага"
        else -> "Flag Settings"
    }

    val dataRecords: String get() = when (currentLocale) {
        "ru" -> "Записи данных"
        else -> "Data Records"
    }

    val flagDescriptionWithColon: String get() = when (currentLocale) {
        "ru" -> "Описание флага:"
        else -> "Flag Description:"
    }

    val entityType: String get() = when (currentLocale) {
        "ru" -> "Тип сущности:"
        else -> "Entity Type:"
    }

    val flagNotes: String get() = when (currentLocale) {
        "ru" -> "Заметки по флагу"
        else -> "Flag Notes"
    }

    val add: String get() = when (currentLocale) {
        "ru" -> "Добавить"
        else -> "Add"
    }

    val newTag: String get() = when (currentLocale) {
        "ru" -> "+ Новый тег"
        else -> "+ New Tag"
    }

    val noVariantsYet: String get() = when (currentLocale) {
        "ru" -> "Для этого флага еще не создано вариантов"
        else -> "No variants created for this feature flag yet"
    }

    val newSegment: String get() = when (currentLocale) {
        "ru" -> "Новый сегмент"
        else -> "New Segment"
    }

    val noSegmentsYet: String get() = when (currentLocale) {
        "ru" -> "Для этого флага еще не создано сегментов"
        else -> "No segments created for this feature flag yet"
    }
    val noSegmentsInProject: String get() = when (currentLocale) {
        "ru" -> "В проекте пока нет сегментов. Создайте флаг и добавьте сегменты в его настройках."
        else -> "No segments in project yet. Create a flag and add segments in its settings."
    }

    val dragToReorder: String get() = when (currentLocale) {
        "ru" -> "Перетащите для изменения порядка"
        else -> "Drag to reorder"
    }

    val constraintsMatchAll: String get() = when (currentLocale) {
        "ru" -> "Ограничения (должны пройти ВСЕ)"
        else -> "Constraints (match ALL of them)"
    }

    val noConstraintsAllPass: String get() = when (currentLocale) {
        "ru" -> "Нет ограничений (пройдут ВСЕ)"
        else -> "No constraints (ALL will pass)"
    }

    val distribution: String get() = when (currentLocale) {
        "ru" -> "Распределение"
        else -> "Distribution"
    }

    val noDistributionYet: String get() = when (currentLocale) {
        "ru" -> "Распределение еще не задано"
        else -> "No distribution yet"
    }

    val deleteFlagConfirm: String get() = when (currentLocale) {
        "ru" -> "Вы уверены, что хотите удалить этот feature flag?"
        else -> "Are you sure you want to delete this feature flag?"
    }

    val deleteFlag: String get() = when (currentLocale) {
        "ru" -> "Удалить флаг"
        else -> "Delete Flag"
    }

    val failedToDeleteFlag: String get() = when (currentLocale) {
        "ru" -> "Не удалось удалить флаг"
        else -> "Failed to delete flag"
    }
    
    val flagKey: String get() = when (currentLocale) {
        "ru" -> "Ключ флага"
        else -> "Flag Key"
    }
    
    val description: String get() = when (currentLocale) {
        "ru" -> "Описание"
        else -> "Description"
    }
    
    val enableThisFlag: String get() = when (currentLocale) {
        "ru" -> "Включить этот флаг"
        else -> "Enable this flag"
    }
    
    val addSegment: String get() = when (currentLocale) {
        "ru" -> "Добавить сегмент"
        else -> "Add Segment"
    }
    
    val addVariant: String get() = when (currentLocale) {
        "ru" -> "Добавить вариант"
        else -> "Add Variant"
    }

    val createVariant: String get() = when (currentLocale) {
        "ru" -> "Создать вариант"
        else -> "Create Variant"
    }

    val saveFlag: String get() = when (currentLocale) {
        "ru" -> "Сохранить флаг"
        else -> "Save Flag"
    }

    val saveVariant: String get() = when (currentLocale) {
        "ru" -> "Сохранить вариант"
        else -> "Save Variant"
    }

    val duplicateSegment: String get() = when (currentLocale) {
        "ru" -> "Дублировать"
        else -> "Duplicate"
    }
    val exportSegment: String get() = when (currentLocale) {
        "ru" -> "Экспорт"
        else -> "Export"
    }
    val saveSegmentSetting: String get() = when (currentLocale) {
        "ru" -> "Сохранить настройки сегмента"
        else -> "Save Segment Setting"
    }
    
    val addConstraint: String get() = when (currentLocale) {
        "ru" -> "Добавить ограничение"
        else -> "Add Constraint"
    }
    
    val addDistribution: String get() = when (currentLocale) {
        "ru" -> "Добавить распределение"
        else -> "Add Distribution"
    }
    
    val rollout: String get() = when (currentLocale) {
        "ru" -> "Rollout"
        else -> "Rollout"
    }
    
    val rolloutPercent: String get() = when (currentLocale) {
        "ru" -> "Процент Rollout"
        else -> "Rollout Percent"
    }
    
    val constraints: String get() = when (currentLocale) {
        "ru" -> "Ограничения"
        else -> "Constraints"
    }
    
    val distributions: String get() = when (currentLocale) {
        "ru" -> "Распределения"
        else -> "Distributions"
    }
    
    val variantKey: String get() = when (currentLocale) {
        "ru" -> "Ключ варианта"
        else -> "Variant Key"
    }
    
    val attachment: String get() = when (currentLocale) {
        "ru" -> "Вложение"
        else -> "Attachment"
    }
    
    val property: String get() = when (currentLocale) {
        "ru" -> "Свойство"
        else -> "Property"
    }
    
    val operator: String get() = when (currentLocale) {
        "ru" -> "Оператор"
        else -> "Operator"
    }
    
    val value: String get() = when (currentLocale) {
        "ru" -> "Значение"
        else -> "Value"
    }
    
    val percent: String get() = when (currentLocale) {
        "ru" -> "Процент"
        else -> "Percent"
    }
    
    // Debug Console
    val debugConsole: String get() = when (currentLocale) {
        "ru" -> "Консоль отладки"
        else -> "Debug Console"
    }

    val debugConsoleTooltipDescription: String get() = when (currentLocale) {
        "ru" -> "Консоль отладки позволяет тестировать evaluation флагов в реальном времени. Введите ID сущности, тип и контекст, чтобы увидеть назначенный вариант и причины."
        else -> "The Debug Console allows you to test flag evaluation in real-time. Enter an entity ID, type, and context to see which variant would be assigned and why."
    }

    val debugConsoleTooltipDetails: String get() = when (currentLocale) {
        "ru" -> "Используйте это, чтобы проверить сегменты, ограничения и распределения перед выкладкой в production. Включите режим отладки для подробной информации о процессе evaluation."
        else -> "Use this to verify that your segments, constraints, and distributions are working correctly before deploying to production. Enable debug mode to see detailed evaluation information."
    }

    val evaluation: String get() = when (currentLocale) {
        "ru" -> "Оценка"
        else -> "Evaluation"
    }

    val batchEvaluation: String get() = when (currentLocale) {
        "ru" -> "Пакетная оценка"
        else -> "Batch Evaluation"
    }

    val request: String get() = when (currentLocale) {
        "ru" -> "Запрос"
        else -> "Request"
    }

    val response: String get() = when (currentLocale) {
        "ru" -> "Ответ"
        else -> "Response"
    }

    val flagKeyWithColon: String get() = when (currentLocale) {
        "ru" -> "Ключ флага:"
        else -> "Flag Key:"
    }

    val entityIdWithColon: String get() = when (currentLocale) {
        "ru" -> "ID сущности:"
        else -> "Entity ID:"
    }

    val entityContextJsonWithColon: String get() = when (currentLocale) {
        "ru" -> "Контекст сущности (JSON):"
        else -> "Entity Context (JSON):"
    }

    val entitiesJsonArrayWithColon: String get() = when (currentLocale) {
        "ru" -> "Сущности (JSON массив):"
        else -> "Entities (JSON array):"
    }

    val flagIdsCommaSeparated: String get() = when (currentLocale) {
        "ru" -> "ID флагов (через запятую):"
        else -> "Flag IDs (comma-separated):"
    }

    val flagKeysCommaSeparated: String get() = when (currentLocale) {
        "ru" -> "Ключи флагов (через запятую):"
        else -> "Flag Keys (comma-separated):"
    }

    val enableDebug: String get() = when (currentLocale) {
        "ru" -> "Включить отладку"
        else -> "Enable Debug"
    }

    val noResponseYet: String get() = when (currentLocale) {
        "ru" -> "Ответа пока нет"
        else -> "No response yet"
    }
    
    val testFlagEvaluation: String get() = when (currentLocale) {
        "ru" -> "Тестирование оценки флага"
        else -> "Test flag evaluation"
    }
    
    val entityId: String get() = when (currentLocale) {
        "ru" -> "ID сущности"
        else -> "Entity ID"
    }
    
    val entityContext: String get() = when (currentLocale) {
        "ru" -> "Контекст сущности"
        else -> "Entity Context"
    }
    
    val evaluate: String get() = when (currentLocale) {
        "ru" -> "Оценить"
        else -> "Evaluate"
    }
    
    val result: String get() = when (currentLocale) {
        "ru" -> "Результат"
        else -> "Result"
    }
    
    val evaluationLogs: String get() = when (currentLocale) {
        "ru" -> "Логи оценки"
        else -> "Evaluation Logs"
    }
    
    // Flag History
    val changeHistory: String get() = when (currentLocale) {
        "ru" -> "История изменений"
        else -> "Change History"
    }
    
    val timestamp: String get() = when (currentLocale) {
        "ru" -> "Время"
        else -> "Timestamp"
    }
    
    val changes: String get() = when (currentLocale) {
        "ru" -> "Изменения"
        else -> "Changes"
    }
    
    val user: String get() = when (currentLocale) {
        "ru" -> "Пользователь"
        else -> "User"
    }
    
    val noHistory: String get() = when (currentLocale) {
        "ru" -> "История изменений недоступна"
        else -> "No history available"
    }

    val flagHistory: String get() = when (currentLocale) {
        "ru" -> "История флага"
        else -> "Flag History"
    }

    val flagHistoryTooltipDescription: String get() = when (currentLocale) {
        "ru" -> "История флага показывает все изменения feature flag с течением времени. Каждый снимок (snapshot) — это состояние флага в конкретный момент."
        else -> "Flag History shows all changes made to a feature flag over time. Each snapshot represents the state of the flag at a specific point in time."
    }

    val flagHistoryTooltipDetails: String get() = when (currentLocale) {
        "ru" -> "Вы можете сравнить любые два снимка, чтобы увидеть, что изменилось. Полезно для аудита изменений, понимания rollout и отладки проблем."
        else -> "You can compare any two snapshots to see what changed. This is useful for auditing changes, understanding rollouts, and debugging issues."
    }

    val snapshotId: String get() = when (currentLocale) {
        "ru" -> "ID снимка"
        else -> "Snapshot ID"
    }

    val updatedByUpper: String get() = when (currentLocale) {
        "ru" -> "ОБНОВИЛ"
        else -> "UPDATED BY"
    }

    val noChanges: String get() = when (currentLocale) {
        "ru" -> "Нет изменений"
        else -> "No changes"
    }

    val editDistribution: String get() = when (currentLocale) {
        "ru" -> "Редактировать распределение"
        else -> "Edit Distribution"
    }

    val saveDistribution: String get() = when (currentLocale) {
        "ru" -> "Сохранить распределение"
        else -> "Save Distribution"
    }

    val distributionsTooltipTitle: String get() = when (currentLocale) {
        "ru" -> "Распределения"
        else -> "Distributions"
    }

    val distributionsTooltipDescription: String get() = when (currentLocale) {
        "ru" -> "Распределения задают, как варианты назначаются сущностям внутри сегмента. Каждый вариант получает процент трафика. Сумма процентов должна быть ровно 100%."
        else -> "Distributions define how variants are assigned to entities in a segment. Each variant gets a percentage of the traffic. Percentages must add up to exactly 100%."
    }

    val distributionsTooltipDetails: String get() = when (currentLocale) {
        "ru" -> "Например, сплит 50/50 означает, что половина сущностей получает вариант A, а половина — вариант B. Используется для A/B тестов и постепенных rollout."
        else -> "For example, a 50/50 split means half the entities get variant A and half get variant B. This is used for A/B testing and gradual rollouts."
    }

    val constraintsTooltipTitle: String get() = when (currentLocale) {
        "ru" -> "Ограничения"
        else -> "Constraints"
    }

    val constraintsTooltipDescription: String get() = when (currentLocale) {
        "ru" -> "Ограничения задают правила таргетинга для сегментов. Они проверяют, соответствуют ли свойства сущности условиям. Только сущности, прошедшие все ограничения сегмента, получат варианты из этого сегмента."
        else -> "Constraints define targeting rules for segments. They check if an entity's properties match specific conditions. Only entities that match all constraints in a segment will be assigned variants from that segment."
    }

    val constraintsTooltipDetails: String get() = when (currentLocale) {
        "ru" -> "Операторы: EQ (равно), NEQ (не равно), GT/LT (больше/меньше), IN/NOT_IN (в списке), CONTAINS (подстрока), STARTS_WITH/ENDS_WITH (совпадение по началу/концу)."
        else -> "Operators: EQ (equals), NEQ (not equals), GT/LT (greater/less than), IN/NOT_IN (in list), CONTAINS (string contains), STARTS_WITH/ENDS_WITH (string matching)."
    }

    val segmentEditorTitle: String get() = when (currentLocale) {
        "ru" -> "Сегменты"
        else -> "Segments"
    }

    val segmentsTooltipDescription: String get() = when (currentLocale) {
        "ru" -> "Сегменты группируют сущности, которые должны получать одинаковое назначение варианта. У каждого сегмента могут быть ограничения (правила таргетинга) и распределения (проценты вариантов)."
        else -> "Segments group entities that should receive the same variant assignment. Each segment can have constraints (targeting rules) and distributions (variant percentages)."
    }

    val segmentsTooltipDetails: String get() = when (currentLocale) {
        "ru" -> "Сегменты проверяются по порядку. Первый сегмент, чьи ограничения совпали с сущностью, определит назначенный вариант. Используйте rollout %, чтобы контролировать долю совпавших сущностей, которым назначается вариант."
        else -> "Segments are evaluated in order. The first segment whose constraints match an entity will determine which variant is assigned. Use rollout percentage to control what portion of matching entities get assigned."
    }

    val editConstraint: String get() = when (currentLocale) {
        "ru" -> "Редактировать ограничение"
        else -> "Edit Constraint"
    }

    val updateConstraint: String get() = when (currentLocale) {
        "ru" -> "Обновить ограничение"
        else -> "Update Constraint"
    }

    val createConstraint: String get() = when (currentLocale) {
        "ru" -> "Создать ограничение"
        else -> "Create Constraint"
    }

    val editSegment: String get() = when (currentLocale) {
        "ru" -> "Редактировать сегмент"
        else -> "Edit Segment"
    }

    val updateSegment: String get() = when (currentLocale) {
        "ru" -> "Обновить сегмент"
        else -> "Update Segment"
    }

    val createSegment: String get() = when (currentLocale) {
        "ru" -> "Создать сегмент"
        else -> "Create Segment"
    }

    val searchSegmentsPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Поиск по флагу, описанию, ID…"
        else -> "Search by flag, description, ID…"
    }

    val noSegmentsMatchSearch: String get() = when (currentLocale) {
        "ru" -> "Нет сегментов по запросу"
        else -> "No segments match your search"
    }

    val distributionsSavedSuccessfully: String get() = when (currentLocale) {
        "ru" -> "Распределения успешно сохранены!"
        else -> "Distributions saved successfully!"
    }
    
    // Errors
    val error: String get() = when (currentLocale) {
        "ru" -> "Ошибка"
        else -> "Error"
    }
    
    val failedToLoadFlags: String get() = when (currentLocale) {
        "ru" -> "Не удалось загрузить флаги"
        else -> "Failed to load flags"
    }

    val failedToLoadFlag: String get() = when (currentLocale) {
        "ru" -> "Не удалось загрузить флаг"
        else -> "Failed to load flag"
    }

    val failedToLoadDeletedFlags: String get() = when (currentLocale) {
        "ru" -> "Не удалось загрузить удаленные флаги"
        else -> "Failed to load deleted flags"
    }

    val failedToRestoreFlag: String get() = when (currentLocale) {
        "ru" -> "Не удалось восстановить флаг"
        else -> "Failed to restore flag"
    }

    val permanentDeleteFlag: String get() = when (currentLocale) {
        "ru" -> "Удалить навсегда"
        else -> "Permanent delete"
    }

    val confirmPermanentDeleteFlag: String get() = when (currentLocale) {
        "ru" -> "Удалить флаг навсегда? Это действие нельзя отменить."
        else -> "Permanently delete this flag? This cannot be undone."
    }

    val failedToPermanentDeleteFlag: String get() = when (currentLocale) {
        "ru" -> "Не удалось удалить флаг навсегда"
        else -> "Failed to permanently delete flag"
    }

    val failedToCreateFlag: String get() = when (currentLocale) {
        "ru" -> "Не удалось создать флаг"
        else -> "Failed to create flag"
    }

    val constraintNotFound: String get() = when (currentLocale) {
        "ru" -> "Ограничение не найдено"
        else -> "Constraint not found"
    }

    val failedToLoadConstraint: String get() = when (currentLocale) {
        "ru" -> "Не удалось загрузить ограничение"
        else -> "Failed to load constraint"
    }

    val failedToSaveConstraint: String get() = when (currentLocale) {
        "ru" -> "Не удалось сохранить ограничение"
        else -> "Failed to save constraint"
    }

    val segmentNotFound: String get() = when (currentLocale) {
        "ru" -> "Сегмент не найден"
        else -> "Segment not found"
    }

    val failedToLoadSegment: String get() = when (currentLocale) {
        "ru" -> "Не удалось загрузить сегмент"
        else -> "Failed to load segment"
    }

    val failedToSaveSegment: String get() = when (currentLocale) {
        "ru" -> "Не удалось сохранить сегмент"
        else -> "Failed to save segment"
    }

    val failedToLoadDistributions: String get() = when (currentLocale) {
        "ru" -> "Не удалось загрузить распределения"
        else -> "Failed to load distributions"
    }

    val failedToSaveDistributions: String get() = when (currentLocale) {
        "ru" -> "Не удалось сохранить распределения"
        else -> "Failed to save distributions"
    }

    val failedToLoadFlagHistory: String get() = when (currentLocale) {
        "ru" -> "Не удалось загрузить историю флага"
        else -> "Failed to load flag history"
    }

    val descriptionIsRequired: String get() = when (currentLocale) {
        "ru" -> "Описание обязательно"
        else -> "Description is required"
    }

    val propertyAndValueAreRequired: String get() = when (currentLocale) {
        "ru" -> "Свойство и значение обязательны"
        else -> "Property and Value are required"
    }

    fun percentagesMustAddUpTo100(total: Int): String = when (currentLocale) {
        "ru" -> "Сумма процентов должна быть равна 100% (сейчас $total%)"
        else -> "Percentages must add up to 100% (currently at $total%)"
    }
    
    val failedToSave: String get() = when (currentLocale) {
        "ru" -> "Не удалось сохранить"
        else -> "Failed to save"
    }

    val failedToSaveFlag: String get() = when (currentLocale) {
        "ru" -> "Не удалось сохранить флаг"
        else -> "Failed to save flag"
    }

    val failedToUpdateFlag: String get() = when (currentLocale) {
        "ru" -> "Не удалось обновить флаг"
        else -> "Failed to update flag"
    }

    val failedToAddTag: String get() = when (currentLocale) {
        "ru" -> "Не удалось добавить тег"
        else -> "Failed to add tag"
    }

    val failedToRemoveTag: String get() = when (currentLocale) {
        "ru" -> "Не удалось удалить тег"
        else -> "Failed to remove tag"
    }

    val failedToCreateVariant: String get() = when (currentLocale) {
        "ru" -> "Не удалось создать вариант"
        else -> "Failed to create variant"
    }

    val failedToUpdateVariant: String get() = when (currentLocale) {
        "ru" -> "Не удалось обновить вариант"
        else -> "Failed to update variant"
    }

    fun confirmDeleteVariant(variantId: Int, variantKey: String): String = when (currentLocale) {
        "ru" -> "Вы уверены, что хотите удалить вариант #$variantId [$variantKey]?"
        else -> "Are you sure you want to delete variant #$variantId [$variantKey]?"
    }

    val failedToDeleteVariant: String get() = when (currentLocale) {
        "ru" -> "Не удалось удалить вариант"
        else -> "Failed to delete variant"
    }

    val failedToReorderSegments: String get() = when (currentLocale) {
        "ru" -> "Не удалось изменить порядок сегментов"
        else -> "Failed to reorder segments"
    }

    val failedToCreateSegment: String get() = when (currentLocale) {
        "ru" -> "Не удалось создать сегмент"
        else -> "Failed to create segment"
    }

    val failedToDuplicateSegment: String get() = when (currentLocale) {
        "ru" -> "Не удалось дублировать сегмент"
        else -> "Failed to duplicate segment"
    }
    val failedToDeleteSegment: String get() = when (currentLocale) {
        "ru" -> "Не удалось удалить сегмент"
        else -> "Failed to delete segment"
    }

    val confirmDeleteSegment: String get() = when (currentLocale) {
        "ru" -> "Вы уверены, что хотите удалить этот сегмент?"
        else -> "Are you sure you want to delete this segment?"
    }

    val failedToCreateConstraint: String get() = when (currentLocale) {
        "ru" -> "Не удалось создать ограничение"
        else -> "Failed to create constraint"
    }

    val failedToDeleteConstraint: String get() = when (currentLocale) {
        "ru" -> "Не удалось удалить ограничение"
        else -> "Failed to delete constraint"
    }

    val confirmDeleteConstraint: String get() = when (currentLocale) {
        "ru" -> "Вы уверены, что хотите удалить это ограничение?"
        else -> "Are you sure you want to delete this constraint?"
    }

    val enterOrSelectEntityTypePlaceholder: String get() = when (currentLocale) {
        "ru" -> "Введите или выберите тип сущности"
        else -> "Enter or select entity type"
    }

    val enterVariantKeyPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Введите ключ варианта..."
        else -> "Enter variant key..."
    }

    val enterSegmentDescriptionPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Введите описание сегмента"
        else -> "Enter segment description"
    }

    val propertyPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Свойство"
        else -> "Property"
    }

    val valuePlaceholder: String get() = when (currentLocale) {
        "ru" -> "Значение"
        else -> "Value"
    }

    val flagKeyPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Ключ флага"
        else -> "Flag key"
    }

    val flagDescriptionPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Описание флага"
        else -> "Flag description"
    }

    val variantId: String get() = when (currentLocale) {
        "ru" -> "ID варианта"
        else -> "Variant ID"
    }

    val segmentId: String get() = when (currentLocale) {
        "ru" -> "ID сегмента"
        else -> "Segment ID"
    }
    
    val flagNotFound: String get() = when (currentLocale) {
        "ru" -> "Флаг не найден"
        else -> "Flag not found"
    }
    
    // Success messages
    val flagCreated: String get() = when (currentLocale) {
        "ru" -> "Флаг создан успешно"
        else -> "Flag created successfully"
    }
    
    val flagUpdated: String get() = when (currentLocale) {
        "ru" -> "Флаг обновлен успешно"
        else -> "Flag updated successfully"
    }
    
    val flagDeleted: String get() = when (currentLocale) {
        "ru" -> "Флаг удален успешно"
        else -> "Flag deleted successfully"
    }
    
    val flagRestored: String get() = when (currentLocale) {
        "ru" -> "Флаг восстановлен успешно"
        else -> "Flag restored successfully"
    }
    
    // Settings
    val settingsTitle: String get() = when (currentLocale) {
        "ru" -> "Настройки"
        else -> "Settings"
    }
    val settingsSubtitle: String get() = when (currentLocale) {
        "ru" -> "Настройки Flagent и интеграции"
        else -> "Configure Flagent settings and integrations"
    }
    val generalTab: String get() = when (currentLocale) {
        "ru" -> "Общие"
        else -> "General"
    }
    val multiTenancyTab: String get() = when (currentLocale) {
        "ru" -> "Мультитенантность"
        else -> "Multi-Tenancy"
    }
    val manageTenantsLink: String get() = when (currentLocale) {
        "ru" -> "Управление тенантами"
        else -> "Manage Tenants"
    }
    val ssoProvidersTab: String get() = when (currentLocale) {
        "ru" -> "SSO провайдеры"
        else -> "SSO Providers"
    }
    val slackTab: String get() = when (currentLocale) {
        "ru" -> "Slack"
        else -> "Slack"
    }
    val billingTab: String get() = when (currentLocale) {
        "ru" -> "Биллинг"
        else -> "Billing"
    }
    val generalSettings: String get() = when (currentLocale) {
        "ru" -> "Общие настройки"
        else -> "General Settings"
    }
    val apiBaseUrl: String get() = when (currentLocale) {
        "ru" -> "API Base URL"
        else -> "API Base URL"
    }
    val apiBaseUrlDesc: String get() = when (currentLocale) {
        "ru" -> "Базовый URL API Flagent"
        else -> "Base URL for Flagent API"
    }
    val debugMode: String get() = when (currentLocale) {
        "ru" -> "Режим отладки"
        else -> "Debug Mode"
    }
    val debugModeDesc: String get() = when (currentLocale) {
        "ru" -> "Подробные логи в консоли браузера"
        else -> "Enable verbose logging in browser console"
    }
    val apiTimeout: String get() = when (currentLocale) {
        "ru" -> "Таймаут API"
        else -> "API Timeout"
    }
    val apiTimeoutDesc: String get() = when (currentLocale) {
        "ru" -> "Таймаут запроса в миллисекундах"
        else -> "Request timeout in milliseconds"
    }
    val enabledFeatures: String get() = when (currentLocale) {
        "ru" -> "Включённые функции"
        else -> "Enabled Features"
    }
    val mcpSectionTitle: String get() = when (currentLocale) {
        "ru" -> "MCP (AI-ассистенты)"
        else -> "MCP (AI Assistants)"
    }
    val mcpSectionDescription: String get() = when (currentLocale) {
        "ru" -> "Подключите Cursor, Claude или другой MCP-клиент к этому URL для управления флагами через AI."
        else -> "Connect Cursor, Claude, or another MCP client to this URL to manage flags via AI."
    }
    val mcpCopyUrl: String get() = when (currentLocale) {
        "ru" -> "Копировать URL"
        else -> "Copy URL"
    }
    val mcpDocsLinkText: String get() = when (currentLocale) {
        "ru" -> "Документация MCP"
        else -> "MCP documentation"
    }
    val billingTitle: String get() = when (currentLocale) {
        "ru" -> "Биллинг и подписка"
        else -> "Billing & Subscription"
    }
    val billingSubtitle: String get() = when (currentLocale) {
        "ru" -> "Управление подпиской и оплатой через Stripe"
        else -> "Manage your subscription and billing information via Stripe"
    }
    val openBillingPortal: String get() = when (currentLocale) {
        "ru" -> "Открыть портал биллинга"
        else -> "Open Billing Portal"
    }
    val noActiveSubscription: String get() = when (currentLocale) {
        "ru" -> "Нет активной подписки. Войдите с тенантом для управления биллингом или используйте Stripe Checkout с price ID для апгрейда."
        else -> "No active subscription. Sign in with a tenant account to manage billing, or use Stripe Checkout with a price ID to upgrade."
    }
    val plan: String get() = when (currentLocale) {
        "ru" -> "План"
        else -> "Plan"
    }
    val currentPeriod: String get() = when (currentLocale) {
        "ru" -> "Текущий период"
        else -> "Current period"
    }
    val cancelsAtPeriodEnd: String get() = when (currentLocale) {
        "ru" -> "Отмена в конце периода"
        else -> "Cancels at period end"
    }
    val ssoTitle: String get() = when (currentLocale) {
        "ru" -> "SSO провайдеры"
        else -> "SSO Providers"
    }
    val ssoSubtitle: String get() = when (currentLocale) {
        "ru" -> "Настройка Single Sign-On для аутентификации"
        else -> "Configure Single Sign-On providers for user authentication"
    }
    val noSsoProviders: String get() = when (currentLocale) {
        "ru" -> "Нет настроенных SSO провайдеров. Добавьте провайдера через API или конфиг бэкенда."
        else -> "No SSO providers configured. Add a provider via API or backend config."
    }
    val slackTitle: String get() = when (currentLocale) {
        "ru" -> "Интеграция Slack"
        else -> "Slack Integration"
    }
    val slackSubtitle: String get() = when (currentLocale) {
        "ru" -> "Webhook и канал настраиваются на сервере (FLAGENT_SLACK_*). Здесь можно проверить статус и отправить тестовое уведомление."
        else -> "Webhook and channel are configured on the server (FLAGENT_SLACK_*). Here you can check status and send a test notification."
    }
    val sendTestNotification: String get() = when (currentLocale) {
        "ru" -> "Отправить тестовое уведомление"
        else -> "Send test notification"
    }
    val slackConfigured: String get() = when (currentLocale) {
        "ru" -> "Slack настроен"
        else -> "Slack is configured"
    }
    val slackNotConfigured: String get() = when (currentLocale) {
        "ru" -> "Slack не настроен"
        else -> "Slack is not configured"
    }
    val sending: String get() = when (currentLocale) {
        "ru" -> "Отправка..."
        else -> "Sending..."
    }
    val billingErrorHint: String get() = when (currentLocale) {
        "ru" -> "Убедитесь, что бэкенд запущен и Stripe настроен."
        else -> "Ensure backend is running and Stripe is configured."
    }
    val billingHint404: String get() = when (currentLocale) {
        "ru" -> "Биллинг не настроен на бэкенде (404)."
        else -> "Billing is not configured on the backend (404)."
    }
    val billingHint502: String get() = when (currentLocale) {
        "ru" -> "Stripe временно недоступен (502/503). Попробуйте позже."
        else -> "Stripe is temporarily unavailable (502/503). Try again later."
    }
    val ssoErrorHint: String get() = when (currentLocale) {
        "ru" -> "Провайдеры SSO настраиваются на бэкенде или через API."
        else -> "SSO providers are configured on the backend or via API."
    }
    val ssoHint404: String get() = when (currentLocale) {
        "ru" -> "SSO не настроен на бэкенде (404)."
        else -> "SSO is not configured on the backend (404)."
    }
    val ssoHint502: String get() = when (currentLocale) {
        "ru" -> "Сервис SSO временно недоступен (502/503). Попробуйте позже."
        else -> "SSO service is temporarily unavailable (502/503). Try again later."
    }
    val settingsIntroHint: String get() = when (currentLocale) {
        "ru" -> "Общие настройки, тенанты, SSO, Slack и биллинг (Enterprise)."
        else -> "General settings, tenants, SSO, Slack and billing (Enterprise)."
    }
    val flagsListKeyHint: String get() = when (currentLocale) {
        "ru" -> "Подсказка: ключ флага используется в коде для evaluation."
        else -> "Tip: the flag key is used in code for evaluation."
    }

    // Experiments (A/B)
    val experimentsTitle: String get() = when (currentLocale) {
        "ru" -> "Эксперименты (A/B)"
        else -> "Experiments (A/B)"
    }
    val experimentsSubtitle: String get() = when (currentLocale) {
        "ru" -> "Флаги с двумя и более вариантами для A/B тестов и постепенного rollout"
        else -> "Flags with two or more variants for A/B testing and gradual rollout"
    }
    val segmentsTitle: String get() = when (currentLocale) {
        "ru" -> "Сегменты"
        else -> "Segments"
    }
    val segmentsSubtitle: String get() = when (currentLocale) {
        "ru" -> "Группы сущностей с правилами таргетинга и распределением вариантов по флагам"
        else -> "Entity groups with targeting rules and variant distribution per flag"
    }
    val noExperiments: String get() = when (currentLocale) {
        "ru" -> "Нет экспериментов"
        else -> "No experiments"
    }
    val noExperimentsHint: String get() = when (currentLocale) {
        "ru" -> "Создайте флаг с несколькими вариантами и распределениями, чтобы он появился здесь"
        else -> "Create a flag with multiple variants and distributions to see it here"
    }
    val viewFlags: String get() = when (currentLocale) {
        "ru" -> "К списку флагов"
        else -> "View Flags"
    }
    val viewMetrics: String get() = when (currentLocale) {
        "ru" -> "Метрики"
        else -> "View Metrics"
    }

    // Analytics
    val analyticsTitle: String get() = when (currentLocale) {
        "ru" -> "Аналитика"
        else -> "Analytics"
    }
    val analyticsSubtitle: String get() = when (currentLocale) {
        "ru" -> "Метрики по флагам. Выберите флаг для просмотра графиков и агрегатов"
        else -> "Per-flag metrics. Select a flag to view charts and aggregations"
    }

    val backToAnalytics: String get() = when (currentLocale) {
        "ru" -> "← Назад к аналитике"
        else -> "← Back to Analytics"
    }

    val backToCrash: String get() = when (currentLocale) {
        "ru" -> "← Назад к Crash"
        else -> "← Back to Crash"
    }

    // Deployment mode / Plan (Settings)
    val deploymentModeLabel: String get() = when (currentLocale) {
        "ru" -> "Режим развёртывания"
        else -> "Deployment Mode"
    }
    val selfHostedOpenSource: String get() = when (currentLocale) {
        "ru" -> "Self-hosted Open Source"
        else -> "Self-hosted Open Source"
    }
    val selfHostedEnterprise: String get() = when (currentLocale) {
        "ru" -> "Self-hosted Enterprise"
        else -> "Self-hosted Enterprise"
    }
    val saasEnterprise: String get() = when (currentLocale) {
        "ru" -> "SaaS Enterprise"
        else -> "SaaS Enterprise"
    }
    val saasLowPrice: String get() = when (currentLocale) {
        "ru" -> "SaaS Low-price"
        else -> "SaaS Low-price"
    }
    val quickAccess: String get() = when (currentLocale) {
        "ru" -> "Быстрый доступ"
        else -> "Quick Access"
    }

    val commandBarPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Поиск флагов, навигация..."
        else -> "Search flags, navigate..."
    }

    val commandBarNoResults: String get() = when (currentLocale) {
        "ru" -> "Ничего не найдено"
        else -> "No results found"
    }
    val dashboardOverview: String get() = when (currentLocale) {
        "ru" -> "Обзор флагов и статуса системы"
        else -> "Overview of your feature flags and system status"
    }
    val evaluationsOverTime: String get() = when (currentLocale) {
        "ru" -> "Оценки за период"
        else -> "Evaluations over time"
    }
    val totalEvaluationsLabel: String get() = when (currentLocale) {
        "ru" -> "Всего оценок"
        else -> "Total evaluations"
    }
    val uniqueFlagsLabel: String get() = when (currentLocale) {
        "ru" -> "Уникальных флагов"
        else -> "Unique flags"
    }
    val byFlagsTab: String get() = when (currentLocale) {
        "ru" -> "По флагам"
        else -> "By flags"
    }
    val totalEventsLabel: String get() = when (currentLocale) {
        "ru" -> "Всего событий"
        else -> "Total events"
    }
    val uniqueUsersLabel: String get() = when (currentLocale) {
        "ru" -> "Уникальных пользователей"
        else -> "Unique users"
    }
    val overviewTab: String get() = when (currentLocale) {
        "ru" -> "Обзор"
        else -> "Overview"
    }
    val eventsTab: String get() = when (currentLocale) {
        "ru" -> "События"
        else -> "Events"
    }
    val eventsOverTime: String get() = when (currentLocale) {
        "ru" -> "События за период"
        else -> "Events over time"
    }
    val topFlagsByEvaluations: String get() = when (currentLocale) {
        "ru" -> "Топ флагов по оценкам"
        else -> "Top flags by evaluations"
    }
    val noMetricsData: String get() = when (currentLocale) {
        "ru" -> "Нет данных метрик за выбранный период"
        else -> "No metrics data for the selected period"
    }
    val metricsUnavailable: String get() = when (currentLocale) {
        "ru" -> "Метрики временно недоступны"
        else -> "Metrics temporarily unavailable"
    }
    val activityTimeline: String get() = when (currentLocale) {
        "ru" -> "Хронология изменений"
        else -> "Activity Timeline"
    }
    val recentlyUpdatedFlags: String get() = when (currentLocale) {
        "ru" -> "Недавно изменённые флаги"
        else -> "Recently updated flags"
    }
    val topFlags: String get() = when (currentLocale) {
        "ru" -> "Топ флагов"
        else -> "Top Flags"
    }
    val healthStatus: String get() = when (currentLocale) {
        "ru" -> "Статус системы"
        else -> "Health Status"
    }
    val healthStatusOk: String get() = when (currentLocale) {
        "ru" -> "Система работает нормально"
        else -> "System operational"
    }
    val flagActivityOverPeriod: String get() = when (currentLocale) {
        "ru" -> "Активность флагов за период"
        else -> "Flag activity over period"
    }
    val statusDistribution: String get() = when (currentLocale) {
        "ru" -> "Распределение по статусам"
        else -> "Status distribution"
    }
    val evaluationsTrend: String get() = when (currentLocale) {
        "ru" -> "Тренд оценок"
        else -> "Evaluations trend"
    }
    val today: String get() = when (currentLocale) {
        "ru" -> "Сегодня"
        else -> "Today"
    }
    val week: String get() = when (currentLocale) {
        "ru" -> "Неделя"
        else -> "Week"
    }
    val period1h: String get() = when (currentLocale) {
        "ru" -> "1 ч"
        else -> "1h"
    }
    val period24h: String get() = when (currentLocale) {
        "ru" -> "24 ч"
        else -> "24h"
    }
    val period7d: String get() = when (currentLocale) {
        "ru" -> "7 дн."
        else -> "7d"
    }
    val month: String get() = when (currentLocale) {
        "ru" -> "Месяц"
        else -> "Month"
    }
    val exportCsv: String get() = when (currentLocale) {
        "ru" -> "Экспорт CSV"
        else -> "Export CSV"
    }
    val exportJson: String get() = when (currentLocale) {
        "ru" -> "Экспорт JSON"
        else -> "Export JSON"
    }
    val compareWithPreviousPeriod: String get() = when (currentLocale) {
        "ru" -> "Сравнить с предыдущим периодом"
        else -> "Compare with previous period"
    }
    val evaluations: String get() = when (currentLocale) {
        "ru" -> "оценок"
        else -> "evaluations"
    }
    val crashReportsCount: String get() = when (currentLocale) {
        "ru" -> "Отчётов о крашах"
        else -> "Crash reports"
    }
    val filterByTime: String get() = when (currentLocale) {
        "ru" -> "Период"
        else -> "Time range"
    }
    val stackTrace: String get() = when (currentLocale) {
        "ru" -> "Стек вызовов"
        else -> "Stack trace"
    }
    val groupByType: String get() = when (currentLocale) {
        "ru" -> "Группировать по типу"
        else -> "Group by type"
    }
    val noMessage: String get() = when (currentLocale) {
        "ru" -> "Нет сообщения"
        else -> "No message"
    }
    val other: String get() = when (currentLocale) {
        "ru" -> "Другое"
        else -> "Other"
    }
    val activeFlags: String get() = when (currentLocale) {
        "ru" -> "Активные флаги"
        else -> "Active flags"
    }
    val cardsView: String get() = when (currentLocale) {
        "ru" -> "Карточки"
        else -> "Cards"
    }
    val tableView: String get() = when (currentLocale) {
        "ru" -> "Таблица"
        else -> "Table"
    }
    val experimentsCount: String get() = when (currentLocale) {
        "ru" -> "экспериментов"
        else -> "experiments"
    }
    val noExperimentsMatchFilter: String get() = when (currentLocale) {
        "ru" -> "Нет экспериментов по выбранному фильтру"
        else -> "No experiments match the selected filter"
    }
    val noExperimentsChangeFilterHint: String get() = when (currentLocale) {
        "ru" -> "Выберите другой фильтр выше (Все / Включённые / Выключенные)"
        else -> "Select a different filter above (All / Enabled / Disabled)"
    }
    val filterAll: String get() = when (currentLocale) {
        "ru" -> "Все"
        else -> "All"
    }
    val clearSearch: String get() = when (currentLocale) {
        "ru" -> "Сбросить поиск"
        else -> "Clear search"
    }
    val statusWithSegments: String get() = when (currentLocale) {
        "ru" -> "С сегментами"
        else -> "With segments"
    }
    val statusExperiments: String get() = when (currentLocale) {
        "ru" -> "Эксперименты"
        else -> "Experiments"
    }
    val statusEnabledLabel: String get() = when (currentLocale) {
        "ru" -> "Включены"
        else -> "Enabled"
    }
    val statusDisabledLabel: String get() = when (currentLocale) {
        "ru" -> "Выключены"
        else -> "Disabled"
    }
    val noFlagsDescription: String get() = when (currentLocale) {
        "ru" -> "Создайте флаги для отслеживания крашей"
        else -> "Create flags to track crash rates"
    }

    // Navigation labels (sidebar/navbar)
    val dashboardNav: String get() = when (currentLocale) {
        "ru" -> "Дашборд"
        else -> "Dashboard"
    }
    val flagsNav: String get() = when (currentLocale) {
        "ru" -> "Флаги"
        else -> "Flags"
    }
    val experimentsNav: String get() = when (currentLocale) {
        "ru" -> "Эксперименты"
        else -> "Experiments"
    }
    val analyticsNav: String get() = when (currentLocale) {
        "ru" -> "Аналитика"
        else -> "Analytics"
    }
    val crashNav: String get() = when (currentLocale) {
        "ru" -> "Краши"
        else -> "Crash"
    }
    val alertsNav: String get() = when (currentLocale) {
        "ru" -> "Оповещения"
        else -> "Alerts"
    }
    val tenantsNav: String get() = when (currentLocale) {
        "ru" -> "Тенанты"
        else -> "Tenants"
    }
    val settingsNav: String get() = when (currentLocale) {
        "ru" -> "Настройки"
        else -> "Settings"
    }
    val loginButton: String get() = when (currentLocale) {
        "ru" -> "Войти"
        else -> "Login"
    }
    val logoutButton: String get() = when (currentLocale) {
        "ru" -> "Выйти"
        else -> "Logout"
    }
    val userFallback: String get() = when (currentLocale) {
        "ru" -> "Пользователь"
        else -> "User"
    }
    val blog: String get() = when (currentLocale) {
        "ru" -> "Блог"
        else -> "Blog"
    }
    val github: String get() = when (currentLocale) {
        "ru" -> "GitHub"
        else -> "GitHub"
    }

    // Dashboard stats
    val totalFlagsStat: String get() = when (currentLocale) {
        "ru" -> "Всего флагов"
        else -> "Total Flags"
    }
    val withSegmentsStat: String get() = when (currentLocale) {
        "ru" -> "С сегментами"
        else -> "With Segments"
    }
    val unresolvedAlerts: String get() = when (currentLocale) {
        "ru" -> "Неразрешённые оповещения"
        else -> "Unresolved Alerts"
    }
    val viewAll: String get() = when (currentLocale) {
        "ru" -> "Смотреть все"
        else -> "View All"
    }
    val viewAllFlags: String get() = when (currentLocale) {
        "ru" -> "Все флаги"
        else -> "All flags"
    }
    val createFirstTenant: String get() = when (currentLocale) {
        "ru" -> "Создать первого тенанта →"
        else -> "Create first tenant →"
    }
    val logInAdmin: String get() = when (currentLocale) {
        "ru" -> "Войти (админ) →"
        else -> "Log in (admin) →"
    }

    // Auth / Login
    val signIn: String get() = when (currentLocale) {
        "ru" -> "Войти"
        else -> "Sign In"
    }
    val signingIn: String get() = when (currentLocale) {
        "ru" -> "Вход..."
        else -> "Signing in..."
    }
    val welcomeToFlagent: String get() = when (currentLocale) {
        "ru" -> "Добро пожаловать в Flagent"
        else -> "Welcome to Flagent"
    }
    val signInToManage: String get() = when (currentLocale) {
        "ru" -> "Войдите для управления feature flags"
        else -> "Sign in to manage your feature flags"
    }
    val emailLabel: String get() = when (currentLocale) {
        "ru" -> "Email"
        else -> "Email"
    }
    val passwordLabel: String get() = when (currentLocale) {
        "ru" -> "Пароль"
        else -> "Password"
    }
    val rememberMeLabel: String get() = when (currentLocale) {
        "ru" -> "Запомнить меня"
        else -> "Remember me"
    }
    val orSignInWithSso: String get() = when (currentLocale) {
        "ru" -> "Или войдите через SSO"
        else -> "Or sign in with SSO"
    }
    val loginWithSso: String get() = when (currentLocale) {
        "ru" -> "Войти через SSO"
        else -> "Login with SSO"
    }
    val supportLink: String get() = when (currentLocale) {
        "ru" -> "Поддержка"
        else -> "Support"
    }
    val questionsAndGuides: String get() = when (currentLocale) {
        "ru" -> "Вопросы и гайды"
        else -> "Questions & Guides"
    }
    val documentation: String get() = when (currentLocale) {
        "ru" -> "Документация"
        else -> "Documentation"
    }

    // Tenants
    val createTenant: String get() = when (currentLocale) {
        "ru" -> "Создать тенанта"
        else -> "Create Tenant"
    }
    val keyUniqueIdentifier: String get() = when (currentLocale) {
        "ru" -> "Ключ (уникальный идентификатор)"
        else -> "Key (unique identifier)"
    }
    val nameLabel: String get() = when (currentLocale) {
        "ru" -> "Название"
        else -> "Name"
    }
    val ownerEmail: String get() = when (currentLocale) {
        "ru" -> "Email владельца"
        else -> "Owner Email"
    }
    val switchTenant: String get() = when (currentLocale) {
        "ru" -> "Переключить"
        else -> "Switch"
    }
    val allPlans: String get() = when (currentLocale) {
        "ru" -> "Все планы"
        else -> "All plans"
    }
    val allStatuses: String get() = when (currentLocale) {
        "ru" -> "Все статусы"
        else -> "All statuses"
    }
    val noGrouping: String get() = when (currentLocale) {
        "ru" -> "Без группировки"
        else -> "No grouping"
    }
    val groupByPlan: String get() = when (currentLocale) {
        "ru" -> "По плану"
        else -> "Group by plan"
    }
    val groupByStatus: String get() = when (currentLocale) {
        "ru" -> "По статусу"
        else -> "Group by status"
    }
    val actionsLabel: String get() = when (currentLocale) {
        "ru" -> "Действия"
        else -> "Actions"
    }

    // Pagination (override if different from previous/next)
    val previousLabel: String get() = when (currentLocale) {
        "ru" -> "Назад"
        else -> "Previous"
    }
    val nextLabel: String get() = when (currentLocale) {
        "ru" -> "Вперёд"
        else -> "Next"
    }

    // Crash / Analytics / Alerts / Experiments
    val crashAnalytics: String get() = when (currentLocale) {
        "ru" -> "Аналитика крашей"
        else -> "Crash Analytics"
    }
    val crashAnalyticsDescription: String get() = when (currentLocale) {
        "ru" -> "Отчёты о крашах из SDK и частота крашей по флагам. Интеграция с обнаружением аномалий и Smart Rollout."
        else -> "Track crash reports from SDK and crash rate per flag. Integrates with Anomaly Detection and Smart Rollout."
    }
    val crashReports: String get() = when (currentLocale) {
        "ru" -> "Отчёты о крашах"
        else -> "Crash reports"
    }
    val byFlagsCrashRate: String get() = when (currentLocale) {
        "ru" -> "По флагам (CRASH_RATE)"
        else -> "By flags (CRASH_RATE)"
    }
    val ensureApiKeySet: String get() = when (currentLocale) {
        "ru" -> "Убедитесь, что задан X-API-Key (Настройки или после создания тенанта)."
        else -> "Ensure X-API-Key is set (Settings or after creating a tenant)."
    }
    val retry: String get() = when (currentLocale) {
        "ru" -> "Повторить"
        else -> "Retry"
    }
    val viewCrashRate: String get() = when (currentLocale) {
        "ru" -> "Смотреть CRASH_RATE →"
        else -> "View CRASH_RATE →"
    }
    val copyLabel: String get() = when (currentLocale) {
        "ru" -> "Копировать"
        else -> "Copy"
    }
    val anomalyAlerts: String get() = when (currentLocale) {
        "ru" -> "Оповещения об аномалиях"
        else -> "Anomaly Alerts"
    }
    val resolved: String get() = when (currentLocale) {
        "ru" -> "Разрешено"
        else -> "Resolved"
    }
    val resolve: String get() = when (currentLocale) {
        "ru" -> "Разрешить"
        else -> "Resolve"
    }
    val abStatistics: String get() = when (currentLocale) {
        "ru" -> "A/B статистика"
        else -> "A/B Statistics"
    }
    val noConversionData: String get() = when (currentLocale) {
        "ru" -> "Нет данных конверсии. Отправьте метрики CONVERSION_RATE с variantId для отображения A/B статистики."
        else -> "No conversion data. Send CONVERSION_RATE metrics with variantId to see A/B stats."
    }
    val variantLabel: String get() = when (currentLocale) {
        "ru" -> "Вариант"
        else -> "Variant"
    }
    val sample: String get() = when (currentLocale) {
        "ru" -> "Выборка"
        else -> "Sample"
    }
    val conversions: String get() = when (currentLocale) {
        "ru" -> "Конверсии"
        else -> "Conversions"
    }
    val rate: String get() = when (currentLocale) {
        "ru" -> "Доля"
        else -> "Rate"
    }
    val dauByDay: String get() = when (currentLocale) {
        "ru" -> "DAU по дням"
        else -> "DAU by day"
    }
    val dailyActiveUsers: String get() = when (currentLocale) {
        "ru" -> "Активные пользователи по дням"
        else -> "Daily active users"
    }
    val topEvents: String get() = when (currentLocale) {
        "ru" -> "Топ событий"
        else -> "Top events"
    }
    val noAnalyticsEventsYet: String get() = when (currentLocale) {
        "ru" -> "Событий аналитики пока нет. Используйте logEvent() в SDK для отправки событий."
        else -> "No analytics events yet. Use SDK logEvent() to send events."
    }
    val allMetrics: String get() = when (currentLocale) {
        "ru" -> "Все метрики"
        else -> "All Metrics"
    }
    val lastHour: String get() = when (currentLocale) {
        "ru" -> "Последний час"
        else -> "Last Hour"
    }
    val last24Hours: String get() = when (currentLocale) {
        "ru" -> "Последние 24 часа"
        else -> "Last 24 Hours"
    }
    val anomalyDetectionNotEnabled: String get() = when (currentLocale) {
        "ru" -> "Функция обнаружения аномалий не включена"
        else -> "Anomaly Detection feature is not enabled"
    }
    val monitorAndResolveAlerts: String get() = when (currentLocale) {
        "ru" -> "Мониторинг и разрешение оповещений по всем флагам"
        else -> "Monitor and resolve anomaly alerts across all flags"
    }
    val noUnresolvedAlerts: String get() = when (currentLocale) {
        "ru" -> "Нет неразрешённых оповещений"
        else -> "No unresolved alerts"
    }
    val allAnomaliesResolved: String get() = when (currentLocale) {
        "ru" -> "Все аномалии разрешены"
        else -> "All anomalies have been resolved"
    }
    fun metricDetected(metricType: String, detectedAt: String): String = when (currentLocale) {
        "ru" -> "Метрика: $metricType • Обнаружено: $detectedAt"
        else -> "Metric: $metricType • Detected: $detectedAt"
    }
    val importFlags: String get() = when (currentLocale) {
        "ru" -> "Импорт флагов"
        else -> "Import Flags"
    }
    val importFlagsPasteHint: String get() = when (currentLocale) {
        "ru" -> "Вставьте YAML или JSON в формате GitOps (из Экспорта). Существующие флаги с совпадающими ключами будут обновлены."
        else -> "Paste YAML or JSON in GitOps format (from Export). Existing flags with matching keys will be updated."
    }
    val formatLabel: String get() = when (currentLocale) {
        "ru" -> "Формат:"
        else -> "Format:"
    }
    val importComplete: String get() = when (currentLocale) {
        "ru" -> "Импорт завершён"
        else -> "Import complete"
    }
    fun importCreatedUpdated(created: Int, updated: Int): String = when (currentLocale) {
        "ru" -> "Создано: $created, Обновлено: $updated"
        else -> "Created: $created, Updated: $updated"
    }
    val errorsLabel: String get() = when (currentLocale) {
        "ru" -> "Ошибки:"
        else -> "Errors:"
    }
    val webhooks: String get() = when (currentLocale) {
        "ru" -> "Webhooks"
        else -> "Webhooks"
    }
    val webhooksDescription: String get() = when (currentLocale) {
        "ru" -> "Уведомление внешних систем при изменении флагов. Настройте URL для получения событий (flag.created, flag.updated и т.д.) с опциональной HMAC подписью."
        else -> "Notify external systems when flags change. Configure URLs to receive events (flag.created, flag.updated, etc.) with optional HMAC signature."
    }
    val noWebhooksConfigured: String get() = when (currentLocale) {
        "ru" -> "Webhooks не настроены. Добавьте URL для получения уведомлений об изменениях флагов."
        else -> "No webhooks configured. Add one to receive flag change notifications."
    }
    val urlLabel: String get() = when (currentLocale) {
        "ru" -> "URL"
        else -> "URL"
    }
    val eventsLabel: String get() = when (currentLocale) {
        "ru" -> "События"
        else -> "Events"
    }
    val secretOptionalHmac: String get() = when (currentLocale) {
        "ru" -> "Секрет (опционально, для HMAC)"
        else -> "Secret (optional, for HMAC)"
    }
    val rolesAndPermissions: String get() = when (currentLocale) {
        "ru" -> "Роли и права"
        else -> "Roles & Permissions"
    }
    val manageRolesDescription: String get() = when (currentLocale) {
        "ru" -> "Управление ролями и назначение прав пользователям."
        else -> "Manage roles and assign permissions to users."
    }
    val availableRoles: String get() = when (currentLocale) {
        "ru" -> "Доступные роли"
        else -> "Available Roles"
    }
    val builtIn: String get() = when (currentLocale) {
        "ru" -> "Встроенная"
        else -> "Built-in"
    }
    val usersLabel: String get() = when (currentLocale) {
        "ru" -> "Пользователи"
        else -> "Users"
    }
    fun roleLabel(role: String): String = when (currentLocale) {
        "ru" -> "Роль: $role"
        else -> "Role: $role"
    }
    val adminUsersTab: String get() = when (currentLocale) {
        "ru" -> "Пользователи"
        else -> "Users"
    }
    val adminUsersDescription: String get() = when (currentLocale) {
        "ru" -> "Управление учётными записями администраторов (вход в UI)."
        else -> "Manage admin user accounts (UI login)."
    }
    val addUser: String get() = when (currentLocale) {
        "ru" -> "Добавить пользователя"
        else -> "Add user"
    }
    val editUser: String get() = when (currentLocale) {
        "ru" -> "Редактировать"
        else -> "Edit"
    }
    val blockUser: String get() = when (currentLocale) {
        "ru" -> "Заблокировать"
        else -> "Block"
    }
    val unblockUser: String get() = when (currentLocale) {
        "ru" -> "Разблокировать"
        else -> "Unblock"
    }
    val deleteUser: String get() = when (currentLocale) {
        "ru" -> "Удалить"
        else -> "Delete"
    }
    val confirmBlockUserTitle: String get() = when (currentLocale) {
        "ru" -> "Заблокировать пользователя?"
        else -> "Block user?"
    }
    val confirmBlockUserMessage: String get() = when (currentLocale) {
        "ru" -> "Пользователь не сможет войти в систему до разблокировки."
        else -> "The user will not be able to log in until unblocked."
    }
    val confirmUnblockUserTitle: String get() = when (currentLocale) {
        "ru" -> "Разблокировать пользователя?"
        else -> "Unblock user?"
    }
    val confirmDeleteUserTitle: String get() = when (currentLocale) {
        "ru" -> "Удалить пользователя?"
        else -> "Delete user?"
    }
    val confirmDeleteUserMessage: String get() = when (currentLocale) {
        "ru" -> "Учётная запись будет удалена. Восстановление невозможно."
        else -> "The account will be removed. This cannot be undone."
    }
    val userStatusActive: String get() = when (currentLocale) {
        "ru" -> "Активен"
        else -> "Active"
    }
    val userStatusBlocked: String get() = when (currentLocale) {
        "ru" -> "Заблокирован"
        else -> "Blocked"
    }
    val emailPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Email"
        else -> "Email"
    }
    val passwordPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Пароль"
        else -> "Password"
    }
    val namePlaceholder: String get() = when (currentLocale) {
        "ru" -> "Имя (опционально)"
        else -> "Name (optional)"
    }
    val newPasswordPlaceholder: String get() = when (currentLocale) {
        "ru" -> "Новый пароль (оставьте пустым, чтобы не менять)"
        else -> "New password (leave blank to keep)"
    }
    val exportData: String get() = when (currentLocale) {
        "ru" -> "Экспорт данных"
        else -> "Export Data"
    }
    val excludeSnapshots: String get() = when (currentLocale) {
        "ru" -> "Исключить снапшоты (уменьшить размер файла)"
        else -> "Exclude snapshots (reduce file size)"
    }
    val subscription: String get() = when (currentLocale) {
        "ru" -> "Подписка"
        else -> "Subscription"
    }
    val smartRolloutAi: String get() = when (currentLocale) {
        "ru" -> "Smart Rollout (на базе ИИ)"
        else -> "Smart Rollout (AI-Powered)"
    }
    fun segmentIdLabel(id: Int): String = when (currentLocale) {
        "ru" -> "Сегмент #$id"
        else -> "Segment #$id"
    }
    fun targetCurrentRollout(target: Int, current: Int): String = when (currentLocale) {
        "ru" -> "Цель: ${target}% | Текущий: ${current}%"
        else -> "Target: ${target}% | Current: ${current}%"
    }
    val executeRolloutStep: String get() = when (currentLocale) {
        "ru" -> "Выполнить шаг rollout"
        else -> "Execute Rollout Step"
    }
    val comingSoon: String get() = when (currentLocale) {
        "ru" -> "Скоро. Следите за новостями Flagent, гайдами и лучшими практиками."
        else -> "Coming soon. Stay updated with Flagent news, tutorials, and best practices."
    }
    val goToGitHubDiscussions: String get() = when (currentLocale) {
        "ru" -> "Перейти в GitHub Discussions"
        else -> "Go to GitHub Discussions"
    }
    val backToHome: String get() = when (currentLocale) {
        "ru" -> "На главную"
        else -> "Back to Home"
    }
    val ssoProviders: String get() = when (currentLocale) {
        "ru" -> "SSO провайдеры"
        else -> "SSO Providers"
    }
    val signInShort: String get() = when (currentLocale) {
        "ru" -> "Войти"
        else -> "Sign in"
    }
    val getStarted: String get() = when (currentLocale) {
        "ru" -> "Начать"
        else -> "Get Started"
    }
    val subscribe: String get() = when (currentLocale) {
        "ru" -> "Подписаться"
        else -> "Subscribe"
    }
    val newsletter: String get() = when (currentLocale) {
        "ru" -> "Рассылка"
        else -> "Newsletter"
    }
    val poweredByOpenSource: String get() = when (currentLocale) {
        "ru" -> "На базе открытого кода"
        else -> "Powered by open source"
    }
    val noTenantYet: String get() = when (currentLocale) {
        "ru" -> "Тенанта ещё нет? "
        else -> "No tenant yet? "
    }
    val createYourFirstTenant: String get() = when (currentLocale) {
        "ru" -> "Создать первого тенанта"
        else -> "Create your first tenant"
    }
    val toGetStarted: String get() = when (currentLocale) {
        "ru" -> " чтобы начать."
        else -> " to get started."
    }
    val metricsTab: String get() = when (currentLocale) {
        "ru" -> "Метрики"
        else -> "Metrics"
    }
    val noTenants: String get() = when (currentLocale) {
        "ru" -> "Нет тенантов"
        else -> "No tenants"
    }
    val createFirstTenantDescription: String get() = when (currentLocale) {
        "ru" -> "Создайте первого тенанта для начала работы"
        else -> "Create your first tenant to get started"
    }
    val selectTenantToContinue: String get() = when (currentLocale) {
        "ru" -> "Выберите тенанта ниже, чтобы продолжить, или создайте нового."
        else -> "Select a tenant below to continue, or create a new one."
    }
    val tenantApiKeyOnlyInThisBrowser: String get() = when (currentLocale) {
        "ru" -> "API‑ключ сохраняется только для тенантов, созданных в этом браузере."
        else -> "Only tenants created in this browser have an API key saved."
    }
    val apiKeyNotAvailableForTenant: String get() = when (currentLocale) {
        "ru" -> "API‑ключ этого тенанта не сохранён в этом браузере. Создайте нового тенанта или выберите тенанта, созданного здесь."
        else -> "API key for this tenant is not stored in this browser. Create a new tenant or use one you created here."
    }
    val useApiKeyFromAnotherDevice: String get() = when (currentLocale) {
        "ru" -> "Войти по API‑ключу с другого устройства"
        else -> "Use API key from another device"
    }
    val useApiKeyFromAnotherDeviceDescription: String get() = when (currentLocale) {
        "ru" -> "Если вы создали тенанта в другом браузере или сохранили ключ — вставьте его ниже."
        else -> "If you created a tenant in another browser or have the API key saved — paste it below."
    }
    val useThisKey: String get() = when (currentLocale) {
        "ru" -> "Использовать этот ключ"
        else -> "Use this key"
    }
    val apiKeyPastePlaceholder: String get() = when (currentLocale) {
        "ru" -> "Вставьте API‑ключ тенанта"
        else -> "Paste tenant API key"
    }
    val invalidApiKeyOrNotEnterprise: String get() = when (currentLocale) {
        "ru" -> "Неверный ключ или функция недоступна (нужен Flagent Enterprise)."
        else -> "Invalid API key or feature not available (Flagent Enterprise required)."
    }
    val tenantCreatedSaveKey: String get() = when (currentLocale) {
        "ru" -> "Тенант создан. Сохраните API‑ключ сейчас — позже он не будет показан снова."
        else -> "Tenant created. Save your API key now — it won't be shown again."
    }
    val apiKeyUseHint: String get() = when (currentLocale) {
        "ru" -> "Используйте ключ в приложении (заголовок X-API-Key) или вставьте его на странице «Тенанты» в другом браузере."
        else -> "Use this key in your app (X-API-Key header) or paste it on the Tenants page in another browser."
    }
    val copyApiKey: String get() = when (currentLocale) {
        "ru" -> "Копировать ключ"
        else -> "Copy key"
    }
    val copiedToClipboard: String get() = when (currentLocale) {
        "ru" -> "Скопировано"
        else -> "Copied!"
    }
    val continueToDashboard: String get() = when (currentLocale) {
        "ru" -> "Перейти в дашборд"
        else -> "Continue to Dashboard"
    }
    val afterCreateYouWillGetApiKey: String get() = when (currentLocale) {
        "ru" -> "После создания вы получите API‑ключ. Сохраните его — он понадобится для SDK и входа с другого устройства."
        else -> "After creation you'll get an API key. Save it — you'll need it for SDK config and to sign in from another device."
    }
    val createApiKey: String get() = when (currentLocale) {
        "ru" -> "Выдать ключ"
        else -> "Create API key"
    }
    val createApiKeyForTenant: String get() = when (currentLocale) {
        "ru" -> "Новый API‑ключ для тенанта"
        else -> "New API key for tenant"
    }
    val createApiKeyNamePlaceholder: String get() = when (currentLocale) {
        "ru" -> "Например: Recovery, Production"
        else -> "e.g. Recovery, Production"
    }
    val newKeyCreatedSaveNow: String get() = when (currentLocale) {
        "ru" -> "Новый ключ создан. Сохраните его — он больше не будет показан."
        else -> "New key created. Save it now — it won't be shown again."
    }
    val useThisKeyInThisBrowser: String get() = when (currentLocale) {
        "ru" -> "Использовать в этом браузере"
        else -> "Use in this browser"
    }
    val closeButton: String get() = when (currentLocale) {
        "ru" -> "Закрыть"
        else -> "Close"
    }
    val noTenantsMatchFilters: String get() = when (currentLocale) {
        "ru" -> "Нет тенантов по фильтрам"
        else -> "No tenants match filters"
    }
    val tryAdjustingFilters: String get() = when (currentLocale) {
        "ru" -> "Измените поиск или фильтры"
        else -> "Try adjusting search or filters"
    }
    val clearFilters: String get() = when (currentLocale) {
        "ru" -> "Сбросить фильтры"
        else -> "Clear filters"
    }
    val searchByNameOrKey: String get() = when (currentLocale) {
        "ru" -> "Поиск по названию или ключу"
        else -> "Search by name or key"
    }
    val keyColumn: String get() = when (currentLocale) {
        "ru" -> "Ключ"
        else -> "Key"
    }
}
