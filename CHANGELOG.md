# Changelog

<!--
Telegram: end each version's section with a hidden block for the StillCraft chat. Start it with a
line "<!-- tg", then 2-4 short lines in Russian for users (what is new, what is fixed), no markdown,
then close it like any HTML comment. The release notes carry it, and the chat bot posts those lines.
-->

## [1.0.1] - 2026-11-16

### Fixed
- A sudden power-off (a flat battery, a frozen phone) could cut off a file being written: the figures already opened in a matryoshka, its notes and voices, a given one, a reply or a draft could be lost. Every file is now written aside, forced to the disk and only then put in place, so the old version or the new one is always whole.

<!-- tg
Поправили сохранение: если телефон внезапно выключится (сел аккумулятор, завис), открытые фигурки, заметки и черновики больше не пропадут.
Всё записывается на диск надёжно и целиком.
-->

## [1.0.0] - 2026-11-01

### Added
- Matryoshkas of up to nine figures: notes, photos and voices inside, the present in the smallest, solid one.
- Locks: time (with a notification), riddle and someone's word (real encryption), place (within 50–300 m), gesture (cover, shake, turn over).
- The `.matr` file: a chain of keys, AES-GCM, PBKDF2 for answers; sent through any messenger.
- Shared matryoshkas passed round in a set order, everyone painting one figure; the last one seals it.
- Replies from the recipient (words and voice) sent back as a file.
- Shelf of given and received matryoshkas, workshop with drafts, onboarding, limewood and lacquer themes, English and Russian.

<!-- tg
Подарок, который открывают слой за слоем: сам подарок лежит в самой маленькой фигурке
У каждой фигурки свой замок: время, загадка, место, кодовое слово у бабушки или жест
Можно собрать матрёшку вместе: файл идёт по кругу, и каждый расписывает свою фигурку
Без сервера и без интернета: матрёшка отправляется одним файлом в любом мессенджере
-->
