package app.matryoshka.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import app.matryoshka.MainActivity
import app.matryoshka.R
import app.matryoshka.domain.LockKind
import app.matryoshka.domain.Meta
import java.util.concurrent.TimeUnit

/** When the next figure is locked until a moment, a quiet note at that moment says it can be opened. */
object Nudge {
    private const val CHANNEL = "ready"

    fun plan(ctx: Context, meta: Meta, next: Int) {
        val wm = runCatching { WorkManager.getInstance(ctx) }.getOrNull() ?: return
        val name = "nudge-${meta.id}"
        val f = meta.figures.getOrNull(next)
        val wait = (f?.lock?.at ?: 0) - System.currentTimeMillis()
        if (f == null || f.lock.kind != LockKind.TIME || wait <= 0) {
            wm.cancelUniqueWork(name)
            return
        }
        val req = OneTimeWorkRequestBuilder<NudgeWorker>()
            .setInitialDelay(wait, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("id" to meta.id, "title" to meta.title.ifBlank { meta.to }))
            .build()
        wm.enqueueUniqueWork(name, ExistingWorkPolicy.REPLACE, req)
    }

    fun post(ctx: Context, id: String, title: String) {
        if (ctx.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val nm = ctx.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel(CHANNEL, ctx.getString(R.string.n_channel), NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(ctx, id.hashCode(),
            Intent(ctx, MainActivity::class.java).putExtra(MainActivity.OPEN, id).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val n = NotificationCompat.Builder(ctx, CHANNEL)
            .setSmallIcon(R.drawable.ic_notify)
            .setContentTitle(ctx.getString(R.string.n_title, title))
            .setContentText(ctx.getString(R.string.n_body))
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(ctx).notify(id.hashCode(), n) }
    }
}

class NudgeWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        Nudge.post(applicationContext, inputData.getString("id") ?: return Result.success(), inputData.getString("title").orEmpty())
        return Result.success()
    }
}
