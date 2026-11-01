package app.matryoshka

import app.matryoshka.domain.Content
import app.matryoshka.domain.Draft
import app.matryoshka.domain.Geo
import app.matryoshka.domain.Layer
import app.matryoshka.domain.Lock
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Matr
import app.matryoshka.domain.Paint
import app.matryoshka.domain.Parsed
import app.matryoshka.domain.Relay
import app.matryoshka.domain.Reply
import app.matryoshka.domain.ReplyNote
import app.matryoshka.domain.norm
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LogicTest {
    private val photo = ByteArray(5000) { (it * 7).toByte() }

    private fun draft() = Draft(
        id = "t1", title = "На 30-летие", to = "Ане",
        layers = listOf(
            Layer(Paint.KHOKHLOMA, "Марат", Lock(LockKind.TIME, at = 123), content = Content("первая")),
            Layer(Paint.GZHEL, "Лена", Lock(LockKind.RIDDLE, question = "Кот?"), answer = "Пломбир", content = Content("вторая", photo = photo)),
            Layer(Paint.POLKHOV, "Бабушка", Lock(LockKind.WORD, who = "бабушка"), answer = "калачи", content = Content("третья", voice = byteArrayOf(1, 2, 3), voiceMs = 900)),
        ),
        gift = Content("в шкафу", link = "https://example.com/t"),
    )

    @Test fun answersIgnoreCaseYoAndPunctuation() {
        assertEquals(norm("Пломбир"), norm(" пломбир! "))
        assertEquals(norm("Ёлка"), norm("елка"))
        assertFalse(norm("кот") == norm("кит"))
    }

    @Test fun sealedOpensFigureByFigureWithAnswers() {
        val s = (Matr.read(Matr.seal(draft())) as Parsed.Doll).sealed
        assertEquals(4, s.count)
        assertEquals(listOf(LockKind.TIME, LockKind.RIDDLE, LockKind.WORD, LockKind.NONE), s.meta.figures.map { it.lock.kind })
        assertEquals("Кот?", s.meta.figures[1].lock.question)
        assertEquals(listOf("Марат", "Лена", "Бабушка"), s.meta.authors)

        val o0 = s.open(0, s.key(0, s.meta.seed, null))!!
        assertEquals("первая", o0.content.note)
        // A wrong answer gives a key that GCM rejects.
        assertNull(s.open(1, s.key(1, o0.next!!, "сметана")))
        val o1 = s.open(1, s.key(1, o0.next!!, "ПЛОМБИР!"))!!
        assertEquals("вторая", o1.content.note)
        assertArrayEquals(photo, o1.content.photo)
        val o2 = s.open(2, s.key(2, o1.next!!, "Калачи"))!!
        assertEquals(900, o2.content.voiceMs)
        assertArrayEquals(byteArrayOf(1, 2, 3), o2.content.voice)
        val g = s.open(3, s.key(3, o2.next!!, null))!!
        assertEquals("в шкафу", g.content.note)
        assertEquals("https://example.com/t", g.content.link)
        assertNull(g.next)
    }

    @Test fun answersAndContentAreNotInTheFileInTheClear() {
        val bytes = Matr.seal(draft())
        val text = String(bytes, Charsets.ISO_8859_1) + String(bytes, Charsets.UTF_8)
        for (secret in listOf("Пломбир", "пломбир", "калачи", "вторая", "в шкафу")) assertFalse(secret, text.contains(secret))
    }

    @Test fun innerFigureCannotBeOpenedWithoutTheOuterOnes() {
        val s = (Matr.read(Matr.seal(draft())) as Parsed.Doll).sealed
        // The outer seed doesn't open the second figure, even with the right answer.
        assertNull(s.open(1, s.key(1, s.meta.seed, "пломбир")))
    }

    @Test fun relayKeepsEverythingForTheSeal() {
        val r = Relay("r1", "Папе", "Папе", listOf("Мама", "Дима", "Аня"), 1, draft().layers.take(2), Paint.VYATKA, Content("подарок"), 5)
        val back = (Matr.read(Matr.relay(r)) as Parsed.Going).relay
        assertEquals(r.people, back.people)
        assertEquals(1, back.turn)
        assertEquals("Пломбир", back.layers[1].answer)
        assertArrayEquals(photo, back.layers[1].content.photo)
        assertEquals("подарок", back.gift.note)
        assertEquals("Аня", back.next)
        assertFalse(back.last)
        assertTrue(back.copy(turn = 2).last)
        val s = (Matr.read(Matr.seal(back.draft())) as Parsed.Doll).sealed
        assertEquals(3, s.count)
    }

    @Test fun replyRoundTrip() {
        val r = Reply("t1", "Аня", 77, listOf(ReplyNote(0, "спасибо"), ReplyNote(2, "", byteArrayOf(9, 9), 1200)))
        val back = (Matr.read(Matr.reply(r)) as Parsed.Back).reply
        assertEquals("Аня", back.from)
        assertEquals("спасибо", back.notes[0].text)
        assertArrayEquals(byteArrayOf(9, 9), back.notes[1].voice)
        assertEquals(1200, back.notes[1].voiceMs)
    }

    @Test fun garbageIsNotAMatryoshka() {
        assertNull(Matr.read(byteArrayOf(1, 2, 3)))
        assertNull(Matr.read(ByteArray(0)))
    }

    @Test fun coordinatesFromWhateverIsPasted() {
        fun near(p: Pair<Double, Double>?, lat: Double, lon: Double) {
            assertNotNull(p)
            assertEquals(lat, p!!.first, 1e-6)
            assertEquals(lon, p.second, 1e-6)
        }
        near(Geo.parse("55.7558, 37.6173"), 55.7558, 37.6173)
        near(Geo.parse("https://yandex.ru/maps/?ll=37.644700%2C55.759400&z=17"), 55.7594, 37.6447)
        near(Geo.parse("https://www.google.com/maps/place/X/@55.7594,37.6447,17z"), 55.7594, 37.6447)
        near(Geo.parse("geo:55.75,37.61"), 55.75, 37.61)
        assertNull(Geo.parse("кофейня на Покровке"))
        assertNull(Geo.parse("200, 500"))
    }

    @Test fun distanceAndBearing() {
        assertEquals(111_195.0, Geo.distance(0.0, 0.0, 1.0, 0.0), 50.0)
        assertEquals(90.0, Geo.bearing(0.0, 0.0, 0.0, 1.0), .5)
    }
}
