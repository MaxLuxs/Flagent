# Безопасная настройка Flagent-Enterprise (submodule)

Код enterprise уже закоммичен в **локальном** репо `/tmp/flagent-enterprise-push`. Осталось запушить его в GitHub и заменить папку на submodule.

## Шаг 1. Запушить код в Flagent-Enterprise (один раз)

В терминале (нужна авторизация GitHub: SSH ключ или token):

```bash
cd /tmp/flagent-enterprise-push
git push -u origin main
```

Если ремоут не добавлен:
```bash
git remote add origin https://github.com/MaxLuxs/Flagent-Enterprise.git
# или по SSH: git remote add origin git@github.com:MaxLuxs/Flagent-Enterprise.git
git push -u origin main
```

Проверь: https://github.com/MaxLuxs/Flagent-Enterprise — в репо должны появиться файлы.

---

## Шаг 2. В основном репо заменить папку на submodule

Из корня репо **flagent/** (не Flagent):

```bash
cd /Users/maxluxs/pet/Flagent/flagent

# Убрать папку из индекса и удалить (код уже в Flagent-Enterprise)
git rm -r --cached internal/flagent-enterprise
rm -rf internal/flagent-enterprise

# Подключить submodule
git submodule add https://github.com/MaxLuxs/Flagent-Enterprise.git internal/flagent-enterprise
# или по SSH: git submodule add git@github.com:MaxLuxs/Flagent-Enterprise.git internal/flagent-enterprise
```

---

## Шаг 3. Закоммитить всё в основном репо

```bash
git add .gitmodules internal/flagent-enterprise
git add .github/workflows/ci.yml CHANGELOG.md README.md backend/build.gradle.kts backend/src/main/kotlin/flagent/application/Application.kt backend/src/main/kotlin/flagent/application/CoreDependenciesImpl.kt backend/src/main/kotlin/flagent/application/DefaultEnterpriseConfigurator.kt backend/src/main/kotlin/flagent/application/EnterpriseBackendContextImpl.kt backend/src/main/kotlin/flagent/repository/Database.kt internal/README.md settings.gradle.kts shared/build.gradle.kts shared/src/jvmMain/
git status
git commit -m "feat(enterprise): contract, optional module, Flagent-Enterprise as submodule

- Shared: EnterpriseConfigurator.configureRoutes, EnterpriseBackendContext schema methods
- Backend: Database tenant schema helpers, EnterpriseBackendContextImpl, DefaultEnterpriseConfigurator
- Application: tenant/billing/SSO from core when enterprise absent, configureRoutes() when present
- internal/flagent-enterprise as submodule (Flagent-Enterprise)
- CI, README, CHANGELOG updated"
git push origin main
```

После этого клонирование с enterprise: `git clone --recursive <url>` или `git submodule update --init --recursive`.
