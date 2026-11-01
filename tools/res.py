"""Writes the string resources (English and Russian) from one table, so the two never drift apart.

python tools/res.py
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"

S = [
    ("app_name", "Matryoshka", "Матрёшка"),
    ("tagline", "A present opened layer by layer. Every figure has its own lock: a moment, a riddle, a place, someone else’s word.",
     "Подарок, который открывают слой за слоем. У каждой фигурки свой замок: время, загадка, место, чужое слово."),
    ("tab_shelf", "Shelf", "Полка"),
    ("tab_workshop", "Workshop", "Мастерская"),
    ("settings", "Settings", "Настройки"),
    ("next", "Next", "Дальше"),
    ("back", "Back", "Назад"),
    ("cancel", "Cancel", "Отмена"),
    ("delete", "Delete", "Удалить"),
    ("start", "Start painting", "Начать роспись"),
    ("you", "you", "вы"),
    # shelf
    ("make", "Make a matryoshka", "Собрать матрёшку"),
    ("make_sub", "The present is ready? Hide it in the smallest figure and decide how to get to it.",
     "Подарок уже есть? Спрячьте его в самую маленькую фигурку и решите, как до неё добираться."),
    ("shelf_got", "Given to me", "Мне подарили"),
    ("shelf_got_empty", "When someone sends you a matryoshka, it will stand here. Tap the .matr file in the messenger.",
     "Когда вам пришлют матрёшку, она встанет сюда. Просто нажмите на файл .matr в мессенджере."),
    ("shelf_given", "I gave", "Я подарил(а)"),
    ("shelf_given_empty", "The matryoshkas you seal or paint a figure of will stand here, with the replies.",
     "Здесь встанут матрёшки, которые вы запечатали или для которых расписали фигурку. И отклики на них."),
    ("open_file", "Open a .matr file", "Открыть файл .matr"),
    ("open_file_sub", "If the messenger didn’t offer Matryoshka, save the file and open it here.",
     "Если мессенджер не предложил «Матрёшку», сохраните файл и откройте его отсюда."),
    ("for_you", "For you", "Для вас"),
    ("from_x", "from %1$s", "от: %1$s"),
    ("to_x", "For %1$s", "Кому: %1$s"),
    ("state_done", "all opened", "открыта целиком"),
    ("state_wait", "opens %1$s", "откроется %1$s"),
    ("state_opened", "%1$d of %2$d opened", "открыто %1$d из %2$d"),
    ("state_sent", "sent", "отправлена"),
    ("state_passed", "passed on", "передана дальше"),
    ("bad_file", "This file isn’t a matryoshka, or it got damaged on the way.", "Это не матрёшка, или файл повредился по дороге."),
    ("reply_came", "%1$s sent a reply", "Пришёл отклик: %1$s"),
    # given
    ("gave_unknown", "This matryoshka isn’t on your shelf. Maybe it was removed.", "Этой матрёшки нет на вашей полке. Возможно, её убрали."),
    ("painted_by", "Painted by", "Расписывали"),
    ("gave_sealed", "Sealed %1$s.", "Запечатана %1$s."),
    ("gave_passed", "You painted figure %1$d and passed it on. The last one in the chain seals it.",
     "Вы расписали фигурку %1$d и передали матрёшку дальше. Печать ставит последний в цепочке."),
    ("replies", "Replies", "Отклики"),
    ("replies_none", "Replies come as a file once the matryoshka is opened. Open that file and they’ll appear here.",
     "Отклики придут файлом, когда матрёшку откроют. Откройте этот файл, и они появятся здесь."),
    ("reply_from", "%1$s, %2$s", "%1$s, %2$s"),
    ("reply_empty", "No words, just a thank-you.", "Без слов, просто спасибо."),
    ("figure_n", "Figure %1$d", "Фигурка %1$d"),
    ("send_again", "Send the file again", "Отправить файл ещё раз"),
    ("forget", "Remove from the shelf", "Убрать с полки"),
    ("forget_q", "Remove from the shelf?", "Убрать с полки?"),
    ("forget_given_b", "The record and its replies will go. The matryoshka you sent stays with whoever has it.",
     "Исчезнет запись и отклики к ней. Отправленная матрёшка останется у того, кому вы её подарили."),
    # workshop
    ("workshop_sub", "Drafts and your turns in shared matryoshkas.", "Черновики и ваша очередь в общих матрёшках."),
    ("new_doll", "New matryoshka", "Новая матрёшка"),
    ("drafts_empty", "No drafts yet.", "Черновиков пока нет."),
    ("draft_turn", "your figure: %1$d of %2$d", "ваша фигурка: %1$d из %2$d"),
    ("draft_gone", "This draft is gone: it was sealed or passed on.", "Этого черновика уже нет: его запечатали или передали дальше."),
    ("how_t", "How it works", "Как это устроено"),
    ("how_b", "You assemble the matryoshka from the outside in. Each figure holds a note, a photo or a voice and has a lock. The smallest one is solid and holds the present itself. A sealed matryoshka is a single file: send it in any messenger. Riddles and code words are real encryption: without the answer the file can’t be opened, even taken apart.",
     "Матрёшку собирают снаружи внутрь. В каждой фигурке записка, фото или голос, и у каждой свой замок. Самая маленькая цельная, в ней лежит сам подарок. Запечатанная матрёшка — это один файл, его можно отправить в любом мессенджере. Загадки и кодовые слова — настоящее шифрование: без ответа файл не вскрыть, даже если разобрать его по частям."),
    ("f_to", "For whom", "Кому"),
    ("f_to_hint", "Anna", "Ане"),
    ("f_title", "What for", "По какому поводу"),
    ("f_title_hint", "For your 30th", "На 30-летие"),
    ("f_me", "Your name", "Ваше имя"),
    ("f_me_hint", "How to sign your figures", "Так будут подписаны ваши фигурки"),
    ("mode_alone", "On my own", "Сам(а)"),
    ("mode_together", "Together", "Вместе"),
    ("mode_alone_b", "You paint every figure yourself.", "Все фигурки расписываете вы."),
    ("mode_together_b", "The matryoshka goes round as a file: everyone paints one figure and doesn’t see the others. You start and put in the present; the last one seals it.",
     "Матрёшка пойдёт по кругу файлом: каждый расписывает одну фигурку и не видит чужих. Вы начинаете и кладёте подарок, последний ставит печать."),
    ("f_people", "In this order", "В таком порядке"),
    ("f_person_hint", "Name", "Имя"),
    ("add_person", "Add someone", "Добавить человека"),
    # editor
    ("figure_n_paint", "Figure %1$d · painting", "Фигурка %1$d · роспись"),
    ("f_author_hint", "Who painted it", "Кто расписал"),
    ("inside", "Inside", "Внутри"),
    ("lock", "Lock", "Замок"),
    ("remove_figure", "Remove this figure", "Убрать эту фигурку"),
    ("figure_hidden", "Painted by %1$s. What’s inside stays hidden from everyone until the day itself.",
     "Роспись: %1$s. Что внутри, не увидит никто до самого праздника."),
    ("gift_hidden", "The present in the smallest figure is from %1$s. It’s a surprise for you too.",
     "Подарок в самую маленькую фигурку кладёт %1$s. Для вас это тоже сюрприз."),
    ("relay_after", "After you: %1$s", "После вас: %1$s"),
    ("relay_you_last", "You’re the last: you seal it and send it to the one it’s for.", "Ваша очередь последняя: вы ставите печать и отправляете матрёшку виновнику торжества."),
    ("gift_t", "The smallest figure", "Самая маленькая фигурка"),
    ("gift_b", "It doesn’t open: it’s solid. It holds the present itself, or where to find it.",
     "Она не открывается, она цельная. В ней сам подарок или то, где его искать."),
    ("gift_hint", "Look in the wardrobe: top shelf, behind the jumpers.", "Загляни в шкаф: верхняя полка, за свитерами."),
    ("note_hint", "A note", "Записка"),
    ("link_hint", "A link (tickets, a certificate…)", "Ссылка (билеты, сертификат…)"),
    ("photo_add", "Add a photo", "Добавить фото"),
    ("photo_remove", "Remove the photo", "Убрать фото"),
    ("photo_failed", "Couldn’t read that photo.", "Не получилось прочитать это фото."),
    ("voice_add", "Record a voice", "Записать голос"),
    ("voice_stop", "Stop · %1$s", "Стоп · %1$s"),
    ("voice_remove", "Remove the voice", "Убрать голос"),
    ("mic_failed", "The microphone is busy or unavailable.", "Микрофон занят или недоступен."),
    ("lock_none", "No lock", "Без замка"),
    ("lock_time", "Time", "Время"),
    ("lock_riddle", "Riddle", "Загадка"),
    ("lock_place", "Place", "Место"),
    ("lock_word", "Someone’s word", "Чужое слово"),
    ("lock_gesture", "Gesture", "Жест"),
    ("lock_none_b0", "The outer figure opens straight away.", "Внешняя фигурка откроется сразу."),
    ("lock_none_b", "Opens right after the one around it.", "Откроется сразу после предыдущей."),
    ("lock_time_b", "Opens at a moment you choose. The phone will quietly say when.", "Откроется в выбранный момент. Телефон тихо подскажет, когда пора."),
    ("lock_riddle_b", "The answer is the key: without it the figure can’t be opened, even by taking the file apart. Case and punctuation don’t matter.",
     "Ответ — это ключ: без него фигурку не вскрыть, даже если разобрать файл. Регистр, «ё» и знаки препинания не важны."),
    ("lock_place_b", "Opens only near the place. The phone checks it itself; the coordinates go nowhere.",
     "Откроется только рядом с местом. Телефон проверяет это сам, координаты никуда не уходят."),
    ("lock_word_b", "Someone else knows the word: a grandmother, a friend. Tell them the word yourself beforehand.",
     "Слово знает другой человек: бабушка, друг. Скажите ему слово сами, заранее."),
    ("lock_gesture_b", "Opens with the hands: cover it with a palm, shake it, turn the phone over.", "Откроется руками: накрыть ладонью, потрясти, перевернуть телефон."),
    ("f_question", "The question", "Вопрос"),
    ("f_answer", "The answer (the key)", "Ответ (он же ключ)"),
    ("f_hint", "A hint after a wrong answer (optional)", "Подсказка после неверного ответа (можно без неё)"),
    ("f_who", "Who knows the word", "Кто знает слово"),
    ("f_word", "The code word", "Кодовое слово"),
    ("f_place", "What the place is called", "Как называется место"),
    ("f_coords", "Coordinates or a map link", "Координаты или ссылка на карту"),
    ("coords_help", "Paste a link from Yandex or Google Maps, or “55.7558, 37.6173”.", "Вставьте ссылку из Яндекс или Google Карт или «55.7558, 37.6173»."),
    ("where_here", "I’m here right now", "Я сейчас здесь"),
    ("where_finding", "Finding where you are…", "Определяем место…"),
    ("where_failed", "Couldn’t find where you are. Is location on?", "Не удалось определить место. Геолокация включена?"),
    ("radius_m", "%1$d m", "%1$d м"),
    ("time_none", "Not chosen", "Не выбрано"),
    ("time_hour", "In an hour", "Через час"),
    ("time_morning", "Tomorrow, 8:00", "Завтра в 8:00"),
    ("time_evening", "Tomorrow, 19:00", "Завтра в 19:00"),
    ("time_pick", "Choose a date and time", "Выбрать дату и время"),
    ("g_cover", "Cover with a palm", "Накрыть ладонью"),
    ("g_shake", "Shake", "Потрясти"),
    ("g_flip", "Turn over", "Перевернуть"),
    ("p_question", "Figure %1$d: write the riddle.", "Фигурка %1$d: напишите загадку."),
    ("p_answer", "Figure %1$d: the riddle needs an answer.", "Фигурка %1$d: у загадки нет ответа."),
    ("p_who", "Figure %1$d: who knows the word?", "Фигурка %1$d: кто знает слово?"),
    ("p_word", "Figure %1$d: choose the code word.", "Фигурка %1$d: придумайте кодовое слово."),
    ("p_time", "Figure %1$d: choose when it opens.", "Фигурка %1$d: выберите, когда она откроется."),
    ("p_place", "Figure %1$d: choose the place.", "Фигурка %1$d: выберите место."),
    ("p_gift", "Put the present (or where to find it) into the smallest figure.", "Положите в самую маленькую фигурку подарок или то, где его искать."),
    ("seal", "Seal the matryoshka", "Запечатать матрёшку"),
    ("pass_to", "Pass it to %1$s", "Передать: %1$s"),
    ("seal_t", "Seal it?", "Запечатать?"),
    ("pass_t", "Pass it on?", "Передать дальше?"),
    ("seal_letter", "M", "М"),
    ("seal_b", "After the seal nothing can be changed. Riddles and words are real encryption (locks like that: %1$d). Send the file %2$s in any messenger.",
     "После печати ничего не поменять. Загадки и слова — настоящее шифрование (таких замков: %1$d). Отправьте файл через любой мессенджер. Кому: %2$s."),
    ("pass_b", "The file goes to %1$s. They’ll paint the next figure and won’t see yours.",
     "Файл уйдёт к следующему: %1$s. Он распишет свою фигурку и вашу не увидит."),
    ("seal_go", "Seal and send", "Запечатать и отправить"),
    ("pass_go", "Send the file", "Отправить файл"),
    ("not_yet", "Not yet", "Пока нет"),
    ("sealed_toast", "Sealed. The matryoshka is on your shelf.", "Запечатано. Матрёшка стоит у вас на полке."),
    ("passed_toast", "Passed on. Your figure is on your shelf.", "Передано дальше. Ваша фигурка стоит у вас на полке."),
    ("delete_draft_q", "Delete the draft?", "Удалить черновик?"),
    ("delete_draft_b", "The figures painted so far will be lost.", "Расписанные фигурки пропадут."),
    # opening
    ("open_gone", "This matryoshka isn’t on the shelf any more.", "Этой матрёшки больше нет на полке."),
    ("open_broken", "This figure won’t open: the file seems damaged.", "Фигурка не открывается: похоже, файл повреждён."),
    ("smallest", "The smallest", "Самая маленькая"),
    ("smallest_b", "She doesn’t open: she’s solid. There’s nothing inside her but the present.", "Она не открывается, она цельная. Внутри неё только подарок."),
    ("tap_it", "Touch her.", "Коснитесь её."),
    ("figure_of", "Figure %1$d of %2$d", "Фигурка %1$d из %2$d"),
    ("unlocked", "The lock is open", "Замок открыт"),
    ("opened_t", "Opened", "Открыта"),
    ("unlocked_b", "Touch the matryoshka: she’ll come apart at the waist.", "Коснитесь матрёшки, и она разнимется по поясу."),
    ("painted_x", "Painted by %1$s", "Роспись: %1$s"),
    ("still_locked", "Still locked: %1$s", "Замок ещё закрыт: %1$s"),
    ("time_opens", "Opens %1$s.", "Откроется %1$s."),
    ("time_note", "The phone will quietly say when it’s time.", "Телефон тихо подскажет, когда пора."),
    ("word_q", "The word is known by: %1$s", "Слово знает: %1$s"),
    ("word_hint", "The code word", "Кодовое слово"),
    ("answer_hint", "The answer", "Ответ"),
    ("open_it", "Open", "Открыть"),
    ("checking", "…", "…"),
    ("wrong", "Not that. Try again.", "Не то. Попробуйте ещё."),
    ("wrong_hint", "Not that. Hint: %1$s", "Не то. Подсказка: %1$s"),
    ("secret_note", "The answer is the key: there’s no way to peek at it in the file.", "Ответ — это ключ: подсмотреть его в файле нельзя."),
    ("place_q", "Opens here: %1$s", "Откроется здесь: %1$s"),
    ("place_some", "a special place", "одно особенное место"),
    ("place_need", "To know you’re there, the phone needs your location. It stays on the phone.", "Чтобы понять, что вы на месте, телефону нужна геолокация. Она никуда не уходит."),
    ("place_allow", "Allow location", "Разрешить геолокацию"),
    ("place_dir", "%1$s · opens within %2$d m", "%1$s · откроется ближе %2$d м"),
    ("place_map", "Show on the map", "Показать на карте"),
    ("km", "%1$s km", "%1$s км"),
    ("m", "%1$d m", "%1$d м"),
    ("g_cover_do", "Cover the matryoshka with your palm and hold until she warms up. Or press and hold her.",
     "Накройте матрёшку ладонью и подержите, пока не согреется. Или зажмите её пальцем."),
    ("g_shake_do", "Shake the phone, like a box with something inside.", "Потрясите телефон, как коробку, в которой что-то есть."),
    ("g_flip_do", "Put the phone face down for a second.", "Положите телефон экраном вниз на секунду."),
    ("reaction", "A word back", "Пара слов в ответ"),
    ("reaction_hint", "What you felt when you opened it", "Что вы почувствовали, когда открыли"),
    ("reaction_voice", "Record a voice reply", "Ответить голосом"),
    ("reaction_note", "Replies go back as a file when you decide to send them.", "Отклики уйдут дарителям файлом, когда вы сами решите."),
    ("next_figure", "The next figure", "Следующая фигурка"),
    ("send_reply", "Send the replies", "Отправить отклики"),
    ("send_reply_again", "Send the replies again", "Отправить отклики ещё раз"),
    ("to_shelf", "Put her on the shelf", "Поставить на полку"),
    ("review_t", "Everything inside", "Всё, что было внутри"),
    ("you_said", "You replied: %1$s", "Ваш отклик: %1$s"),
    # dates
    ("day_time", "%1$s, %2$s", "%1$s, в %2$s"),
    ("left_dh", "%1$d d %2$d h", "%1$d дн. %2$d ч"),
    ("left_hm", "%1$d h %2$d min", "%1$d ч %2$d мин"),
    ("left_m", "%1$d min", "%1$d мин"),
    ("left_s", "less than a minute", "меньше минуты"),
    # notification
    ("n_channel", "Figures ready to open", "Фигурки, которые можно открыть"),
    ("n_title", "%1$s: the next figure can be opened", "«%1$s»: следующую фигурку можно открыть"),
    ("n_body", "Its time has come.", "Её время пришло."),
    # onboarding
    ("ob1", "A present that is opened layer by layer. Each figure has its own lock, and the present itself lies in the smallest one.",
     "Подарок, который открывают слой за слоем. У каждой фигурки свой замок, а сам подарок лежит в самой маленькой."),
    ("ob2_t", "Five kinds of lock", "Пять видов замков"),
    ("ob2", "You choose how each figure opens.", "Вы решаете, как открывается каждая фигурка."),
    ("ob_time", "On the birthday morning, or an hour after the last one", "Утром в день рождения или через час после предыдущей"),
    ("ob_riddle", "Only the one it’s for knows the answer", "Ответ знает только тот, кому дарят"),
    ("ob_place", "Opens only near the café where you met", "Откроется только у кафе, где вы познакомились"),
    ("ob_word", "“Ask your gran for the word”", "«Спроси кодовое слово у бабушки»"),
    ("ob_gesture", "Cover it with a palm, shake it, turn it over", "Накрыть ладонью, потрясти, перевернуть"),
    ("ob3_t", "Together, without a server", "Вместе и без сервера"),
    ("ob3", "A matryoshka is a single file: it goes in any messenger. A shared one goes round the givers: everyone paints a figure and doesn’t see the others. The recipient sends replies back the same way.",
     "Матрёшка — это один файл, его можно отправить в любом мессенджере. Общая матрёшка идёт по кругу: каждый расписывает свою фигурку и не видит чужих. Отклики получатель присылает так же, файлом."),
    ("ob4_t", "How to sign your figures?", "Как подписывать ваши фигурки?"),
    ("ob4", "The name goes on every figure you paint. It can be changed in settings.", "Имя будет на каждой фигурке, которую вы распишете. Его можно поменять в настройках."),
    ("ob_go", "Let’s begin", "Начать"),
    # settings
    ("name_note", "Shown on the figures you paint and on your replies.", "Видно на ваших фигурках и в ваших откликах."),
    ("theme", "Theme", "Тема"),
    ("theme_system", "As the system", "Как в системе"),
    ("theme_light", "Limewood", "Липа"),
    ("theme_dark", "Lacquer", "Лак"),
    ("language", "Language", "Язык"),
    ("lang_system", "System", "Системный"),
    ("lang_ru", "Русский", "Русский"),
    ("lang_en", "English", "English"),
    ("about_t", "Where it all lives", "Где всё хранится"),
    ("about_b", "Only on your phone and in the files you send. The app has no internet access and no accounts. Photos, voices and answers live inside the matryoshka file; the answers to riddles are never stored at all: they become the keys.",
     "Только на вашем телефоне и в файлах, которые вы отправляете. У приложения нет доступа в интернет и нет аккаунтов. Фото, голос и ответы живут внутри файла матрёшки, а ответы на загадки не хранятся вовсе: из них получаются ключи."),
    ("version", "Version %1$s", "Версия %1$s"),
    ("community", "Community", "Сообщество"),
    ("community_text", "Matryoshka is made by one person. Bugs, ideas and news go through the StillCraft bot in Telegram.",
     "«Матрёшку» делает один человек. Ошибки, идеи и новости — через бота StillCraft в Telegram."),
    ("community_bug", "Report a bug", "Сообщить об ошибке"),
    ("community_bug_sub", "Something broke or looks wrong", "Что-то сломалось или выглядит не так"),
    ("community_idea", "Suggest an idea", "Предложить идею"),
    ("community_idea_sub", "What would make it better", "Что сделало бы приложение лучше"),
    ("community_follow", "Follow updates", "Следить за обновлениями"),
    ("community_follow_sub", "A message when a new version is out", "Сообщение, когда выйдет новая версия"),
    ("community_chat", "StillCraft chat", "Чат StillCraft"),
    ("community_chat_sub", "Talk to other people who use the apps", "Поговорить с теми, кто пользуется приложениями"),
    # paintings
    ("p_khokhloma", "Khokhloma", "Хохлома"),
    ("p_gzhel", "Gzhel", "Гжель"),
    ("p_polkhov", "Polkhov-Maidan", "Полхов-Майдан"),
    ("p_semyonov", "Semyonov", "Семёнов"),
    ("p_vyatka", "Vyatka", "Вятка"),
]

P = [
    ("figures", ("%1$d figure", "%1$d figures"), ("%1$d фигурка", "%1$d фигурки", "%1$d фигурок")),
    ("locks", ("%1$d lock", "%1$d locks"), ("%1$d замок", "%1$d замка", "%1$d замков")),
    ("replies", ("%1$d reply", "%1$d replies"), ("%1$d отклик", "%1$d отклика", "%1$d откликов")),
]

A = [
    ("compass", ["north", "north-east", "east", "south-east", "south", "south-west", "west", "north-west"],
                ["на север", "на северо-восток", "на восток", "на юго-восток", "на юг", "на юго-запад", "на запад", "на северо-запад"]),
]

UNTRANSLATABLE = ("lang_ru", "lang_en")


def esc(s):
    s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    return s.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"')


def write(lang, col):
    out = ['<?xml version="1.0" encoding="utf-8"?>', "<!-- Generated by tools/res.py: edit the table there. -->", "<resources>"]
    for name, en, ru in S:
        if name in UNTRANSLATABLE:
            if col == 1:
                continue
            out.append(f'    <string name="{name}" translatable="false">{esc(en)}</string>')
            continue
        out.append(f'    <string name="{name}">{esc(en if col == 0 else ru)}</string>')
    for name, en, ru in P:
        out.append(f'    <plurals name="{name}">')
        if col == 0:
            out.append(f'        <item quantity="one">{esc(en[0])}</item>')
            out.append(f'        <item quantity="other">{esc(en[1])}</item>')
        else:
            for q, v in zip(("one", "few", "many", "other"), (ru[0], ru[1], ru[2], ru[2])):
                out.append(f'        <item quantity="{q}">{esc(v)}</item>')
        out.append("    </plurals>")
    for name, en, ru in A:
        out.append(f'    <string-array name="{name}">')
        for v in (en if col == 0 else ru):
            out.append(f"        <item>{esc(v)}</item>")
        out.append("    </string-array>")
    out.append("</resources>")
    d = RES / ("values" if lang == "en" else "values-ru")
    d.mkdir(parents=True, exist_ok=True)
    (d / "strings.xml").write_text("\n".join(out) + "\n", encoding="utf-8")


if __name__ == "__main__":
    names = [s[0] for s in S]
    assert len(names) == len(set(names)), [n for n in names if names.count(n) > 1]
    for a in A:
        assert len(a[1]) == len(a[2]), a[0]
    write("en", 0)
    write("ru", 1)
    print(len(S), "strings,", len(P), "plurals,", len(A), "arrays")
