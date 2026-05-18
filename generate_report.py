"""Generate coursework report for Flux Messenger project (login-flow branch, my part)."""

from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH, WD_LINE_SPACING
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml.ns import qn
from docx.oxml import OxmlElement
import copy

doc = Document()

# ── Page setup: A4, margins per СТО-005 (ГОСТ 7.32) ───────────────────────
for section in doc.sections:
    section.page_height = Cm(29.7)
    section.page_width = Cm(21.0)
    section.left_margin   = Cm(3.0)   # 30 mm
    section.right_margin  = Cm(1.0)   # 10 mm
    section.top_margin    = Cm(1.5)   # 15 mm (ГОСТ 7.32)
    section.bottom_margin = Cm(2.0)   # 20 mm


# ── Style helpers ──────────────────────────────────────────────────────────

def set_run_font(run, size=14, bold=False, italic=False, name="Times New Roman"):
    run.font.name = name
    run.font.size = Pt(size)
    run.font.bold = bold
    run.font.italic = italic
    r = run._r
    rPr = r.get_or_add_rPr()
    rFonts = OxmlElement('w:rFonts')
    rFonts.set(qn('w:ascii'), name)
    rFonts.set(qn('w:hAnsi'), name)
    rFonts.set(qn('w:cs'), name)
    existing = rPr.find(qn('w:rFonts'))
    if existing is not None:
        rPr.remove(existing)
    rPr.insert(0, rFonts)


def fmt_para(para, align=WD_ALIGN_PARAGRAPH.JUSTIFY, indent_first=True,
             spacing_before=0, spacing_after=0, line_spacing=WD_LINE_SPACING.SINGLE):
    pf = para.paragraph_format
    pf.alignment = align
    pf.first_line_indent = Cm(1.25) if indent_first else None
    pf.space_before = Pt(spacing_before)
    pf.space_after = Pt(spacing_after)
    pf.line_spacing_rule = line_spacing


def add_normal(doc, text, bold=False, align=WD_ALIGN_PARAGRAPH.JUSTIFY,
               indent_first=True, spacing_before=0, spacing_after=0, size=14):
    p = doc.add_paragraph()
    fmt_para(p, align=align, indent_first=indent_first,
             spacing_before=spacing_before, spacing_after=spacing_after)
    run = p.add_run(text)
    set_run_font(run, size=size, bold=bold)
    return p


def add_heading1(doc, text):
    """Section heading (numbered, bold, centred, new page not forced here)."""
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False,
             spacing_before=6, spacing_after=3)
    run = p.add_run(text.upper())
    set_run_font(run, size=14, bold=True)
    return p


def add_heading2(doc, text):
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False,
             spacing_before=6, spacing_after=3)
    run = p.add_run(text)
    set_run_font(run, size=14, bold=True)
    return p


def add_page_break(doc):
    doc.add_page_break()


def add_caption(doc, text, fig=True):
    """Figure / table caption, centered."""
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.CENTER, indent_first=False,
             spacing_before=3, spacing_after=6)
    run = p.add_run(text)
    set_run_font(run, size=12, bold=False, italic=False)
    return p


def add_bullet(doc, text, level=0):
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.JUSTIFY, indent_first=False,
             spacing_before=0, spacing_after=0)
    pf = p.paragraph_format
    pf.left_indent = Cm(1.25 * (level + 1))
    run = p.add_run(("– " if level == 0 else "    • ") + text)
    set_run_font(run, size=14)
    return p


def add_placeholder(doc, text):
    """Gray-boxed placeholder for diagrams to be inserted manually."""
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.CENTER, indent_first=False,
             spacing_before=6, spacing_after=6)
    run = p.add_run(text)
    set_run_font(run, size=12, italic=True)
    run.font.color.rgb = RGBColor(0x88, 0x88, 0x88)
    # Shading
    pPr = p._p.get_or_add_pPr()
    shd = OxmlElement('w:shd')
    shd.set(qn('w:val'), 'clear')
    shd.set(qn('w:color'), 'auto')
    shd.set(qn('w:fill'), 'E8E8E8')
    pPr.append(shd)
    return p


# ── Spec-table helper ──────────────────────────────────────────────────────

def add_spec_table(doc, title, columns, rows_data):
    """Adds a labeled spec table. columns = list of header strings."""
    add_normal(doc, title, bold=False, align=WD_ALIGN_PARAGRAPH.LEFT,
               indent_first=False, spacing_before=6, spacing_after=3, size=12)
    t = doc.add_table(rows=1 + len(rows_data), cols=len(columns))
    t.style = 'Table Grid'
    t.alignment = WD_TABLE_ALIGNMENT.CENTER
    # Header
    for i, col in enumerate(columns):
        cell = t.cell(0, i)
        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
        p = cell.paragraphs[0]
        p.alignment = WD_ALIGN_PARAGRAPH.CENTER
        run = p.add_run(col)
        set_run_font(run, size=10, bold=True)
    # Data
    for r_idx, row in enumerate(rows_data, start=1):
        for c_idx, val in enumerate(row):
            cell = t.cell(r_idx, c_idx)
            p = cell.paragraphs[0]
            p.alignment = WD_ALIGN_PARAGRAPH.LEFT
            run = p.add_run(str(val))
            set_run_font(run, size=10)
    # After table
    doc.add_paragraph()
    return t


# ══════════════════════════════════════════════════════════════════════════
# TITLE PAGE
# ══════════════════════════════════════════════════════════════════════════

def title_page(doc):
    def center(text, size=12, bold=False, sb=0, sa=0):
        p = doc.add_paragraph()
        fmt_para(p, align=WD_ALIGN_PARAGRAPH.CENTER, indent_first=False,
                 spacing_before=sb, spacing_after=sa)
        run = p.add_run(text)
        set_run_font(run, size=size, bold=bold)

    center("Министерство науки и высшего образования Российской Федерации", 12)
    center("Федеральное государственное бюджетное образовательное учреждение", 12)
    center("высшего образования", 12)
    center("ИРКУТСКИЙ НАЦИОНАЛЬНЫЙ ИССЛЕДОВАТЕЛЬСКИЙ ТЕХНИЧЕСКИЙ УНИВЕРСИТЕТ", 12, bold=True)
    center("Институт информационных технологий и анализа данных", 12, sb=12)

    # допускаю
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.RIGHT, indent_first=False, spacing_before=24)
    run = p.add_run("Допускаю к защите\nРуководитель __________ Фамилия И.О.")
    set_run_font(run, size=12)

    center("Мессенджер Flux Messenger на Android", 14, bold=True, sb=30)

    center("ПОЯСНИТЕЛЬНАЯ ЗАПИСКА", 14, bold=True, sb=12)
    center("к курсовой работе по дисциплине", 12)
    center("Объектно-ориентированное программирование", 12, bold=True)
    center("1.023.00.00 ПЗ", 12, sb=6)

    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=30)
    run = p.add_run("Выполнил студент    __________  Гриценюк Н. И.")
    set_run_font(run, size=12)

    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    run = p.add_run("Нормоконтроль      __________  Фамилия И.О.")
    set_run_font(run, size=12)

    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    run = p.add_run("Курсовая работа защищена с оценкой  ____________________")
    set_run_font(run, size=12)

    center("Иркутск 2025 г.", 12, sb=36)
    add_page_break(doc)


# ══════════════════════════════════════════════════════════════════════════
# TABLE OF CONTENTS (manual)
# ══════════════════════════════════════════════════════════════════════════

def toc_page(doc):
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.CENTER, indent_first=False,
             spacing_before=0, spacing_after=12)
    run = p.add_run("СОДЕРЖАНИЕ")
    set_run_font(run, size=14, bold=True)

    toc_entries = [
        ("Введение", "4"),
        ("1  Анализ предметной области", "6"),
        ("   1.1  Описание предметной области", "6"),
        ("   1.2  Диаграмма AS IS", "8"),
        ("   1.3  Постановка задачи", "9"),
        ("2  Проектирование", "11"),
        ("   2.1  Диаграмма TO BE", "11"),
        ("   2.3  UML-диаграмма Use Case", "12"),
        ("   2.4  Проектирование графического интерфейса", "13"),
        ("3  Реализация", "15"),
        ("   3.1  Используемые технологии и библиотеки", "15"),
        ("   3.2  Диаграммы последовательности", "17"),
        ("   3.3  Спецификация", "19"),
        ("   3.4  UML-диаграмма классов", "34"),
        ("   3.5  Взаимодействие с базой данных", "35"),
        ("   3.6  Тестирование", "37"),
        ("   3.7  Руководство пользователя", "39"),
        ("Заключение", "42"),
        ("Список литературы", "43"),
    ]
    for entry, page in toc_entries:
        p = doc.add_paragraph()
        fmt_para(p, align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False,
                 spacing_before=1, spacing_after=1)
        run = p.add_run(f"{entry}")
        set_run_font(run, size=12)
        # tab to page number
        tab_el = OxmlElement('w:tab')
        run._r.append(tab_el)
        run2 = p.add_run(f"{'.' * max(1, 60 - len(entry))} {page}")
        set_run_font(run2, size=12)

    add_page_break(doc)


# ══════════════════════════════════════════════════════════════════════════
# ВВЕДЕНИЕ
# ══════════════════════════════════════════════════════════════════════════

def section_intro(doc):
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.CENTER, indent_first=False,
             spacing_before=0, spacing_after=12)
    run = p.add_run("ВВЕДЕНИЕ")
    set_run_font(run, size=14, bold=True)

    add_normal(doc,
        "Современные мессенджеры стали неотъемлемой частью повседневной жизни, "
        "обеспечивая быстрый и безопасный обмен сообщениями между людьми. "
        "Несмотря на широкий выбор существующих решений, многие из них страдают "
        "от чрезмерной сложности, закрытости исходного кода, ограниченного контроля над "
        "персональными данными и отсутствия гибких механизмов аутентификации. "
        "В условиях растущей потребности в защищённой и удобной коммуникации особую "
        "актуальность приобретает разработка собственного мессенджера с открытой "
        "архитектурой и современными подходами к безопасности.")

    add_normal(doc,
        "Актуальность данной работы обусловлена востребованностью мобильных "
        "мессенджеров с поддержкой современных методов аутентификации, таких как "
        "Passkey (WebAuthn), а также необходимостью практического освоения технологий "
        "разработки клиент-серверных мобильных приложений на платформе Android.")

    add_normal(doc,
        "Цель курсовой работы — разработка Android-части мессенджера Flux Messenger, "
        "включающей подсистемы аутентификации, управления контактами и чатами, "
        "а также серверной части, обеспечивающей обработку запросов и хранение данных.")

    add_normal(doc, "Для достижения поставленной цели необходимо решить следующие задачи:")
    add_bullet(doc, "проанализировать предметную область и существующие аналоги;")
    add_bullet(doc, "спроектировать архитектуру клиентской Android-части приложения;")
    add_bullet(doc, "реализовать механизмы аутентификации: логин/регистрация по паролю и вход через Passkey (WebAuthn);")
    add_bullet(doc, "разработать серверный REST API на базе Spring Boot 4 с JWT-авторизацией;")
    add_bullet(doc, "реализовать экраны: приветственный экран, вход, регистрация, список чатов, настройки профиля;")
    add_bullet(doc, "обеспечить безопасное хранение токенов на устройстве с использованием EncryptedSharedPreferences;")
    add_bullet(doc, "провести тестирование ключевых компонентов и подготовить руководство пользователя.")

    add_normal(doc,
        "Объект исследования — мобильное приложение-мессенджер для платформы Android "
        "с клиент-серверной архитектурой.")

    add_normal(doc,
        "Предмет исследования — методы и технологии реализации подсистемы "
        "аутентификации, управления чатами и пользовательским профилем в Android-приложении "
        "с использованием Retrofit 3, Spring Boot 4, JWT и WebAuthn.")

    add_page_break(doc)


# ══════════════════════════════════════════════════════════════════════════
# 1. АНАЛИЗ ПРЕДМЕТНОЙ ОБЛАСТИ
# ══════════════════════════════════════════════════════════════════════════

def section1(doc):
    add_heading1(doc, "1 АНАЛИЗ ПРЕДМЕТНОЙ ОБЛАСТИ")

    # 1.1
    add_heading2(doc, "1.1 Описание предметной области")

    add_normal(doc,
        "Мессенджер — программное обеспечение для мгновенного обмена текстовыми сообщениями "
        "и медиафайлами посредством сети Интернет. Типичная архитектура мессенджера "
        "предполагает наличие клиентского приложения (мобильного или десктопного) и "
        "серверной части, обеспечивающей хранение, маршрутизацию и доставку сообщений.")

    add_normal(doc,
        "Основными участниками предметной области являются пользователь и система. "
        "Пользователь регистрируется в системе, создаёт личный профиль, устанавливает "
        "контакты с другими пользователями, создаёт личные или групповые чаты и обменивается "
        "сообщениями. Система отвечает за идентификацию пользователя, хранение истории "
        "переписки, управление списком контактов и чатов.")

    add_normal(doc, "Основные понятия предметной области:")
    add_bullet(doc,
        "Пользователь (User) — зарегистрированный участник системы, обладающий уникальным "
        "номером телефона, именем пользователя (handle) и профилем. Аутентифицируется по "
        "паролю или через Passkey.")
    add_bullet(doc,
        "Чат (Chat) — коммуникационный канал между двумя (личный чат, тип DIRECT) или более "
        "участниками (групповой чат, тип GROUP). Обладает идентификатором, именем (для групп) "
        "и аватаром.")
    add_bullet(doc,
        "Контакт (Contact) — пользователь, добавленный в адресную книгу текущего пользователя. "
        "Может иметь переопределённые имя и фамилию.")
    add_bullet(doc,
        "Избранное (Favorites) — набор чатов, помеченных пользователем для быстрого доступа.")
    add_bullet(doc,
        "Токен доступа (Access Token) — кратковременный JWT-токен (15 минут), используемый "
        "для авторизации запросов к API.")
    add_bullet(doc,
        "Токен обновления (Refresh Token) — долговременный JWT-токен (7 дней), используемый "
        "для получения нового токена доступа без повторной аутентификации.")
    add_bullet(doc,
        "Passkey (WebAuthn) — стандарт беспарольной аутентификации, основанный на криптографии "
        "публичного ключа. Позволяет входить в систему с помощью биометрии или PIN-кода устройства.")

    add_normal(doc,
        "Существующие аналоги (Telegram, WhatsApp, Signal) обеспечивают широкий функционал, "
        "однако являются закрытыми платформами с ограниченными возможностями кастомизации "
        "и контроля над серверной инфраструктурой. Разработка собственного мессенджера "
        "позволяет освоить полный стек технологий — от мобильного клиента до серверного API — "
        "и применить современные подходы к безопасности аутентификации.")

    add_normal(doc,
        "В рамках данной работы реализуется Android-клиент и серверная часть мессенджера "
        "Flux Messenger. Серверная часть предоставляет REST API для регистрации, входа, "
        "управления чатами, контактами и профилем пользователя. Android-клиент реализует "
        "пользовательский интерфейс для взаимодействия с API посредством библиотеки Retrofit.")

    # 1.2
    add_heading2(doc, "1.2 Диаграмма AS IS")

    add_normal(doc,
        "Диаграмма AS IS описывает текущий процесс коммуникации пользователей "
        "до внедрения системы. В настоящий момент пользователи вынуждены пользоваться "
        "сторонними мессенджерами, не имея возможности настраивать механизмы аутентификации, "
        "управлять своими данными на собственной инфраструктуре или интегрировать систему "
        "с корпоративными сервисами. Процесс аутентификации, как правило, ограничен "
        "логином и паролем, без поддержки современных беспарольных методов.")

    add_normal(doc, "[Вставить диаграмму AS IS здесь]")
    add_placeholder(doc, "[ МЕСТО ДЛЯ ДИАГРАММЫ AS IS ]\n"
                         "Вставьте диаграмму бизнес-процесса «как есть» (нотация IDEF0 или BPMN)")
    add_caption(doc, "Рисунок 1.1 – Диаграмма бизнес-процесса AS IS")

    add_normal(doc,
        "На диаграмме AS IS (рисунок 1.1) показано, что в текущем состоянии "
        "пользователь для коммуникации использует стороннее приложение, "
        "регистрируясь вручную через форму входа с логином и паролем. "
        "Отсутствует централизованное управление контактами и чатами в рамках "
        "единой корпоративной или учебной платформы.")

    # 1.3
    add_heading2(doc, "1.3 Постановка задачи")

    add_normal(doc,
        "На основании анализа предметной области сформулированы следующие "
        "требования к разрабатываемой системе:")

    add_normal(doc, "Функциональные требования к Android-приложению:")
    add_bullet(doc, "Приветственный экран с выбором способа входа: по логину/паролю, через Passkey.")
    add_bullet(doc, "Экран входа: ввод номера телефона и пароля, валидация полей, отображение ошибок.")
    add_bullet(doc, "Экран регистрации: двухэтапная форма — ввод телефона и пароля, затем имя, фамилия, username и аватар.")
    add_bullet(doc, "Passkey-аутентификация: запуск потока WebAuthn через Credential Manager Android.")
    add_bullet(doc, "Экран списка чатов: отображение всех чатов, фильтрация по типу (все / личные / группы), поиск, отображение избранных.")
    add_bullet(doc, "Создание нового чата: личный чат (выбор контакта) или групповой (выбор нескольких участников, название, аватар).")
    add_bullet(doc, "Экран настроек: отображение профиля пользователя, редактирование данных, выход из аккаунта.")
    add_bullet(doc, "Автоматическое обновление токена доступа при истечении срока действия.")

    add_normal(doc, "Функциональные требования к серверной части:")
    add_bullet(doc, "Регистрация пользователя: POST /api/auth/sign-up — принимает ФИО, username, телефон, пароль.")
    add_bullet(doc, "Аутентификация: POST /api/auth/sign-in — возвращает пару JWT-токенов (access + refresh).")
    add_bullet(doc, "Обновление токена: POST /api/auth/refresh.")
    add_bullet(doc, "Passkey-регистрация: POST /api/auth/passkey/options, POST /api/auth/passkey/complete.")
    add_bullet(doc, "Passkey-аутентификация: POST /api/auth/passkey/authenticate/start, .../finish.")
    add_bullet(doc, "Управление чатами: GET /chats, POST /chats/direct, POST /chats/group, DELETE /chats/{id}.")
    add_bullet(doc, "Управление профилем: GET/PUT /users/me, PATCH /users/me/avatar, DELETE /users/me.")
    add_bullet(doc, "Управление контактами: GET /users/me/contacts, POST /users/me/contacts.")
    add_bullet(doc, "Избранное: GET /chats/favorites, POST /chats/favorites.")

    add_normal(doc, "Нефункциональные требования:")
    add_bullet(doc, "Безопасность: JWT с подписью HMAC-SHA256, BCrypt-хеширование паролей, EncryptedSharedPreferences на устройстве.")
    add_bullet(doc, "Производительность: асинхронное выполнение сетевых запросов через Executor/LiveData.")
    add_bullet(doc, "Масштабируемость: модульная архитектура (features/login, features/chats, features/settings).")

    add_page_break(doc)


# ══════════════════════════════════════════════════════════════════════════
# 2. ПРОЕКТИРОВАНИЕ
# ══════════════════════════════════════════════════════════════════════════

def section2(doc):
    add_heading1(doc, "2 ПРОЕКТИРОВАНИЕ")

    # 2.1
    add_heading2(doc, "2.1 Диаграмма TO BE")

    add_normal(doc,
        "Диаграмма TO BE описывает целевой бизнес-процесс после внедрения разрабатываемой "
        "системы. Пользователь взаимодействует с мессенджером Flux Messenger напрямую: "
        "регистрируется через мобильное приложение, выбирает удобный способ аутентификации "
        "(пароль или Passkey), управляет контактами и чатами в рамках единой платформы.")

    add_placeholder(doc, "[ МЕСТО ДЛЯ ДИАГРАММЫ TO BE ]\n"
                         "Вставьте диаграмму бизнес-процесса «как будет» (нотация IDEF0 или BPMN)")
    add_caption(doc, "Рисунок 2.1 – Диаграмма бизнес-процесса TO BE")

    add_normal(doc,
        "На диаграмме TO BE (рисунок 2.1) показано, что после внедрения системы "
        "пользователь регистрируется через мобильное приложение, получает пару JWT-токенов "
        "и взаимодействует с системой через защищённый API. Токен доступа автоматически "
        "обновляется, что обеспечивает бесперебойный сеанс работы.")

    # 2.3
    add_heading2(doc, "2.3 UML-диаграмма Use Case")

    add_normal(doc,
        "Диаграмма вариантов использования (Use Case) описывает функциональные "
        "возможности системы с точки зрения актора — пользователя мессенджера. "
        "На рисунке 2.2 представлена диаграмма Use Case для приложения Flux Messenger.")

    add_normal(doc,
        "Актор «Пользователь» взаимодействует со следующими вариантами использования:")
    add_bullet(doc, "Зарегистрироваться — создание нового аккаунта с указанием телефона, пароля и профиля.")
    add_bullet(doc, "Войти по паролю — аутентификация по номеру телефона и паролю.")
    add_bullet(doc, "Войти через Passkey — беспарольная аутентификация с использованием биометрии устройства.")
    add_bullet(doc, "Просмотреть список чатов — отображение всех доступных чатов с фильтрацией и поиском.")
    add_bullet(doc, "Создать личный чат — открытие диалога с выбранным контактом.")
    add_bullet(doc, "Создать групповой чат — создание группового чата с несколькими участниками.")
    add_bullet(doc, "Управлять профилем — просмотр и редактирование данных профиля.")
    add_bullet(doc, "Добавить контакт — поиск и добавление пользователя в адресную книгу.")
    add_bullet(doc, "Выйти из аккаунта — очистка токенов и возврат на экран входа.")
    add_bullet(doc, "Добавить чат в избранное — пометка чата для быстрого доступа.")

    add_placeholder(doc,
        "[ МЕСТО ДЛЯ UML ДИАГРАММЫ USE CASE ]\n"
        "Диаграмма вариантов использования (PlantUML / draw.io)")
    add_caption(doc, "Рисунок 2.2 – UML-диаграмма Use Case")

    add_normal(doc,
        "Как видно из диаграммы (рисунок 2.2), центральным актором является "
        "«Пользователь», который имеет доступ ко всем перечисленным вариантам использования. "
        "Варианты «Войти по паролю» и «Войти через Passkey» включают в себя "
        "«Обновить токен» (extend), что отражает механизм автоматического обновления "
        "JWT-токена доступа.")

    # 2.4
    add_heading2(doc, "2.4 Проектирование графического интерфейса")

    add_normal(doc,
        "Графический интерфейс Android-приложения спроектирован в соответствии "
        "с принципами Material Design 3. Навигация между экранами реализована "
        "с помощью Android Navigation Component (граф навигации nav_graph.xml).")

    add_normal(doc, "Приложение состоит из следующих экранов:")

    add_normal(doc, "Подсистема аутентификации (LoginActivity):", bold=False)
    add_bullet(doc,
        "Приветственный экран (WelcomeAuthFragment) — содержит три кнопки: «Войти», "
        "«Зарегистрироваться» и «Войти с Passkey». Кнопка Passkey запускает поток "
        "WebAuthn через Credential Manager.")
    add_bullet(doc,
        "Экран входа (LoginFragment) — содержит поля ввода номера телефона (PhoneInputView) "
        "и пароля (PasswordInputView). Кнопка «Войти» активируется после успешной валидации. "
        "При успешной аутентификации приложение переходит к MainActivity.")
    add_bullet(doc,
        "Экран регистрации шаг 1 (SignUpAuthFragment) — ввод номера телефона, пароля "
        "и подтверждения пароля. Кнопка «Далее» передаёт телефон и пароль на следующий экран.")
    add_bullet(doc,
        "Экран регистрации шаг 2 (SignUpCompletionInternalFragment) — ввод имени, фамилии "
        "и имени пользователя (username). Поддержка выбора аватара через AvatarDoubleInputView.")
    add_bullet(doc,
        "Экран завершения регистрации через Passkey (SignUpCompletion3rdPartyFragment) — "
        "ввод имени и username для пользователя, прошедшего Passkey-регистрацию.")

    add_normal(doc, "Основная часть приложения (MainActivity + Navigation):", bold=False)
    add_bullet(doc,
        "Экран списка чатов (ChatsFragment) — RecyclerView со списком чатов, "
        "горизонтальный список избранных, сегментированные вкладки «Все / Личные / Группы», "
        "панель поиска с анимацией раскрытия.")
    add_bullet(doc,
        "Нижний лист нового сообщения (NewMessageBottomSheet) — выбор действия: "
        "создать новый групповой чат, добавить контакт или начать личный диалог.")
    add_bullet(doc,
        "Экран создания группового чата (NewGroupSelectMembersFragment → NewGroupSetupFragment) — "
        "двухэтапный процесс: выбор участников из контактов, затем ввод названия и загрузка аватара.")
    add_bullet(doc,
        "Экран настроек (SettingsFragment) — отображение имени, username, биографии и аватара "
        "пользователя. Кнопки перехода к редактированию профиля, уведомлениям и выходу.")
    add_bullet(doc,
        "Экран редактирования профиля (SettingsProfileFragment) — редактирование имени, "
        "биографии и загрузка нового аватара.")

    add_placeholder(doc,
        "[ МЕСТО ДЛЯ МАКЕТОВ ЭКРАНОВ / WIREFRAMES ]\n"
        "Вставьте скриншоты или мокапы экранов приложения")
    add_caption(doc, "Рисунок 2.3 – Макеты экранов приложения Flux Messenger")

    add_page_break(doc)


# ══════════════════════════════════════════════════════════════════════════
# 3. РЕАЛИЗАЦИЯ
# ══════════════════════════════════════════════════════════════════════════

def section3(doc):
    add_heading1(doc, "3 РЕАЛИЗАЦИЯ")

    # 3.1
    add_heading2(doc, "3.1 Используемые технологии и библиотеки")

    add_normal(doc,
        "Приложение разработано в виде многомодульного Gradle-проекта на языках Java (Android) "
        "и Java (Spring Boot). Ниже перечислены основные технологии и библиотеки.")

    add_normal(doc, "Android-клиент (Java 11, minSdk 26):", bold=False)
    add_spec_table(doc,
        "Таблица 3.1 – Библиотеки Android-части",
        ["Технология / библиотека", "Версия", "Назначение"],
        [
            ["Retrofit 3", "3.0.0", "HTTP-клиент для обращения к REST API"],
            ["Gson (Retrofit converter)", "2.11.0", "Сериализация/десериализация JSON"],
            ["OkHttp 4", "4.x", "HTTP-клиент нижнего уровня, перехватчики запросов"],
            ["AndroidX Credentials (CredentialManager)", "1.5.0", "Поддержка Passkey/WebAuthn"],
            ["AndroidX Navigation Component", "2.8.x", "Навигация между фрагментами"],
            ["AndroidX Security Crypto", "1.1.0-alpha06", "EncryptedSharedPreferences для хранения токенов"],
            ["Glide", "4.16.0", "Загрузка и кэширование изображений"],
            ["Material Design 3 (MDC-Android)", "1.12.0", "UI-компоненты Material"],
            ["LiveData / ViewModel", "2.8.x", "Реактивная архитектура (MVVM)"],
        ]
    )

    add_normal(doc, "Серверная часть (Java 21, Spring Boot 4):", bold=False)
    add_spec_table(doc,
        "Таблица 3.2 – Библиотеки серверной части",
        ["Технология / библиотека", "Версия", "Назначение"],
        [
            ["Spring Boot 4", "4.0.x", "Платформа для построения REST API"],
            ["Spring Security", "6.x", "Аутентификация, авторизация, фильтры"],
            ["Spring Data JPA / Hibernate", "6.x", "ORM, работа с PostgreSQL"],
            ["jjwt (Java JWT)", "0.12.x", "Генерация и валидация JWT-токенов"],
            ["Spring Security WebAuthn", "6.x", "Поддержка Passkey / WebAuthn"],
            ["Lombok", "1.18.x", "Генерация boilerplate-кода"],
            ["SpringDoc OpenAPI (Swagger)", "2.x", "Документация API"],
            ["MinIO Client", "8.x", "Объектное хранилище для аватаров"],
            ["PostgreSQL JDBC", "42.x", "Драйвер базы данных"],
            ["JUnit 5 + Spring Test", "5.x", "Модульное и интеграционное тестирование"],
        ]
    )

    add_normal(doc, "Инфраструктура:", bold=False)
    add_bullet(doc, "PostgreSQL 15 — основная реляционная база данных.")
    add_bullet(doc, "MinIO — S3-совместимое объектное хранилище для аватаров пользователей и групп.")
    add_bullet(doc, "Docker Compose — оркестрация контейнеров PostgreSQL, MinIO и серверного приложения.")
    add_bullet(doc, "Nginx — реверс-прокси для продакшн-деплоя.")
    add_bullet(doc, "GitHub Actions — CI/CD пайплайн для сборки и деплоя.")

    # 3.2
    add_heading2(doc, "3.2 Диаграммы последовательности")

    add_normal(doc,
        "Диаграммы последовательности UML описывают взаимодействие компонентов "
        "системы при выполнении ключевых сценариев. Ниже представлены три основных сценария.")

    add_normal(doc, "Сценарий 1: Вход по логину и паролю (рисунок 3.1)")
    add_normal(doc,
        "Пользователь вводит номер телефона и пароль в LoginFragment. "
        "Фрагмент уведомляет LoginViewModel, который передаёт запрос в LoginRepository. "
        "LoginRepository вызывает authApi.login() через Retrofit, который отправляет "
        "HTTP POST /api/auth/sign-in на сервер. Сервер проверяет учётные данные через "
        "AuthenticationManager, формирует JWT-токены и возвращает их. "
        "LoginRepository сохраняет токены в TokenManager (EncryptedSharedPreferences). "
        "LoginViewModel публикует успешный результат, LoginFragment запускает MainActivity.")

    add_placeholder(doc,
        "[ ДИАГРАММА ПОСЛЕДОВАТЕЛЬНОСТИ: Вход по паролю ]\n"
        "User → LoginFragment → LoginViewModel → LoginRepository → AuthApi (Retrofit)\n"
        "→ POST /api/auth/sign-in → AuthController → AuthenticationService → JwtService\n"
        "← JwtAuthenticationResponse ← TokenManager.saveTokens() ← MainActivity")
    add_caption(doc, "Рисунок 3.1 – Диаграмма последовательности: вход по паролю")

    add_normal(doc, "Сценарий 2: Регистрация нового пользователя (рисунок 3.2)")
    add_normal(doc,
        "Пользователь последовательно заполняет SignUpAuthFragment (телефон, пароль) "
        "и SignUpCompletionInternalFragment (имя, фамилия, username, аватар). "
        "При нажатии «Готово» SignUpCompletionInternalFragment вызывает "
        "LoginViewModel.signUp(). ViewModel передаёт параметры в LoginRepository, "
        "который выполняет POST /api/auth/sign-up. Сервер проверяет уникальность телефона "
        "и username, сохраняет пользователя в базу, возвращает JWT-токены. "
        "Приложение переходит к MainActivity.")

    add_placeholder(doc,
        "[ ДИАГРАММА ПОСЛЕДОВАТЕЛЬНОСТИ: Регистрация ]\n"
        "User → SignUpAuthFragment → SignUpCompletionInternalFragment → LoginViewModel\n"
        "→ LoginRepository → POST /api/auth/sign-up → AuthController → UserService\n"
        "← JwtAuthenticationResponse ← TokenManager.saveTokens() ← MainActivity")
    add_caption(doc, "Рисунок 3.2 – Диаграмма последовательности: регистрация")

    add_normal(doc, "Сценарий 3: Passkey-аутентификация (рисунок 3.3)")
    add_normal(doc,
        "Пользователь нажимает «Войти с Passkey» на WelcomeAuthFragment. "
        "WelcomeAuthFragment вызывает PasskeyAuthManager.authenticate(). "
        "PasskeyAuthManager через LoginRepository выполняет POST /api/auth/passkey/authenticate/start, "
        "получая challenge и nonce. Сервер генерирует challenge через PasskeyService и "
        "кэширует его в PasskeyChallengeCache. Клиент запрашивает у Credential Manager "
        "подписанный ответ (assertion), который передаёт вместе с nonce на "
        "POST /api/auth/passkey/authenticate/finish. Сервер верифицирует подпись, "
        "находит пользователя и возвращает JWT-токены.")

    add_placeholder(doc,
        "[ ДИАГРАММА ПОСЛЕДОВАТЕЛЬНОСТИ: Passkey ]\n"
        "User → WelcomeAuthFragment → PasskeyAuthManager → LoginRepository\n"
        "→ POST .../authenticate/start → PasskeyAuthController → PasskeyService\n"
        "← challenge/nonce → CredentialManager (биометрия) ← assertion\n"
        "→ POST .../authenticate/finish ← JwtAuthenticationResponse ← MainActivity")
    add_caption(doc, "Рисунок 3.3 – Диаграмма последовательности: Passkey-аутентификация")

    # 3.3
    add_heading2(doc, "3.3 Спецификация")

    add_normal(doc,
        "В данном разделе приведены спецификации всех классов, реализованных "
        "в рамках данной курсовой работы. Спецификации включают описание полей и методов "
        "каждого класса.")

    # ── Android: Login ──────────────────────────────────────────────────

    add_normal(doc, "3.3.1 Пакет features.login", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc, "Класс LoginActivity", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Входная точка подсистемы аутентификации. Наследует AppCompatActivity. "
        "При создании проверяет наличие refresh-токена: если токен есть — сразу "
        "переходит к MainActivity, иначе отображает навигационный граф аутентификации.")

    add_spec_table(doc,
        "Таблица 3.3.1.1 – Методы класса LoginActivity",
        ["№", "Метод", "Тип доступа", "Тип возврата", "Назначение"],
        [
            ["1", "onCreate(Bundle)", "protected", "void", "Инициализация Activity, проверка сессии"],
            ["2", "hasActiveSession()", "private", "boolean", "Проверяет наличие refresh-токена"],
            ["3", "openMainScreen()", "private", "void", "Запускает MainActivity и завершает LoginActivity"],
        ]
    )

    add_normal(doc, "Класс LoginViewModel", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "ViewModel для управления состоянием форм входа и регистрации. "
        "Содержит LiveData для состояния формы (LoginFormState), результата входа "
        "(LoginResult) и результата регистрации. Выполняет запросы через LoginRepository "
        "в фоновом потоке Executor.")

    add_spec_table(doc,
        "Таблица 3.3.1.2 – Поля класса LoginViewModel",
        ["№", "Поле", "Тип", "Доступ", "Назначение"],
        [
            ["1", "loginFormState", "MutableLiveData<LoginFormState>", "private", "Состояние валидации формы"],
            ["2", "loginResult", "MutableLiveData<LoginResult>", "private", "Результат операции входа"],
            ["3", "signUpResult", "MutableLiveData<LoginResult>", "private", "Результат операции регистрации"],
            ["4", "executor", "Executor", "private", "Фоновый поток для сетевых запросов"],
            ["5", "loginRepository", "LoginRepository", "private", "Репозиторий аутентификации"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.3.1.3 – Методы класса LoginViewModel",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "login(phone, password)", "public", "void", "Выполняет вход в фоновом потоке"],
            ["2", "signUp(firstName, lastName, username, phone, password)", "public", "void", "Регистрирует нового пользователя"],
            ["3", "loginDataChanged(phone, password)", "public", "void", "Валидирует поля формы входа"],
            ["4", "getLoginFormState()", "public", "LiveData<LoginFormState>", "Возвращает LiveData состояния формы"],
            ["5", "getLoginResult()", "public", "LiveData<LoginResult>", "Возвращает LiveData результата входа"],
            ["6", "getSignUpResult()", "public", "LiveData<LoginResult>", "Возвращает LiveData результата регистрации"],
            ["7", "isPhoneValid(phone)", "private", "boolean", "Проверяет корректность телефона"],
            ["8", "isPasswordValid(password)", "private", "boolean", "Проверяет длину пароля (> 5 символов)"],
        ]
    )

    add_normal(doc, "Класс LoginRepository", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Репозиторий аутентификации. Инкапсулирует все HTTP-вызовы к API "
        "аутентификации через AuthApi (Retrofit). Сохраняет полученные токены "
        "в TokenManager. Поддерживает стандартный вход, регистрацию и полный цикл "
        "Passkey: получение опций, завершение регистрации, запуск и завершение аутентификации.")

    add_spec_table(doc,
        "Таблица 3.3.1.4 – Поля класса LoginRepository",
        ["№", "Поле", "Тип", "Доступ", "Назначение"],
        [
            ["1", "authApi", "AuthApi", "private final", "Retrofit-интерфейс для auth-запросов"],
            ["2", "tokenManager", "TokenManager", "private final", "Менеджер хранения токенов"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.3.1.5 – Методы класса LoginRepository",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "login(phone, password)", "public", "Result<String>", "POST /api/auth/sign-in"],
            ["2", "signUp(firstName, lastName, username, phone, password)", "public", "Result<String>", "POST /api/auth/sign-up"],
            ["3", "getPasskeyRegistrationOptions(phone)", "public", "Result<PasskeyRegistrationOptions>", "POST /api/auth/passkey/options"],
            ["4", "completePasskeyRegistration(nonce, credentialJson)", "public", "Result<String>", "POST /api/auth/passkey/complete"],
            ["5", "startPasskeyAuthentication()", "public", "Result<PasskeyAssertionOptions>", "POST /api/auth/passkey/authenticate/start"],
            ["6", "finishPasskeyAuthentication(nonce, credentialJson)", "public", "Result<String>", "POST /api/auth/passkey/authenticate/finish"],
            ["7", "logout()", "public", "void", "Очищает токены через TokenManager"],
            ["8", "isLoggedIn()", "public", "boolean", "Проверяет наличие access-токена"],
            ["9", "resolveUrl(path)", "private", "String", "Преобразует относительный путь в полный URL"],
        ]
    )

    add_normal(doc, "Класс PasskeyAuthManager", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Управляет полным циклом Passkey-аутентификации на клиенте. "
        "Использует AndroidX Credential Manager для взаимодействия с системным "
        "хранилищем ключей. При отсутствии ключа перенаправляет на регистрацию.")

    add_spec_table(doc,
        "Таблица 3.3.1.6 – Методы класса PasskeyAuthManager",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "authenticate(activity, callback)", "public", "void", "Запускает поток аутентификации через Passkey"],
            ["2", "requestGetCredential(activity, options, callback)", "private", "void", "Запрашивает credential у Credential Manager"],
            ["3", "finishAuthentication(nonce, assertionJson, callback)", "private", "void", "Завершает аутентификацию, отправляя assertion на сервер"],
            ["4", "register(activity, phone, callback)", "public", "void", "Запускает поток регистрации нового Passkey"],
            ["5", "notifyError(callback, message)", "private", "void", "Передаёт ошибку в callback на главном потоке"],
        ]
    )

    # ── Android: Core ──────────────────────────────────────────────────

    add_normal(doc, "3.3.2 Пакет core.auth", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=12)

    add_normal(doc, "Класс TokenManager", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Обеспечивает безопасное хранение JWT-токенов на устройстве "
        "с использованием EncryptedSharedPreferences (AES256-GCM для значений, "
        "AES256-SIV для ключей). Отслеживает время истечения access-токена.")

    add_spec_table(doc,
        "Таблица 3.3.2.1 – Поля класса TokenManager",
        ["№", "Поле", "Тип", "Доступ", "Назначение"],
        [
            ["1", "prefs", "SharedPreferences", "private final", "Зашифрованное хранилище"],
            ["2", "PREFS_NAME", "String", "private static final", "Имя файла хранилища"],
            ["3", "KEY_ACCESS", "String", "private static final", "Ключ access-токена"],
            ["4", "KEY_REFRESH", "String", "private static final", "Ключ refresh-токена"],
            ["5", "KEY_EXPIRES", "String", "private static final", "Ключ времени истечения"],
            ["6", "ACCESS_TOKEN_TTL_MS", "long", "private static final", "Время жизни access-токена (900 000 мс)"],
            ["7", "BUFFER_MS", "long", "private static final", "Буфер до истечения (30 000 мс)"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.3.2.2 – Методы класса TokenManager",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "saveTokens(AuthTokens)", "public", "void", "Сохраняет пару токенов и время истечения"],
            ["2", "saveTokens(String, String)", "public", "void", "Перегрузка для строк"],
            ["3", "getAccessToken()", "public", "@Nullable String", "Возвращает access-токен"],
            ["4", "getRefreshToken()", "public", "@Nullable String", "Возвращает refresh-токен"],
            ["5", "isAccessTokenExpired()", "public", "boolean", "Проверяет истечение access-токена"],
            ["6", "clearTokens()", "public", "void", "Удаляет все токены"],
        ]
    )

    add_normal(doc, "Класс AuthInterceptor", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "OkHttp Interceptor, автоматически добавляющий JWT-заголовок Authorization "
        "ко всем исходящим запросам. При получении ответа 401/403 выполняет "
        "автоматическое обновление токена через AuthApi и повторяет запрос.")

    add_spec_table(doc,
        "Таблица 3.3.2.3 – Методы класса AuthInterceptor",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "intercept(chain)", "public", "Response", "Добавляет токен, обрабатывает 401/403"],
            ["2", "getValidToken()", "private", "@Nullable String", "Возвращает актуальный токен (обновляет при необходимости)"],
            ["3", "refreshTokens()", "private synchronized", "@Nullable String", "Обновляет токены через API"],
        ]
    )

    # ── Android: Core Network ──────────────────────────────────────────────────

    add_normal(doc, "3.3.3 Пакет core.network", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=12)

    add_normal(doc, "Класс ApiClient", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Фабрика Retrofit-клиента. Создаёт единственный экземпляр Retrofit "
        "(singleton) с подключённым AuthInterceptor. Отдельный «голый» Retrofit "
        "используется для запросов обновления токена внутри AuthInterceptor, "
        "чтобы избежать рекурсии.")

    add_spec_table(doc,
        "Таблица 3.3.3.1 – Методы класса ApiClient",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "api(context)", "public static", "ApiService", "Создаёт ApiService с TokenManager из context"],
            ["2", "getInstance(tokenManager)", "public static", "Retrofit", "Возвращает singleton Retrofit-экземпляр"],
        ]
    )

    add_normal(doc, "Интерфейс ApiService", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Retrofit-интерфейс для всех защищённых эндпоинтов API. "
        "Определяет методы для работы с чатами, пользователями, контактами и аватарами.")

    add_spec_table(doc,
        "Таблица 3.3.3.2 – Методы интерфейса ApiService",
        ["№", "Метод (HTTP)", "Эндпоинт", "Возврат", "Назначение"],
        [
            ["1", "GET", "/chats", "Call<List<ChatResponse>>", "Список всех чатов"],
            ["2", "GET", "/chats/favorites", "Call<List<FavoriteResponse>>", "Список избранных чатов"],
            ["3", "POST", "/chats/favorites", "Call<FavoriteResponse>", "Добавить чат в избранное"],
            ["4", "GET", "/users", "Call<List<UserResponse>>", "Список всех пользователей"],
            ["5", "GET", "/users/me", "Call<UserResponse>", "Профиль текущего пользователя"],
            ["6", "PUT", "/users/me", "Call<UserResponse>", "Обновить профиль текущего пользователя"],
            ["7", "DELETE", "/users/me", "Call<Void>", "Удалить аккаунт"],
            ["8", "GET", "/users/me/contacts", "Call<List<ContactResponse>>", "Список контактов"],
            ["9", "POST", "/chats/direct", "Call<ChatResponse>", "Создать личный чат"],
            ["10", "Multipart POST", "/chats/group", "Call<ChatResponse>", "Создать групповой чат с аватаром"],
            ["11", "DELETE", "/chats/{id}", "Call<Void>", "Удалить чат"],
            ["12", "POST", "/users/me/contacts", "Call<Void>", "Добавить контакт"],
            ["13", "Multipart PATCH", "/users/me/avatar", "Call<UserResponse>", "Загрузить аватар"],
        ]
    )

    # ── Android: Chats ──────────────────────────────────────────────────

    add_normal(doc, "3.3.4 Пакет features.chats", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=12)

    add_normal(doc, "Класс ChatsViewModel", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "AndroidViewModel для управления состоянием списка чатов. "
        "Содержит LiveData-наблюдаемые для чатов, избранного, контактов, ошибок "
        "и состояния создания чата. Все сетевые запросы выполняются в фоновом потоке "
        "ExecutorService.")

    add_spec_table(doc,
        "Таблица 3.3.4.1 – Поля класса ChatsViewModel",
        ["№", "Поле", "Тип", "Доступ", "Назначение"],
        [
            ["1", "chats", "MutableLiveData<List<Chat>>", "private", "Список чатов"],
            ["2", "favorites", "MutableLiveData<List<Chat>>", "private", "Список избранных чатов"],
            ["3", "contacts", "MutableLiveData<List<Contact>>", "private", "Список контактов"],
            ["4", "error", "MutableLiveData<String>", "private", "Сообщение об ошибке"],
            ["5", "chatCreated", "MutableLiveData<Boolean>", "private", "Флаг успешного создания чата"],
            ["6", "currentUserId", "MutableLiveData<String>", "private", "ID текущего пользователя"],
            ["7", "selectedGroupMembers", "MutableLiveData<List<Contact>>", "private", "Выбранные участники группы"],
            ["8", "executor", "ExecutorService", "private", "Пул потоков для сетевых операций"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.3.4.2 – Методы класса ChatsViewModel",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "loadChats()", "public", "void", "Загружает список чатов (если ещё не загружен)"],
            ["2", "loadFavorites()", "public", "void", "Загружает список избранного"],
            ["3", "fetchChats()", "private", "void", "Выполняет GET /chats в фоновом потоке"],
            ["4", "fetchFavorites()", "private", "void", "Выполняет GET /chats/favorites"],
            ["5", "createDirectChat(memberIds)", "public", "void", "POST /chats/direct"],
            ["6", "createGroupChat(name, memberIds, avatarUri)", "public", "void", "POST /chats/group"],
            ["7", "deleteChat(chat)", "public", "void", "DELETE /chats/{id}"],
            ["8", "addFavorite(chat)", "public", "void", "POST /chats/favorites"],
            ["9", "loadCurrentUser()", "private", "void", "GET /users/me для получения currentUserId"],
            ["10", "toChat(ChatResponse)", "private", "Chat", "Преобразует DTO в модель Chat"],
            ["11", "clearError()", "public", "void", "Сбрасывает текущую ошибку"],
            ["12", "setSelectedGroupMembers(members)", "public", "void", "Задаёт выбранных участников группы"],
        ]
    )

    # ── Android: Settings ──────────────────────────────────────────────────

    add_normal(doc, "3.3.5 Пакет features.settings", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=12)

    add_normal(doc, "Класс SettingsViewModel", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "AndroidViewModel для управления данными профиля пользователя. "
        "Загружает, обновляет и удаляет аккаунт пользователя, а также "
        "загружает аватар через Multipart-запрос.")

    add_spec_table(doc,
        "Таблица 3.3.5.1 – Методы класса SettingsViewModel",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "loadUser()", "public", "void", "GET /users/me — загрузка профиля"],
            ["2", "saveUser(UpdateUserRequest)", "public", "void", "PUT /users/me — обновление профиля"],
            ["3", "deleteAccount(Runnable)", "public", "void", "DELETE /users/me — удаление аккаунта"],
            ["4", "uploadAvatar(Uri)", "public", "void", "PATCH /users/me/avatar — загрузка аватара"],
            ["5", "getUser()", "public", "LiveData<UserResponse>", "Возвращает LiveData профиля"],
            ["6", "getError()", "public", "LiveData<String>", "Возвращает LiveData ошибки"],
        ]
    )

    # ── Backend ──────────────────────────────────────────────────

    add_normal(doc, "3.3.6 Серверная часть: пакет messenger", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=12)

    add_normal(doc, "Класс User (Entity)", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "JPA-сущность, представляющая пользователя системы. Реализует интерфейс "
        "UserDetails Spring Security. Метод getUsername() возвращает phone "
        "(для совместимости с Spring Security), доступ к handle (username) "
        "осуществляется через getHandle().")

    add_spec_table(doc,
        "Таблица 3.3.6.1 – Поля класса User",
        ["№", "Поле", "Тип", "Ограничения", "Назначение"],
        [
            ["1", "id", "UUID", "@Id, @GeneratedValue(UUID)", "Первичный ключ"],
            ["2", "firstName", "String", "NOT NULL", "Имя пользователя"],
            ["3", "lastName", "String", "—", "Фамилия пользователя"],
            ["4", "username", "String", "UNIQUE, 3–32, [a-zA-Z0-9_]", "Handle (отображаемое имя)"],
            ["5", "phone", "String", "UNIQUE, NOT NULL, 10–15 цифр", "Номер телефона (Spring Security principal)"],
            ["6", "email", "String", "UNIQUE, формат email", "Электронная почта"],
            ["7", "avatarUrl", "String", "—", "URL аватара в MinIO"],
            ["8", "bio", "TEXT", "—", "Биография пользователя"],
            ["9", "password", "String", "NOT NULL", "BCrypt-хеш пароля"],
            ["10", "notifications", "boolean", "NOT NULL, default=true", "Флаг уведомлений"],
            ["11", "contacts", "@OneToMany", "—", "Список контактов"],
            ["12", "favorites", "@ManyToMany", "—", "Список избранных чатов"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.3.6.2 – Методы класса User",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "getHandle()", "public", "String", "Возвращает username (handle)"],
            ["2", "getUsername()", "public", "String", "Возвращает phone (для Spring Security)"],
            ["3", "getAuthorities()", "public", "Collection<? extends GrantedAuthority>", "Возвращает пустой список (роли не используются)"],
            ["4", "addFavorite(Chat)", "public", "void", "Добавляет чат в избранное"],
            ["5", "removeFavorite(Chat)", "public", "void", "Удаляет чат из избранного"],
            ["6", "addContact(User, ...)", "public", "void", "Добавляет контакт (с переопределением имени)"],
            ["7", "removeContact(User)", "public", "void", "Удаляет контакт"],
            ["8", "getContactIds()", "public", "List<UUID>", "Список ID контактов"],
            ["9", "getFavoriteIds()", "public", "List<UUID>", "Список ID избранных чатов"],
        ]
    )

    add_normal(doc, "Класс Chat (Entity)", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "JPA-сущность чата. Поддерживает два типа: DIRECT (личный, 2 участника) "
        "и GROUP (групповой, любое число участников). Участники хранятся "
        "в связанных сущностях ChatMember.")

    add_spec_table(doc,
        "Таблица 3.3.6.3 – Поля класса Chat",
        ["№", "Поле", "Тип", "Ограничения", "Назначение"],
        [
            ["1", "id", "UUID", "@Id, @GeneratedValue(UUID)", "Первичный ключ"],
            ["2", "type", "ChatType", "NOT NULL, @Enumerated(STRING)", "Тип чата (DIRECT / GROUP)"],
            ["3", "name", "String", "—", "Название (только для GROUP)"],
            ["4", "avatarUrl", "String", "—", "URL аватара группы"],
            ["5", "members", "List<ChatMember>", "@OneToMany(CASCADE ALL)", "Участники чата"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.3.6.4 – Методы класса Chat",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "addMember(User)", "public", "void", "Добавляет участника в чат"],
            ["2", "removeMember(User)", "public", "void", "Удаляет участника из чата"],
            ["3", "getMemberIds()", "public", "List<UUID>", "Возвращает список ID участников"],
        ]
    )

    add_normal(doc, "3.3.7 Серверная часть: сервисы", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=12)

    add_normal(doc, "Класс AuthenticationService", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Сервис аутентификации. Реализует логику регистрации, входа и обновления "
        "токена. Использует Spring AuthenticationManager для проверки учётных данных "
        "и JwtService для генерации токенов.")

    add_spec_table(doc,
        "Таблица 3.3.7.1 – Методы класса AuthenticationService",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "signUp(SignUpRequest)", "public", "JwtAuthenticationResponse", "Регистрация: создаёт пользователя, возвращает токены"],
            ["2", "signIn(SignInRequest)", "public", "JwtAuthenticationResponse", "Вход: проверяет пароль, возвращает токены"],
            ["3", "refresh(RefreshTokenRequest)", "public", "JwtAuthenticationResponse", "Обновляет пару токенов по refresh-токену"],
            ["4", "buildResponse(User)", "private", "JwtAuthenticationResponse", "Генерирует access + refresh токены"],
        ]
    )

    add_normal(doc, "Класс JwtService", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Сервис работы с JWT-токенами. Генерирует access (15 мин) и refresh (7 дней) "
        "токены, подписанные HMAC-SHA256. Поддерживает также registration-токены "
        "для OAuth-регистрации с проверкой специального claim 'purpose'.")

    add_spec_table(doc,
        "Таблица 3.3.7.2 – Методы класса JwtService",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "generateToken(UserDetails)", "public", "String", "Генерирует access-токен с id и phone в claims"],
            ["2", "generateRefreshToken(UserDetails)", "public", "String", "Генерирует refresh-токен"],
            ["3", "extractSubject(token)", "public", "String", "Извлекает subject (phone) из токена"],
            ["4", "isTokenValid(token, userDetails)", "public", "boolean", "Проверяет подпись и срок действия"],
            ["5", "generateRegistrationToken(subject, claims)", "public", "String", "Генерирует registration-токен для OAuth"],
            ["6", "parseRegistrationToken(token)", "public", "Claims", "Парсит и валидирует registration-токен"],
        ]
    )

    add_normal(doc, "Класс UserService", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Сервис управления пользователями. Предоставляет CRUD-операции для "
        "пользователей, управление контактами и аватарами. Реализует UserDetailsService "
        "для Spring Security.")

    add_spec_table(doc,
        "Таблица 3.3.7.3 – Методы класса UserService",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "registerUser(SignUpRequest, encodedPassword)", "public", "User", "Создаёт пользователя при регистрации"],
            ["2", "getAllUsers()", "public", "List<UserResponse>", "Список всех пользователей"],
            ["3", "getUserById(UUID)", "public", "UserResponse", "Пользователь по ID"],
            ["4", "updateUser(UUID, CreateUserRequest)", "public", "UserResponse", "Обновляет профиль"],
            ["5", "deleteUserById(UUID)", "public", "void", "Удаляет пользователя"],
            ["6", "uploadAvatar(UUID, MultipartFile)", "public", "UserResponse", "Загружает аватар в MinIO"],
            ["7", "getContacts(UUID)", "public", "List<ContactResponse>", "Список контактов пользователя"],
            ["8", "addContact(UUID, AddContactRequest)", "public", "void", "Добавляет контакт"],
            ["9", "removeContact(UUID, UUID)", "public", "void", "Удаляет контакт"],
            ["10", "userDetailsService()", "public", "UserDetailsService", "Фабрика UserDetailsService для Spring Security"],
        ]
    )

    add_normal(doc, "Класс ChatService", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Сервис управления чатами. Реализует создание личных и групповых чатов, "
        "получение списка чатов, управление избранным. Проверяет уникальность "
        "личного чата между двумя пользователями.")

    add_spec_table(doc,
        "Таблица 3.3.7.4 – Методы класса ChatService",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "getAllChats(currentUserId)", "public", "List<ChatResponse>", "Все чаты пользователя"],
            ["2", "getChatById(id, currentUserId)", "public", "ChatResponse", "Чат по ID"],
            ["3", "createChat(CreateChatRequest, currentUserId)", "public", "ChatResponse", "Создаёт личный чат (проверяет уникальность)"],
            ["4", "createGroupChat(name, memberIds, avatar, currentUserId)", "public", "ChatResponse", "Создаёт групповой чат с аватаром"],
            ["5", "deleteChatById(id)", "public", "void", "Удаляет чат"],
            ["6", "getFavorites(userId)", "public", "List<FavoriteResponse>", "Список избранных чатов"],
            ["7", "addFavorite(AddFavoriteRequest, userId)", "public", "FavoriteResponse", "Добавляет чат в избранное"],
            ["8", "toResponse(Chat, currentUserId)", "private", "ChatResponse", "Преобразует Chat в DTO"],
        ]
    )

    add_normal(doc, "3.3.8 Серверная часть: фильтры и конфигурация", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=12)

    add_normal(doc, "Класс JwtAuthenticationFilter", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "OncePerRequestFilter, выполняющий JWT-аутентификацию для каждого "
        "входящего HTTP-запроса. Извлекает Bearer-токен из заголовка Authorization, "
        "валидирует его через JwtService и устанавливает Authentication "
        "в SecurityContextHolder.")

    add_spec_table(doc,
        "Таблица 3.3.8.1 – Методы класса JwtAuthenticationFilter",
        ["№", "Метод", "Доступ", "Возврат", "Назначение"],
        [
            ["1", "doFilterInternal(request, response, chain)", "protected", "void", "Основная логика фильтра: извлекает токен, аутентифицирует пользователя"],
        ]
    )

    add_normal(doc, "Класс SecurityConfig", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False, spacing_before=6)
    add_normal(doc,
        "Spring Security конфигурация. Определяет политику безопасности: "
        "STATELESS-сессии, публичные эндпоинты (/api/auth/**, Swagger), "
        "BCrypt-кодировщик паролей, DaoAuthenticationProvider и FilterRegistrationBean "
        "для предотвращения двойной регистрации JwtAuthenticationFilter.")

    # 3.4
    add_heading2(doc, "3.4 UML-диаграмма классов")

    add_normal(doc,
        "На рисунке 3.4 представлена UML-диаграмма классов, отражающая "
        "архитектуру разработанных компонентов. Диаграмма охватывает как "
        "Android-клиент (подсистемы login, chats, settings, core), "
        "так и серверную часть (слои entity, service, controller).")

    add_placeholder(doc,
        "[ МЕСТО ДЛЯ UML ДИАГРАММЫ КЛАССОВ ]\n"
        "Диаграмма классов Android + Backend (PlantUML / draw.io / StarUML)")
    add_caption(doc, "Рисунок 3.4 – UML-диаграмма классов системы")

    add_normal(doc,
        "Как видно из диаграммы (рисунок 3.4), Android-клиент организован "
        "по паттерну MVVM: View (Fragment) → ViewModel → Repository → API. "
        "Серверная часть следует трёхуровневой архитектуре: "
        "Controller → Service → Repository (Spring Data JPA).")

    # 3.5
    add_heading2(doc, "3.5 Взаимодействие с базой данных")

    add_normal(doc,
        "Серверная часть использует PostgreSQL в качестве реляционной базы данных. "
        "Взаимодействие с БД осуществляется через Spring Data JPA (Hibernate). "
        "Схема базы данных формируется автоматически при запуске "
        "(hibernate.ddl-auto=update).")

    add_normal(doc, "Основные таблицы базы данных:")

    add_spec_table(doc,
        "Таблица 3.5.1 – Таблица users",
        ["Поле", "Тип", "Ограничения", "Описание"],
        [
            ["id", "UUID", "PRIMARY KEY", "Первичный ключ (UUID, автогенерация)"],
            ["first_name", "VARCHAR", "NOT NULL", "Имя пользователя"],
            ["last_name", "VARCHAR", "—", "Фамилия пользователя"],
            ["username", "VARCHAR(32)", "UNIQUE, NOT NULL", "Уникальное отображаемое имя"],
            ["phone", "VARCHAR(20)", "UNIQUE, NOT NULL", "Номер телефона (principal)"],
            ["email", "VARCHAR(254)", "UNIQUE", "Электронная почта"],
            ["avatar_url", "VARCHAR", "—", "URL аватара в MinIO"],
            ["bio", "TEXT", "—", "Биография"],
            ["password", "VARCHAR", "NOT NULL", "BCrypt-хеш пароля"],
            ["notifications", "BOOLEAN", "NOT NULL, DEFAULT true", "Флаг уведомлений"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.5.2 – Таблица chat",
        ["Поле", "Тип", "Ограничения", "Описание"],
        [
            ["id", "UUID", "PRIMARY KEY", "Первичный ключ"],
            ["type", "VARCHAR", "NOT NULL", "Тип чата: DIRECT или GROUP"],
            ["name", "VARCHAR", "—", "Название группового чата"],
            ["avatar_url", "VARCHAR", "—", "URL аватара группы"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.5.3 – Таблица chat_member",
        ["Поле", "Тип", "Ограничения", "Описание"],
        [
            ["id", "UUID", "PRIMARY KEY", "Первичный ключ"],
            ["chat_id", "UUID", "FK → chat.id", "Ссылка на чат"],
            ["user_id", "UUID", "FK → users.id", "Ссылка на пользователя"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.5.4 – Таблица user_contact",
        ["Поле", "Тип", "Ограничения", "Описание"],
        [
            ["id", "UUID", "PRIMARY KEY", "Первичный ключ"],
            ["user_id", "UUID", "FK → users.id", "Владелец записи контакта"],
            ["contact_id", "UUID", "FK → users.id", "Пользователь-контакт"],
            ["first_name_override", "VARCHAR", "—", "Переопределённое имя контакта"],
            ["last_name_override", "VARCHAR", "—", "Переопределённая фамилия контакта"],
        ]
    )

    add_spec_table(doc,
        "Таблица 3.5.5 – Таблица user_favorites (ManyToMany)",
        ["Поле", "Тип", "Ограничения", "Описание"],
        [
            ["chat_id", "UUID", "FK → chat.id, UNIQUE(chat_id, favorite_id)", "Ссылка на чат"],
            ["favorite_id", "UUID", "FK → users.id", "Ссылка на пользователя"],
        ]
    )

    add_normal(doc,
        "Интерфейс UserRepository расширяет JpaRepository<User, UUID> и предоставляет "
        "дополнительные методы: findByPhone(), findByUsername(), existsByPhone(), "
        "existsByUsername(), existsByEmail() — используемые при регистрации для "
        "проверки уникальности.")

    add_normal(doc,
        "Интерфейс ChatRepository расширяет JpaRepository<Chat, UUID>. "
        "Содержит метод findDirectChatWithExactMembers(memberIds, size), "
        "который проверяет уникальность личного чата между двумя пользователями "
        "через JPQL-запрос с подсчётом совпадений.")

    # 3.6
    add_heading2(doc, "3.6 Тестирование")

    add_normal(doc,
        "Для проверки корректности работы ключевых компонентов были написаны "
        "модульные тесты с использованием JUnit 5 и Spring Test. "
        "Основное покрытие направлено на JwtService как критически важный компонент "
        "безопасности системы.")

    add_normal(doc, "3.6.1 Тестирование JwtService", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Класс JwtServiceTest содержит следующие тест-кейсы:")

    add_spec_table(doc,
        "Таблица 3.6.1 – Тест-кейсы JwtServiceTest",
        ["№", "Тест", "Описание", "Ожидаемый результат"],
        [
            ["1", "generateAndExtractAccessToken", "Генерация access-токена и проверка subject", "subject равен phone пользователя, токен валиден"],
            ["2", "generateRefreshTokenContainsSubject", "Генерация refresh-токена", "subject токена равен phone пользователя"],
            ["3", "registrationTokenHasPurposeClaim", "Генерация registration-токена с claims", "Claims содержат purpose=oauth-register и переданные поля"],
            ["4", "parseRegistrationTokenRejectsAccessToken", "Попытка использовать access-токен как registration", "Выброс IllegalArgumentException"],
            ["5", "parseRegistrationTokenRejectsExpiredToken", "Истёкший registration-токен", "Выброс RegistrationTokenExpiredException"],
            ["6", "parseRegistrationTokenRejectsMalformed", "Невалидный токен", "Выброс IllegalArgumentException"],
        ]
    )

    add_normal(doc,
        "Все тесты проходят успешно. Для изоляции тестов используется "
        "ReflectionTestUtils для установки приватных полей (jwtSecret, expirationMs и др.) "
        "без запуска Spring-контекста.")

    add_normal(doc, "3.6.2 Функциональное тестирование", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Помимо модульных тестов, проведено ручное функциональное тестирование "
        "основных пользовательских сценариев на Android-эмуляторе (API 34):")

    add_spec_table(doc,
        "Таблица 3.6.2 – Результаты функционального тестирования",
        ["№", "Сценарий", "Шаги", "Результат"],
        [
            ["1", "Регистрация", "Ввод телефона, пароля, имени и username → нажать «Готово»", "Успешный переход на главный экран, токены сохранены"],
            ["2", "Вход по паролю", "Ввод телефона и пароля → нажать «Войти»", "Переход на главный экран"],
            ["3", "Неверный пароль", "Ввод корректного телефона и неверного пароля", "Toast с сообщением об ошибке"],
            ["4", "Автообновление токена", "Ждать истечения access-токена (15 мин), выполнить запрос", "Токен обновляется автоматически, запрос выполняется"],
            ["5", "Просмотр списка чатов", "Открыть приложение после входа", "Список чатов загружается из API"],
            ["6", "Создание личного чата", "Нажать «+» → выбрать контакт → создать", "Чат появляется в списке"],
            ["7", "Выход из аккаунта", "Настройки → Выйти", "Токены очищены, переход на экран входа"],
            ["8", "Passkey вход", "Нажать «Войти с Passkey», пройти биометрию", "Успешный вход, токены сохранены"],
        ]
    )

    add_placeholder(doc,
        "[ МЕСТО ДЛЯ СКРИНШОТОВ ТЕСТИРОВАНИЯ ]\n"
        "Вставьте скриншоты экранов приложения при выполнении тест-кейсов")
    add_caption(doc, "Рисунок 3.5 – Скриншоты при тестировании приложения")

    # 3.7
    add_heading2(doc, "3.7 Руководство пользователя")

    add_normal(doc,
        "Данный раздел описывает порядок работы с приложением Flux Messenger. "
        "Минимальная версия Android — 8.0 (API 26). Для работы Passkey требуется "
        "Android 9+ с поддержкой биометрии.")

    add_normal(doc, "3.7.1 Установка и первый запуск", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Установите APK-файл приложения на устройство Android. "
        "При первом запуске отображается приветственный экран (рисунок 3.6) "
        "с тремя кнопками: «Войти», «Зарегистрироваться» и «Войти с Passkey».")

    add_placeholder(doc, "[ СКРИНШОТ: Приветственный экран ]")
    add_caption(doc, "Рисунок 3.6 – Приветственный экран")

    add_normal(doc, "3.7.2 Регистрация нового аккаунта", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Для регистрации нажмите «Зарегистрироваться». "
        "Откроется форма ввода номера телефона и пароля (рисунок 3.7). "
        "Номер телефона должен содержать не менее 11 цифр, пароль — не менее 6 символов. "
        "Поле подтверждения пароля должно совпадать с паролем. "
        "Нажмите «Далее» для перехода к следующему шагу.")

    add_placeholder(doc, "[ СКРИНШОТ: Экран регистрации (шаг 1) ]")
    add_caption(doc, "Рисунок 3.7 – Экран регистрации: ввод телефона и пароля")

    add_normal(doc,
        "На втором шаге (рисунок 3.8) введите имя (обязательно), фамилию (опционально) "
        "и имя пользователя (username). Username должен содержать от 3 символов "
        "и состоять только из латинских букв, цифр и символа «_». "
        "Нажмите «Готово» для завершения регистрации.")

    add_placeholder(doc, "[ СКРИНШОТ: Экран регистрации (шаг 2) ]")
    add_caption(doc, "Рисунок 3.8 – Экран регистрации: ввод имени и username")

    add_normal(doc, "3.7.3 Вход в существующий аккаунт", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Нажмите «Войти» на приветственном экране. Введите номер телефона и пароль "
        "(рисунок 3.9). Кнопка «Войти» активируется автоматически после корректного "
        "заполнения полей. При успешной аутентификации вы попадёте на главный экран приложения.")

    add_placeholder(doc, "[ СКРИНШОТ: Экран входа ]")
    add_caption(doc, "Рисунок 3.9 – Экран входа в аккаунт")

    add_normal(doc, "3.7.4 Вход через Passkey", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Нажмите «Войти с Passkey» для беспарольной аутентификации. "
        "Система запросит подтверждение биометрией или PIN-кодом устройства. "
        "При первом использовании Passkey потребуется пройти шаг регистрации ключа: "
        "введите номер телефона, имя и username, затем подтвердите создание ключа на устройстве.")

    add_normal(doc, "3.7.5 Работа со списком чатов", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "После входа отображается главный экран со списком чатов (рисунок 3.10). "
        "В верхней части — горизонтальный список избранных чатов. "
        "Ниже — сегментированные вкладки: «Все», «Личные», «Группы» для фильтрации. "
        "Для поиска чата нажмите иконку поиска — откроется строка поиска. "
        "Для создания нового чата нажмите кнопку «+» в правом нижнем углу.")

    add_placeholder(doc, "[ СКРИНШОТ: Список чатов ]")
    add_caption(doc, "Рисунок 3.10 – Главный экран со списком чатов")

    add_normal(doc, "3.7.6 Создание нового чата", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Нажмите кнопку «+» для открытия нижней панели (рисунок 3.11). "
        "Выберите «Новый контакт» для добавления пользователя в адресную книгу, "
        "или выберите существующий контакт из списка для начала личного диалога. "
        "Для создания группового чата нажмите «Новая группа»: "
        "выберите участников и на следующем экране введите название группы и загрузите аватар.")

    add_placeholder(doc, "[ СКРИНШОТ: Создание нового чата ]")
    add_caption(doc, "Рисунок 3.11 – Панель создания нового чата")

    add_normal(doc, "3.7.7 Управление профилем", bold=True,
               align=WD_ALIGN_PARAGRAPH.LEFT, indent_first=False)

    add_normal(doc,
        "Перейдите в раздел «Настройки» через нижнюю навигацию. "
        "На экране настроек (рисунок 3.12) отображаются имя, username, биография "
        "и аватар пользователя. Нажмите «Профиль» для редактирования данных. "
        "Кнопка «Выйти» завершает сеанс и возвращает к экрану входа.")

    add_placeholder(doc, "[ СКРИНШОТ: Экран настроек ]")
    add_caption(doc, "Рисунок 3.12 – Экран настроек и профиля")

    add_page_break(doc)


# ══════════════════════════════════════════════════════════════════════════
# ЗАКЛЮЧЕНИЕ
# ══════════════════════════════════════════════════════════════════════════

def section_conclusion(doc):
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.CENTER, indent_first=False,
             spacing_before=0, spacing_after=12)
    run = p.add_run("ЗАКЛЮЧЕНИЕ")
    set_run_font(run, size=14, bold=True)

    add_normal(doc,
        "В ходе выполнения курсовой работы была разработана Android-часть мессенджера "
        "Flux Messenger, а также серверная REST API на базе Spring Boot 4. "
        "Все поставленные задачи выполнены в полном объёме.")

    add_normal(doc, "В результате работы:")
    add_bullet(doc,
        "проведён анализ предметной области — изучены принципы работы мессенджеров, "
        "рассмотрены аналоги и сформулированы требования к системе;")
    add_bullet(doc,
        "спроектирована архитектура приложения по паттерну MVVM (Model–View–ViewModel) "
        "для Android-клиента и трёхуровневая архитектура (Controller–Service–Repository) "
        "для серверной части;")
    add_bullet(doc,
        "реализована подсистема аутентификации, включающая стандартный вход и регистрацию "
        "по номеру телефона и паролю, а также современный механизм беспарольного входа "
        "через Passkey (WebAuthn/FIDO2);")
    add_bullet(doc,
        "разработан серверный REST API с JWT-авторизацией (access-токен 15 мин, "
        "refresh-токен 7 дней), автоматическим обновлением токенов на клиенте через "
        "OkHttp AuthInterceptor;")
    add_bullet(doc,
        "реализованы экраны: приветственный экран, вход, двухэтапная регистрация, "
        "список чатов с фильтрацией и поиском, создание личного и группового чатов, "
        "настройки профиля;")
    add_bullet(doc,
        "обеспечена безопасность хранения токенов на устройстве с использованием "
        "EncryptedSharedPreferences (AES-256-GCM), что защищает учётные данные "
        "от несанкционированного доступа;")
    add_bullet(doc,
        "написаны модульные тесты для JwtService, проведено ручное функциональное "
        "тестирование всех ключевых сценариев.")

    add_normal(doc,
        "Разработанное приложение демонстрирует применение современного стека технологий: "
        "Retrofit 3, Android Navigation Component, AndroidX Credentials, Spring Security, "
        "Spring Data JPA, MinIO — в рамках реального клиент-серверного проекта. "
        "Полученные знания и навыки могут быть применены при разработке "
        "коммерческих мобильных приложений.")

    add_normal(doc,
        "В качестве направлений дальнейшего развития проекта можно выделить: "
        "добавление real-time обмена сообщениями через WebSocket, "
        "реализацию push-уведомлений, end-to-end шифрование переписки, "
        "поддержку медиафайлов в сообщениях.")

    add_page_break(doc)


# ══════════════════════════════════════════════════════════════════════════
# СПИСОК ЛИТЕРАТУРЫ
# ══════════════════════════════════════════════════════════════════════════

def section_references(doc):
    p = doc.add_paragraph()
    fmt_para(p, align=WD_ALIGN_PARAGRAPH.CENTER, indent_first=False,
             spacing_before=0, spacing_after=12)
    run = p.add_run("СПИСОК ЛИТЕРАТУРЫ")
    set_run_font(run, size=14, bold=True)

    refs = [
        "Bloch J. Effective Java. 3rd ed. — Addison-Wesley Professional, 2018. — 412 с.",
        "Godfrey N. Android Programming: The Big Nerd Ranch Guide. 4th ed. — Big Nerd Ranch, 2019. — 624 с.",
        "Walls C. Spring in Action. 6th ed. — Manning Publications, 2022. — 520 с.",
        "Фримен Э. Head First. Паттерны проектирования. Обновлённое юбилейное издание. — СПб.: Питер, 2022. — 672 с.",
        "JSON Web Token (JWT). RFC 7519. — IETF, 2015. URL: https://tools.ietf.org/html/rfc7519.",
        "Web Authentication: An API for accessing Public Key Credentials. W3C Recommendation. — W3C, 2021. URL: https://www.w3.org/TR/webauthn-2/.",
        "Android Developers. Credential Manager. — Google, 2023. URL: https://developer.android.com/jetpack/androidx/releases/credentials.",
        "Spring Security Reference Documentation. Spring Framework 6.x. URL: https://docs.spring.io/spring-security/reference/.",
        "Retrofit 3. A type-safe HTTP client for Android and Java. — Square Inc., 2024. URL: https://square.github.io/retrofit/.",
        "PostgreSQL 15 Documentation. — The PostgreSQL Global Development Group, 2023. URL: https://www.postgresql.org/docs/15/.",
        "MinIO High Performance Object Storage. URL: https://min.io/docs/minio/container/.",
        "ГОСТ Р 7.0.5–2008. Библиографическая ссылка. Общие требования и правила составления. — М.: Стандартинформ, 2008.",
    ]

    for i, ref in enumerate(refs, 1):
        p = doc.add_paragraph()
        fmt_para(p, align=WD_ALIGN_PARAGRAPH.JUSTIFY, indent_first=False,
                 spacing_before=3, spacing_after=3)
        pf = p.paragraph_format
        pf.left_indent = Cm(1.25)
        pf.first_line_indent = Cm(-1.25)
        run = p.add_run(f"{i}. {ref}")
        set_run_font(run, size=12)


# ══════════════════════════════════════════════════════════════════════════
# MAIN
# ══════════════════════════════════════════════════════════════════════════

title_page(doc)
toc_page(doc)
section_intro(doc)
section1(doc)
section2(doc)
section3(doc)
section_conclusion(doc)
section_references(doc)

out_path = "/home/user/Flux-Messenger/курсовая_Flux_Messenger.docx"
doc.save(out_path)
print(f"Saved: {out_path}")
