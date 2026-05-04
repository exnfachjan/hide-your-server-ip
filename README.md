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

| Minecraft | Fabric | NeoForge | Notes |
|---|---|---|---|
| 1.21.1 – 1.21.3 | ✅ | ✅ | Java 21 |
| 1.21.4 | ✅ | ✅ | Java 21 |
| 1.21.5 – 1.21.11 | ✅ | ✅ | Java 21 |
| 26.1+ | ✅ | ✅ | Java 25, unobfuscated |

Grab the latest release from the [Releases](https://github.com/exnfachjan/hide-your-server-ip/releases) page.

> **Client-side only** — install only on your own game client, not on the server.

---

## Build structure

The project uses a multi-subproject Gradle layout.  Each row in the table above corresponds to one Fabric JAR and one NeoForge JAR:

```
fabric-1.21.1/      → fabric-mc1.21.1-1.21.3
neoforge-1.21.1/    → neoforge-mc1.21.1-1.21.3
fabric-1.21.4/      → fabric-mc1.21.4
neoforge-1.21.4/    → neoforge-mc1.21.4
fabric-1.21.5/      → fabric-mc1.21.5-1.21.11
neoforge-1.21.5/    → neoforge-mc1.21.5-1.21.11
fabric-26.1/        → fabric-mc26.1  (Java 25)
neoforge-26.1/      → neoforge-mc26.1 (Java 25)
```

Shared source lives in `common/` and version-specific mixins in `versions/<mc>/`.

---

## License

Copyright (c) 2025 exnfachjan. All rights reserved.

---

*Made with ❤️ by exnfachjan*
