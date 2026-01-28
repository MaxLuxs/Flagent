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
        "ru" -> "Поиск по ID, ключу, описанию или тегам..."
        else -> "Search flags by ID, key, description, or tags..."
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
    
    val showDeletedFlags: String get() = when (currentLocale) {
        "ru" -> "Показать удаленные флаги"
        else -> "Show Deleted Flags"
    }
    
    val hideDeletedFlags: String get() = when (currentLocale) {
        "ru" -> "Скрыть удаленные флаги"
        else -> "Hide Deleted Flags"
    }
    
    val deletedFlags: String get() = when (currentLocale) {
        "ru" -> "Удаленные флаги"
        else -> "Deleted Flags"
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
        "ru" -> "Удаленных флагов нет"
        else -> "No deleted flags"
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
    val dashboardOverview: String get() = when (currentLocale) {
        "ru" -> "Обзор флагов и статуса системы"
        else -> "Overview of your feature flags and system status"
    }
    val evaluationsOverTime: String get() = when (currentLocale) {
        "ru" -> "Оценки за период"
        else -> "Evaluations over time"
    }
    val topFlagsByEvaluations: String get() = when (currentLocale) {
        "ru" -> "Топ флагов по оценкам"
        else -> "Top flags by evaluations"
    }
    val noMetricsData: String get() = when (currentLocale) {
        "ru" -> "Нет данных метрик за выбранный период"
        else -> "No metrics data for the selected period"
    }
}
