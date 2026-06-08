import os
from PIL import Image

src = r"c:\Users\Lenovo\AndroidStudioProjects\TaskFlow\app\src\main\ic_launcher-playstore.png"
outdir = r"c:\Users\Lenovo\AndroidStudioProjects\TaskFlow\app\src\main\res"

img = Image.open(src).convert("RGBA")

# Android icon sizes:
sizes = {
    "mipmap-mdpi":    48,
    "mipmap-hdpi":    72,
    "mipmap-xhdpi":   96,
    "mipmap-xxhdpi":  144,
    "mipmap-xxxhdpi": 192,
}

background = Image.new("RGBA", img.size, (21, 101, 192, 255))  # blue #1565C0
composite = Image.alpha_composite(background, img)

for folder, size in sizes.items():
    path = os.path.join(outdir, folder)
    resized = composite.resize((size, size), Image.LANCZOS)
    # Save as PNG (Android can use PNG in mipmap)
    out = os.path.join(path, "ic_launcher.png")
    resized.save(out, "PNG")
    print(f"Saved {out} ({size}x{size})")
    # Also save round
    out_round = os.path.join(path, "ic_launcher_round.png")
    resized.save(out_round, "PNG")
    print(f"Saved {out_round} ({size}x{size})")

print("Done!")