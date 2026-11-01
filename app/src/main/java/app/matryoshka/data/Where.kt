package app.matryoshka.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/** Where the phone is, from the system's own location (no Google services, nothing sent anywhere). */
object Where {
    fun allowed(ctx: Context) =
        ctx.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ctx.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    private fun providers(lm: LocationManager) = listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER)
        .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }

    /** One fresh fix, or the freshest known one if none comes within 20 s. */
    @Suppress("MissingPermission")
    suspend fun now(ctx: Context): Location? {
        if (!allowed(ctx)) return null
        val lm = ctx.getSystemService(LocationManager::class.java) ?: return null
        val ps = providers(lm)
        // A fix from the last two minutes is good enough and saves waiting for a satellite indoors.
        ps.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .filter { System.currentTimeMillis() - it.time < 120_000 }.maxByOrNull { it.time }?.let { return it }
        val fresh = withTimeoutOrNull(20_000) {
            suspendCancellableCoroutine { cont ->
                val sig = android.os.CancellationSignal()
                cont.invokeOnCancellation { sig.cancel() }
                val p = ps.firstOrNull() ?: run { cont.resume(null); return@suspendCancellableCoroutine }
                lm.getCurrentLocation(p, sig, ctx.mainExecutor) { if (cont.isActive) cont.resume(it) }
            }
        }
        return fresh ?: ps.mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }
    }

    /** Follows the phone while a place lock is on screen. Returns a function that stops it. */
    @Suppress("MissingPermission")
    fun follow(ctx: Context, onFix: (Location) -> Unit): () -> Unit {
        if (!allowed(ctx)) return {}
        val lm = ctx.getSystemService(LocationManager::class.java) ?: return {}
        val l = LocationListener { onFix(it) }
        for (p in providers(lm)) runCatching { lm.requestLocationUpdates(p, 3_000L, 5f, l, Looper.getMainLooper()) }
        providers(lm).mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }.maxByOrNull { it.time }?.let(onFix)
        return { lm.removeUpdates(l) }
    }
}
