import os

base = r"c:\Users\Lenovo\AndroidStudioProjects\TaskFlow\app\src\main\res"

print("=== Checking icon state ===")

# Check mipmap directories
has_png = False
has_webp = False
has_xml = False

for root, dirs, files in os.walk(base):
    for f in files:
        if "ic_launcher" in f:
            full = os.path.join(root, f)
            if f.endswith(".png"):
                has_png = True
                print(f"  PNG: {full} ({os.path.getsize(full)} bytes)")
            elif f.endswith(".webp"):
                has_webp = True
                print(f"  WEBP: {full} ({os.path.getsize(full)} bytes)")
            elif f.endswith(".xml"):
                has_xml = True
                with open(full) as fp:
                    print(f"  XML: {full} -> {fp.read().strip()}")

print()
if has_png:
    print("STATUS: PNGs existem e a build compilou sem erros.")
    print("--> O icon DEVERIA aparecer no emulador.")
    print()
    print("Se nao aparecer, o problema e' a CACHE do emulador.")
    print("Solucao: Settings -> Apps -> TaskFlow -> Storage -> Clear Data")
    print("         OU desinstalar e reinstalar a app no emulador.")
elif has_webp or has_xml:
    print("STATUS: Ainda ha' ficheiros antigos (webp/xml).")
    print("--> O icon pode nao aparecer.")
else:
    print("STATUS: NENHUM icon encontrado! Algo correu mal.")