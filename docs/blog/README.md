# Blog

Static blog for GitHub Pages. One listing page + one HTML file per post (and per language if needed).

## Adding a new post

1. **Create the post file(s)**  
   - `YYYY-MM-DD-slug-en.html` (English) and/or `YYYY-MM-DD-slug-ru.html` (Russian).  
   - Copy structure from `2026-02-15-intro-en.html`: same nav/footer, `article-page`, `article-header`, `article-body`, back-link to `index.html`.

2. **Add a card to `index.html`**  
   In the `<ul class="post-list">` block, add:

   ```html
   <li class="post-card">
       <a href="YYYY-MM-DD-slug-en.html">
           <div class="post-meta">DD Mon YYYY</div>
           <h2>Post title</h2>
           <p class="post-excerpt">Short excerpt…</p>
           <div class="post-langs"><a href="YYYY-MM-DD-slug-en.html">English</a> · <a href="YYYY-MM-DD-slug-ru.html">Русский</a></div>
       </a>
   </li>
   ```

   Put the newest post at the **top** of the list.

3. **Optional:** Add cross-link between EN/RU in the article meta (e.g. "English" / "Русский" link in the header of each post).

No build step: just HTML. After push to `main`, GitHub Actions will deploy `docs/` (including `blog/`) to Pages.
