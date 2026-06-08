import os, glob

base = r"c:\Users\Lenovo\AndroidStudioProjects\TaskFlow\app\src\main\res"
patterns = ["ic_launcher.webp", "ic_launcher_round.webp", "ic_launcher_background.webp"]

for root, dirs, files in os.walk(base):
    for f in files:
        if f in patterns:
            path = os.path.join(root, f)
            os.remove(path)
            print(f"Deleted {path}")

print("Done cleaning old icons!")