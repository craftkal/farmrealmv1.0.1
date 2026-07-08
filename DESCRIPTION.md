# Farmrealm v1.0.0

## English

Farmrealm adds a custom **Farm** dimension to Minecraft (Fabric) designed for mob farming and grinding. Natural mob spawning is completely disabled — every mob appears exclusively through configurable command-driven spawn areas.

### Key Features

- **Farm Dimension** (`/farm`): A void dimension with a solid black sky (no sun/moon/cycle), full-bright lighting on the platform, and a 5×5 stone platform at Y=2. Players teleport to (0, 3, 0) upon joining.
- **Command Spawning**: Define cuboid spawn areas with `/farm spawn set <name> <pos1> <pos2> <mob> [rate]`. Each area supports one mob type with a configurable rate of 1–500 per minute. Spawn areas persist through world restarts.
- **Item Rain**: Automatic rain showers drop items near players in the Farm dimension. Item pools include common resources (seeds, crops, cobblestone, bone), uncommon materials (iron, gold, redstone, lapis, XP bottles), and rare loot (diamond, emerald, golden apple, ancient debris, netherite scrap).
- **Growth Acceleration**: Set a global growth multiplier via `/farm set growth <0-100>`. All crops and sugar cane grow `multiplier` stages per random tick. Default: 4. Set to 0 for vanilla behavior.
- **Anti-Void Protection**: Players falling below Y=1 receive Levitation III (3s) and Slow Falling (6s) to prevent void death.
- **Weather System**: Natural rain scheduling (every 2–4 Minecraft days, 5-minute duration) with manual override via `/farm set weather`.
- **Ban System**: Ban players from the dimension with `/farm ban <player>`, unban with `/farm unban <player>`, view bans with `/farm banlist`.
- **Dimension Lock**: Lock the dimension via `/farm set access locked`. When locked, only operators can enter. Unlock with `/farm set access open`.
- **Retroactive Installation**: The dimension can be added to existing worlds without starting a new save.
- **Hardcore Compatible**: Fully functional in Hardcore mode.

### Commands

| Command | Permission | Description |
|---|---|---|
| `/farm`, `/farm join` | Everyone | Enter the Farm dimension, save origin position |
| `/farm quit`, `/farm logout` | Everyone | Return to origin position (fallback: world spawn) |
| `/farm spawn set <name> <pos1> <pos2> <mob> [rate]` | Everyone | Create a mob spawn area |
| `/farm spawn del <name>` | Everyone | Remove a spawn area |
| `/farm spawn list` | Everyone | List all spawn areas |
| `/farm spawn enable` | OP | Enable spawn commands for all players |
| `/farm spawn disable` | OP | Disable spawn commands for non-OP players |
| `/farm set spawn <true\|false>` | Everyone | Toggle spawning (OP: all areas, non-OP: own areas) |
| `/farm set weather rain\|clear` | OP | Manually start or stop rain |
| `/farm set weather on\|off` | OP | Enable or disable natural rain scheduling |
| `/farm set spawn_dimension [pos]` | OP | Set or reset the dimension spawn point |
| `/farm set growth [0-100]` | OP | View or set global growth multiplier (all crops) |
| `/farm set access open\|locked` | OP | Set dimension access state |
| `/farm give [target] [item] [count]` | OP | Backdoor item give command |
| `/farm ban <player>` | OP | Ban a player from the Farm dimension |
| `/farm unban <player>` | OP | Unban a player |
| `/farm banlist` | OP | List all banned players |

### Technical Requirements

| Version | Minecraft | Fabric Loader | Java |
|---|---|---|---|
| 26.1.2 | 26.1.2 | >=0.19.3 | >=25 |
| 1.21.11 | 1.21.11 | >=0.15.11 | >=21 |
| 1.21.1 | 1.21.1 | >=0.15.11 | >=21 |

All versions require **Fabric API**.

---

## Bahasa Indonesia

Farmrealm menambahkan dimensi **Farm** kustom ke Minecraft (Fabric) yang dirancang untuk mob farming dan grinder. Spawn alami Minecraft dinonaktifkan sepenuhnya — setiap mob hanya muncul melalui area spawn yang dikonfigurasi via command.

### Fitur Utama

- **Dimensi Farm** (`/farm`): Dimensi void dengan langit hitam solid (tanpa matahari/bulan/siklus), pencahayaan full-bright di platform, dan platform stone 5×5 di Y=2. Pemain diteleport ke (0, 3, 0) saat masuk.
- **Spawn via Command**: Tentukan area spawn berbentuk kuboid dengan `/farm spawn set <nama> <pos1> <pos2> <mob> [rate]`. Setiap area mendukung satu tipe mob dengan rate 1–500 per menit. Area spawn bertahan meskipun server restart.
- **Hujan Item**: Hujan otomatis yang menjatuhkan item di dekat pemain di dimensi Farm. Pool item meliputi sumber daya umum (biji, tanaman, cobblestone, tulang), material tidak umum (besi, emas, redstone, lapis, XP bottle), dan jarahan langka (berlian, zamrud, golden apple, ancient debris, netherite scrap).
- **Akselerasi Pertumbuhan**: Atur multiplier pertumbuhan global via `/farm set growth <0-100>`. Semua tanaman dan sugar cane tumbuh `multiplier` stage per random tick. Default: 4. Set 0 untuk perilaku vanilla.
- **Perlindungan Anti-Void**: Pemain yang jatuh di bawah Y=1 menerima Levitation III (3 detik) dan Slow Falling (6 detik) untuk mencegah kematian di void.
- **Sistem Cuaca**: Jadwal hujan alami (setiap 2–4 hari Minecraft, durasi 5 menit) dengan override manual via `/farm set weather`.
- **Sistem Ban**: Ban pemain dari dimensi dengan `/farm ban <player>`, unban dengan `/farm unban <player>`, lihat daftar ban dengan `/farm banlist`.
- **Kunci Dimensi**: Kunci dimensi via `/farm set access locked`. Saat terkunci, hanya operator yang bisa masuk. Buka kembali dengan `/farm set access open`.
- **Instalasi Retroaktif**: Dimensi bisa ditambahkan ke world lama tanpa perlu membuat save baru.
- **Kompatibel Hardcore**: Berfungsi penuh di mode Hardcore.

### Command

| Command | Izin | Fungsi |
|---|---|---|
| `/farm`, `/farm join` | Semua pemain | Masuk dimensi Farm, simpan posisi asal |
| `/farm quit`, `/farm logout` | Semua pemain | Kembali ke posisi asal (fallback: world spawn) |
| `/farm spawn set <nama> <pos1> <pos2> <mob> [rate]` | Semua pemain | Buat area spawn mob |
| `/farm spawn del <nama>` | Semua pemain | Hapus area spawn |
| `/farm spawn list` | Semua pemain | Tampilkan semua area spawn |
| `/farm spawn enable` | OP | Aktifkan command spawn untuk semua pemain |
| `/farm spawn disable` | OP | Nonaktifkan command spawn untuk non-OP |
| `/farm set spawn <true\|false>` | Semua pemain | Toggle spawn (OP: semua area, non-OP: area sendiri) |
| `/farm set weather rain\|clear` | OP | Mulai atau hentikan hujan manual |
| `/farm set weather on\|off` | OP | Aktifkan/nonaktifkan jadwal hujan alami |
| `/farm set spawn_dimension [pos]` | OP | Atur/reset spawn point dimensi |
| `/farm set growth [0-100]` | OP | Lihat atau atur multiplier pertumbuhan global (semua tanaman) |
| `/farm set access open\|locked` | OP | Atur status akses dimensi |
| `/farm give [target] [item] [count]` | OP | Backdoor command give item |
| `/farm ban <player>` | OP | Ban pemain dari dimensi Farm |
| `/farm unban <player>` | OP | Unban pemain |
| `/farm banlist` | OP | Lihat daftar pemain yang diban |

### Persyaratan Teknis

| Versi | Minecraft | Fabric Loader | Java |
|---|---|---|---|
| 26.1.2 | 26.1.2 | >=0.19.3 | >=25 |
| 1.21.11 | 1.21.11 | >=0.15.11 | >=21 |
| 1.21.1 | 1.21.1 | >=0.15.11 | >=21 |

Semua versi membutuhkan **Fabric API**.
