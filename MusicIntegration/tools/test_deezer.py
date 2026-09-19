import urllib.request, urllib.parse, json

artist = "ЗимойБезШапки"
title = "Лёха"

q = urllib.parse.quote(f"{artist} {title}")
url = f"https://api.deezer.com/search?q={q}&limit=1"
req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
with urllib.request.urlopen(req, timeout=5) as resp:
    data = json.loads(resp.read())
items = data.get("data", [])
if items:
    cover = items[0].get("album", {}).get("cover_xl") or items[0].get("album", {}).get("cover_big", "")
    print(f"Found: {items[0]['title']} - {items[0]['artist']['name']}")
    print(f"Cover: {cover}")
else:
    print("Not found")
