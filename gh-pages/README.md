# Flagent GitHub Pages

Этот каталог содержит landing page для Flagent, развернутый на GitHub Pages.

## Структура

- `index.html` - главная страница
- `styles.css` - стили
- `script.js` - интерактивность и анимации
- `.nojekyll` - отключает обработку Jekyll для GitHub Pages

## Настройка GitHub Pages

1. В настройках репозитория перейдите в **Settings → Pages**
2. Выберите **Source**: Deploy from a branch
3. Выберите **Branch**: `main` (или нужную ветку)
4. Выберите **Folder**: `/gh-pages`

Сайт будет доступен по адресу: `https://<username>.github.io/flagent/`

## Локальный просмотр

Для локального просмотра можно использовать любой простой HTTP сервер:

```bash
cd gh-pages
python3 -m http.server 8000
# или
npx serve .
```

Затем откройте в браузере: `http://localhost:8000`
