# Архитектура Frontend

## Обзор

Frontend будет переписан на Kotlin/JS или другой современный фреймворк.

## Варианты реализации

### Вариант 1: Kotlin/JS с React
- Использовать Kotlin/JS для компиляции в JavaScript
- React для UI компонентов
- Совместное использование типов с backend

### Вариант 2: Kotlin/JS с Compose for Web
- Jetpack Compose for Web
- Полностью Kotlin экосистема

### Вариант 3: Оставить Vue.js
- Адаптировать под новый API
- Минимальные изменения в UI

## Компоненты для миграции

1. **Flags List** - список всех флагов
2. **Flag Editor** - создание/редактирование флага
3. **Segment Editor** - управление сегментами
4. **Constraint Editor** - создание ограничений
5. **Distribution Editor** - управление распределениями
6. **Debug Console** - консоль для тестирования evaluation
7. **Flag History** - история изменений флага

## API Integration

Все запросы к API через HTTP клиент:
- `GET /api/v1/flags` - список флагов
- `POST /api/v1/flags` - создание флага
- `POST /api/v1/evaluation` - оценка флага
- И т.д.
