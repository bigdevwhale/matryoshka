package app.matryoshka.data

import android.content.Context
import app.matryoshka.AppContainer
import app.matryoshka.art.Fake
import app.matryoshka.domain.Content
import app.matryoshka.domain.Gesture
import app.matryoshka.domain.Layer
import app.matryoshka.domain.Lock
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Matr
import app.matryoshka.domain.Paint
import app.matryoshka.domain.Parsed
import app.matryoshka.domain.Relay
import app.matryoshka.domain.Reply
import app.matryoshka.domain.ReplyNote
import app.matryoshka.ru
import java.io.File

/** The demo shelf: a few matryoshkas given and received, a draft and a turn in a shared one. No real photos. */
object DemoSeed {
    private const val DAY = 86_400_000L

    suspend fun seedIfEmpty(ctx: Context, c: AppContainer) {
        val mark = File(ctx.filesDir, "demo-seeded")
        if (mark.exists()) return
        val ru = ctx.ru
        fun t(r: String, e: String) = if (ru) r else e
        val now = System.currentTimeMillis()
        c.settings.update { it.copy(onboarded = true, name = t("Аня", "Anna")) }
        val store = c.store

        // 1. The big one: Anna's thirtieth, from five people, nothing opened yet.
        val big = Relay(
            id = "demo30th0000001", title = t("На 30-летие", "For your 30th"), to = t("Ане", "Anna"),
            people = listOf(t("Марат", "Mark")), turn = 0, created = now - 2 * DAY,
            layers = listOf(
                Layer(Paint.KHOKHLOMA, t("Марат", "Mark"), Lock(LockKind.TIME, at = now - 60_000),
                    content = Content(t("С днём рождения, Аня! Внутри ещё пять фигурок. Не торопись: каждая откроется в своё время и в своём месте.",
                        "Happy birthday, Anna! There are five more figures inside. Take your time: each opens at its own moment and in its own place."))),
                Layer(Paint.GZHEL, t("Лена", "Lena"), Lock(LockKind.RIDDLE, question = t("Как звали кота, который в 2019-м съел твой торт?", "What was the name of the cat who ate your cake in 2019?"),
                    hint = t("Белый и холодный, как и он сам.", "White and cold, just like him.")), answer = t("Пломбир", "Snowball"),
                    content = Content(t("Тот самый торт. Тот самый кот. Тот самый 2019-й.", "That cake. That cat. That 2019."), photo = Fake.cake())),
                Layer(Paint.POLKHOV, t("Дима", "Dan"), Lock(LockKind.PLACE, lat = 55.7594, lon = 37.6447, place = t("кофейня «Зерно» на Покровке", "the Grain café on Pokrovka"), radius = 100),
                    content = Content(t("Здесь мы пили первый кофе в 2016-м. Закажи раф: он уже оплачен, скажи бариста «от Димы».",
                        "This is where we had our first coffee in 2016. Order a latte: it's paid for, just tell the barista it's from Dan."), photo = Fake.coffee())),
                Layer(Paint.SEMYONOV, t("Бабушка", "Gran"), Lock(LockKind.WORD, who = t("бабушка", "Gran"), hint = t("Что мы пекли по воскресеньям?", "What did we bake on Sundays?")),
                    answer = t("калачи", "scones"),
                    content = Content(t("Анечка, тридцать — это самое начало.", "Annie, thirty is only the beginning."), voice = Fake.chime(), voiceMs = 3200)),
                Layer(Paint.VYATKA, t("Мама", "Mum"), Lock(LockKind.GESTURE, gesture = Gesture.COVER),
                    content = Content(t("Помнишь, в пять лет ты прятала конфеты в нашу матрёшку? Я всё знала. Люблю тебя.",
                        "Remember hiding sweets in our matryoshka when you were five? I knew all along. Love you."))),
            ),
            giftPaint = Paint.KHOKHLOMA,
            gift = Content(t("Это от всех нас: два билета на «Щелкунчика», 29 декабря, 19:00. А ещё загляни в шкаф: верхняя полка, за свитерами.",
                "This is from all of us: two tickets to The Nutcracker, 29 December, 7 pm. And look in the wardrobe: top shelf, behind the jumpers.")),
        )
        // 2. A week away, all opened.
        val week = Relay(
            id = "demoweek0000002", title = t("На неделю командировки", "For the week away"), to = t("Ане", "Anna"),
            people = listOf(t("Лена", "Lena")), turn = 0, created = now - 40 * DAY,
            layers = listOf(
                Layer(Paint.GZHEL, t("Лена", "Lena"), content = Content(t("Понедельник. Не забудь поесть.", "Monday. Don't forget to eat."))),
                Layer(Paint.VYATKA, t("Лена", "Lena"), Lock(LockKind.TIME, at = now - 36 * DAY), content = Content(t("Среда. Половина позади.", "Wednesday. Halfway there."))),
                Layer(Paint.SEMYONOV, t("Лена", "Lena"), Lock(LockKind.TIME, at = now - 34 * DAY), content = Content(t("Пятница. Встречаю на вокзале.", "Friday. I'll meet you at the station."))),
            ),
            giftPaint = Paint.POLKHOV, gift = Content(t("Пирог уже в духовке.", "The pie's already in the oven.")),
        )
        // 3. Just because: the next figure opens in three days.
        val soon = Relay(
            id = "demosoon0000003", title = t("Просто так", "Just because"), to = t("Ане", "Anna"),
            people = listOf(t("Дима", "Dan")), turn = 0, created = now - DAY,
            layers = listOf(
                Layer(Paint.SEMYONOV, t("Дима", "Dan"), content = Content(t("Привет! Следующая — в пятницу вечером.", "Hi! The next one on Friday evening."))),
                Layer(Paint.POLKHOV, t("Дима", "Dan"), Lock(LockKind.TIME, at = now + 3 * DAY + 5 * 3_600_000L), content = Content(t("Пятница!", "Friday!"))),
            ),
            giftPaint = Paint.GZHEL, gift = Content(t("Кино в субботу, я плачу.", "Cinema on Saturday, my treat.")),
        )
        for ((r, open) in listOf(big to 0, week to 4, soon to 1)) {
            val s = (Matr.read(Matr.seal(r.draft())) as Parsed.Doll).sealed
            store.putGot(s)
            var seed = s.meta.seed
            val keys = ArrayList<ByteArray>()
            for (i in 0 until open) {
                val k = s.key(i, seed, r.layers.getOrNull(i)?.answer)
                val o = s.open(i, k) ?: break
                keys += k
                seed = o.next ?: break
            }
            store.saveProgress(s.meta.id, Progress(keys, added = r.created, notes = if (open == 4) mapOf(0 to t("Поела, честно!", "I ate, honestly!")) else emptyMap()))
        }

        // Given: a wedding with replies, a sealed one for Mum, a figure in Gran's shared one.
        store.putGiven(Given("demowed00000004", t("На свадьбу", "For the wedding"), t("Диме и Оле", "Dan and Olya"), now - 20 * DAY, listOf(t("Аня", "Anna")),
            listOf(Paint.POLKHOV, Paint.GZHEL, Paint.SEMYONOV, Paint.VYATKA, Paint.KHOKHLOMA),
            listOf(LockKind.TIME, LockKind.RIDDLE, LockKind.PLACE, LockKind.GESTURE, LockKind.NONE), sealer = true), null)
        val reply = Reply("demowed00000004", t("Дима и Оля", "Dan and Olya"), now - 18 * DAY, listOf(
            ReplyNote(1, t("Мы угадали с первого раза! Оля сказала, что это нечестно лёгкая загадка.", "Got it first time! Olya says the riddle was unfairly easy.")),
            ReplyNote(2, t("Стояли у загса и смеялись: телефон открыл фигурку прямо на ступеньках.", "We stood at the registry office laughing: the phone opened it right on the steps.")),
            ReplyNote(4, t("Спасибо. Это лучший подарок за весь день.", "Thank you. Best present of the whole day."), Fake.chime(2.2f), 2200),
        ))
        store.putReply(reply, Matr.reply(reply))
        store.putGiven(Given("demomum00000005", t("На 8 марта", "For Mother's Day"), t("Маме", "Mum"), now - 60 * DAY, listOf(t("Аня", "Anna")),
            listOf(Paint.VYATKA, Paint.SEMYONOV, Paint.GZHEL), listOf(LockKind.TIME, LockKind.WORD, LockKind.NONE), sealer = true), null)
        store.putGiven(Given("demogran0000006", t("Бабушке · 80 лет", "Gran · 80"), t("Бабушке", "Gran"), now - 5 * DAY,
            listOf(t("Мама", "Mum"), t("Аня", "Anna"), t("Дима", "Dan")),
            listOf(Paint.KHOKHLOMA, Paint.SEMYONOV, Paint.POLKHOV, Paint.GZHEL), listOf(LockKind.TIME, LockKind.RIDDLE, LockKind.GESTURE, LockKind.NONE),
            sealer = false, mine = 1), null)

        // Drafts: my own for Lena's housewarming, and my turn in Dad's.
        store.putDraft(Relay(
            id = "demohome0000007", title = t("На новоселье", "Housewarming"), to = t("Лене", "Lena"), people = listOf(t("Аня", "Anna")), turn = 0, created = now - 3_600_000L,
            layers = listOf(
                Layer(Paint.GZHEL, t("Аня", "Anna"), Lock(LockKind.TIME, at = now + 6 * DAY), content = Content(t("С новосельем! Ключи от подарка — внутри.", "Happy housewarming! The keys to the present are inside."))),
                Layer(Paint.POLKHOV, t("Аня", "Anna"), Lock(LockKind.RIDDLE, question = t("Сколько коробок мы таскали на пятый этаж?", "How many boxes did we carry up to the fifth floor?")), answer = "42",
                    content = Content(t("Сорок две. Я считала.", "Forty-two. I counted."))),
                Layer(Paint.VYATKA, t("Аня", "Anna")),
            ),
            giftPaint = Paint.SEMYONOV, gift = Content(t("Под ковриком у двери.", "Under the doormat.")),
        ))
        store.putDraft(Relay(
            id = "demodad00000008", title = t("Папе · юбилей", "Dad · 60"), to = t("Папе", "Dad"),
            people = listOf(t("Мама", "Mum"), t("Дима", "Dan"), t("Аня", "Anna"), t("Бабушка", "Gran")), turn = 2, created = now - 2 * DAY,
            layers = listOf(
                Layer(Paint.KHOKHLOMA, t("Мама", "Mum"), Lock(LockKind.TIME, at = now + 9 * DAY), content = Content("…")),
                Layer(Paint.POLKHOV, t("Дима", "Dan"), Lock(LockKind.PLACE, lat = 55.75, lon = 37.61, place = "…"), content = Content("…")),
                Layer(Paint.SEMYONOV, t("Аня", "Anna"), Lock(LockKind.RIDDLE, question = t("Как звали нашу первую собаку?", "What was our first dog called?")), answer = t("Тузик", "Rex"),
                    content = Content(t("Пап, помнишь, как Тузик утащил твой ботинок на дачу соседей?", "Dad, remember Rex dragging your boot to the neighbours' dacha?"))),
            ),
            giftPaint = Paint.GZHEL, gift = Content("…"),
        ))
        mark.writeText("1")
    }
}
