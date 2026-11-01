package app.matryoshka

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.matryoshka.data.Settings
import app.matryoshka.ui.Root
import app.matryoshka.ui.theme.MatryoshkaTheme
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) handle(intent)
        setContent {
            val settings: Settings? by container.settings.settings.collectAsStateWithLifecycle(initialValue = null)
            val ready by container.ready.collectAsStateWithLifecycle()
            if (ready) settings?.let { s -> MatryoshkaTheme(s.theme) { Root(s) } }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handle(intent)
    }

    /** A .matr opened from a messenger or a file manager, or a notification pointing at a matryoshka. */
    private fun handle(i: Intent?) {
        i ?: return
        i.getStringExtra(OPEN)?.let { id ->
            container.incoming.value = Incoming.Doll(id)
            return
        }
        val uri: Uri? = when (i.action) {
            Intent.ACTION_VIEW -> i.data
            Intent.ACTION_SEND -> IntentCompat.getParcelableExtra(i, Intent.EXTRA_STREAM, Uri::class.java)
            else -> null
        }
        if (uri != null) lifecycleScope.launch { container.incoming.value = container.import(uri) }
    }

    companion object {
        const val OPEN = "open"
    }
}
