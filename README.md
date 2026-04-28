# Hide Your Server IP (HYSI)

![Hide Your Server IP](common/src/main/resources/assets/hysi/icon.png)

> **Built for streamers, content creators and server owners who want to keep their server IP private.**

HYSI masks server IP addresses with bullet points (`•••••••••`) in every Minecraft multiplayer screen. A single click reveals or hides the address — just like a password field in a browser.

---

## Why?

Accidentally leaking a server IP during a live stream can expose the server to DDoS attacks or unwanted visitors. With HYSI installed the IP is hidden by default. You reveal it intentionally, never by accident.

---

## How it works

| Screen | Behaviour |
|---|---|
| **Direct Connect** | IP field starts masked — click **[*]** to reveal |
| **Add Server** | IP field starts masked — click **[*]** to reveal |
| **Edit Server** | IP field starts masked — click **[*]** to reveal |

- **[*]** (red) → IP is hidden
- **[O]** (green) → IP is visible

---

## Download

| Minecraft | Fabric | NeoForge |
|---|---|---|
| 1.21.1 – 1.21.3 | ✅ | ✅ |
| 1.21.4 | ✅ | ✅ |

Grab the latest release from the [Releases](https://github.com/exnfachjan/hide-your-server-ip/releases) page.

> **Client-side only** — install only on your own game client, not on the server.

---

## Building from source

```bash
./gradlew build          # Linux / macOS
.\gradlew.bat build      # Windows
```

Output JARs — the filename shows the exact compatible MC version range:

```
fabric-1.21.1/build/libs/   hide-your-server-ip-fabric-mc1.21.1-1.21.3-1.0.0.jar
fabric-1.21.4/build/libs/   hide-your-server-ip-fabric-mc1.21.4-1.0.0.jar
neoforge-1.21.1/build/libs/ hide-your-server-ip-neoforge-mc1.21.1-1.21.3-1.0.0.jar
neoforge-1.21.4/build/libs/ hide-your-server-ip-neoforge-mc1.21.4-1.0.0.jar
```

Build only a specific version:
```bash
.\gradlew.bat :fabric-1.21.4:build
.\gradlew.bat :neoforge-1.21.1:build
```

---

## Project structure

```
hide-your-server-ip/
├── common/                        # Shared code & resources
│   ├── HYSIMod.java
│   ├── hysi.mixins.json
│   └── assets/hysi/icon.png
│
├── versions/
│   ├── 1.21.1/                    # Mixins for MC 1.21.1–1.21.3
│   └── 1.21.4/                    # Mixins for MC 1.21.4
│
├── fabric-1.21.1/                 # Fabric — MC 1.21.1–1.21.3
├── fabric-1.21.4/                 # Fabric — MC 1.21.4
├── neoforge-1.21.1/               # NeoForge — MC 1.21.1–1.21.3
└── neoforge-1.21.4/               # NeoForge — MC 1.21.4
```

---

## License

[MIT](LICENSE) — free to use, modify and distribute.

---

*Made with ❤️ by exnfachjan*
