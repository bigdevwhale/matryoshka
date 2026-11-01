"""Writes the launcher icon, the notification icon and the small XML resources.

python tools/icon.py

The icon is the same matryoshka the app paints (Khokhloma: cinnabar dress, black scarf, gold flower),
as vector drawables, on black lacquer.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"

BODY = "M100,8 C68,8 52,34 54,66 C55,88 64,98 60,110 C36,134 22,174 22,218 C22,268 54,292 100,292 C146,292 178,268 178,218 C178,174 164,134 140,110 C136,98 145,88 146,66 C148,34 132,8 100,8 Z"
SCARF = "M100,8 C68,8 52,34 54,66 C55,88 64,98 60,110 C80,130 120,130 140,110 C136,98 145,88 146,66 C148,34 132,8 100,8 Z"


def oval(cx, cy, rx, ry):
    return f"M{cx - rx},{cy}a{rx},{ry} 0,1 0,{2 * rx},0a{rx},{ry} 0,1 0,{-2 * rx},0"


def path(fill, d, extra=""):
    return f'        <path android:fillColor="{fill}" {extra}android:pathData="{d}" />'


def doll_paths():
    dots = " ".join(oval(x, y, 3, 3) for x, y in [(70, 36), (130, 36), (100, 20), (61, 92), (139, 92), (78, 116), (122, 116)])
    return "\n".join([
        path("#B3262E", BODY),
        path("#1F1512", oval(100, 212, 52, 64), 'android:strokeColor="#E8B24A" android:strokeWidth="4" '),
        path("#E8B24A", " ".join(oval(100 + dx, 186 + dy, 9, 6) for dx, dy in [(0, -12), (0, 12), (-12, 0), (12, 0)])),
        path("#C8323C", oval(100, 186, 7, 7)),
        path("#E8B24A", oval(72, 236, 10, 5) + " " + oval(128, 236, 10, 5) + " " + oval(84, 256, 10, 5) + " " + oval(116, 256, 10, 5)),
        path("#C8323C", oval(100, 252, 6, 6) + " " + oval(76, 222, 5, 5) + " " + oval(124, 222, 5, 5)),
        path("#1F1512", SCARF),
        path("#1F1512", "M86,122 L100,146 L114,122 Z"),
        path("#E8B24A", dots),
        path("#F7DCC1", oval(100, 70, 30, 32)),
        path("#5A3018", "M70,64 C70,42 130,42 130,64 C124,52 108,48 100,57 C92,48 76,52 70,64Z"),
        path("#2B1A12", oval(89, 75, 3, 3.8) + " " + oval(111, 75, 3, 3.8)),
        path("#E5636A", oval(82, 87, 7, 7) + " " + oval(118, 87, 7, 7), 'android:fillAlpha="0.55" '),
        path("#C8323C", "M94.5,93 Q97.5,89.5 100,92 Q102.5,89.5 105.5,93 Q100,99 94.5,93Z"),
    ])


def vector(w, vw, body):
    return (f'<?xml version="1.0" encoding="utf-8"?>\n'
            f'<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            f'    android:width="{w}dp" android:height="{w}dp" android:viewportWidth="{vw}" android:viewportHeight="{vw}">\n'
            f'{body}\n</vector>\n')


def main():
    (RES / "drawable").mkdir(parents=True, exist_ok=True)
    (RES / "mipmap-anydpi-v26").mkdir(parents=True, exist_ok=True)
    (RES / "xml").mkdir(parents=True, exist_ok=True)
    (RES / "values").mkdir(parents=True, exist_ok=True)
    k = 0.21
    fg = f'    <group android:translateX="{54 - 100 * k:.2f}" android:translateY="{54 - 150 * k:.2f}" android:scaleX="{k}" android:scaleY="{k}">\n{doll_paths()}\n    </group>'
    (RES / "drawable/ic_launcher_foreground.xml").write_text(vector(108, 108, fg), encoding="utf-8")
    bg = "\n".join([
        path("#241510", "M0,0h108v108h-108z"),
        path("#3A2016", oval(54, 40, 50, 46)),
    ])
    (RES / "drawable/ic_launcher_background.xml").write_text(vector(108, 108, bg), encoding="utf-8")
    mono = f'    <group android:translateX="{54 - 100 * k:.2f}" android:translateY="{54 - 150 * k:.2f}" android:scaleX="{k}" android:scaleY="{k}">\n{path("#FFFFFFFF", BODY)}\n    </group>'
    (RES / "drawable/ic_launcher_monochrome.xml").write_text(vector(108, 108, mono), encoding="utf-8")
    n = 0.077
    notify = f'    <group android:translateX="{12 - 100 * n:.2f}" android:translateY="{12 - 150 * n:.2f}" android:scaleX="{n}" android:scaleY="{n}">\n{path("#FFFFFFFF", BODY)}\n    </group>'
    (RES / "drawable/ic_notify.xml").write_text(vector(24, 24, notify), encoding="utf-8")
    (RES / "mipmap-anydpi-v26/ic_launcher.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@drawable/ic_launcher_background" />\n'
        '    <foreground android:drawable="@drawable/ic_launcher_foreground" />\n'
        '    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />\n'
        '</adaptive-icon>\n', encoding="utf-8")
    (RES / "xml/files.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<paths>\n    <cache-path name="share" path="share/" />\n</paths>\n', encoding="utf-8")
    (RES / "values/colors.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <color name="window">#1D110C</color>\n    <color name="gold">#E8B24A</color>\n</resources>\n',
        encoding="utf-8")
    (RES / "values/themes.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n<resources>\n'
        '    <style name="Theme.Matryoshka" parent="Theme.AppCompat.NoActionBar">\n'
        '        <item name="android:windowBackground">@color/window</item>\n'
        '        <item name="android:statusBarColor">@android:color/transparent</item>\n'
        '        <item name="android:navigationBarColor">@android:color/transparent</item>\n'
        '        <item name="colorPrimary">@color/gold</item>\n'
        '        <item name="colorAccent">@color/gold</item>\n'
        '    </style>\n</resources>\n', encoding="utf-8")


if __name__ == "__main__":
    main()
