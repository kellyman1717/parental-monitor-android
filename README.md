# Parental Monitor - Panduan Lengkap

## 📋 Daftar Isi
1. [Persiapan](#1-persiapan)
2. [Setup Firebase](#2-setup-firebase)
3. [Build Aplikasi](#3-build-aplikasi)
4. [Install & Setup di HP Anak](#4-install--setup-di-hp-anak)
5. [Dashboard di HP Orang Tua](#5-dashboard-di-hp-orang-tua)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Persiapan

### Yang Dibutuhkan

| Item | Keterangan |
|------|------------|
| **Android Studio** | Download di https://developer.android.com/studio |
| **Akun Google** | Untuk Firebase & Google Maps API |
| **HP Anak** | Android 8.0+ (API 26+) |
| **HP Orang Tua** | Android 8.0+ (untuk dashboard) |
| **Koneksi Internet** | Kedua HP harus online |

### Install Android Studio

1. Download Android Studio dari https://developer.android.com/studio
2. Install dengan pengaturan default
3. Buka Android Studio → tunggu sampai selesai download SDK
4. Pastikan **Android SDK 34** terinstall:
   - Menu: `Tools` → `SDK Manager`
   - Centang `Android 14.0 (API 34)`
   - Klik `Apply`

---

## 2. Setup Firebase

### Step 2.1: Buat Firebase Project

1. Buka **https://console.firebase.google.com**
2. Login dengan akun Google
3. Klik **"Create a project"** (atau "Add project")
4. Isi:
   - Project name: `Parental Monitor`
   - Google Analytics: **Disable** (tidak perlu)
5. Klik **"Create project"**
6. Tunggu sampai selesai → Klik **"Continue"**

### Step 2.2: Aktifkan Authentication

1. Di Firebase Console → klik **"Authentication"** (sidebar kiri)
2. Ktab **"Sign-in method"**
3. Klik **"Email/Password"**
4. Toggle **"Enable"** → ON
5. Klik **"Save"**

### Step 2.3: Buat Firestore Database

1. Di Firebase Console → klik **"Firestore Database"** (sidebar kiri)
2. Klik **"Create database"**
3. Pilih lokasi: **"asia-southeast2"** (Jakarta) atau terdekat
4. Pilih **"Start in test mode"** (untuk development)
5. Klik **"Create"**

#### Set Security Rules (PENTING!)

1. Tab **"Rules"**
2. Replace isi rules dengan:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Hanya user yang login yang bisa akses
    match /devices/{deviceId} {
      allow read, write: if request.auth != null;

      match /{subcollection}/{document} {
        allow read, write: if request.auth != null;
      }
    }
  }
}
```

3. Klik **"Publish"**

### Step 2.4: Aktifkan Cloud Messaging

1. Di Firebase Console → klik **"Project Settings"** (gear icon)
2. Tab **"Cloud Messaging"**
3. Pastikan **Firebase Cloud Messaging API (V1)** aktif

### Step 2.5: Tambahkan Android App

1. Di Firebase Console → klik **"Project Settings"** (gear icon)
2. Scroll ke bawah → klik icon **Android** (tambahkan app)
3. Isi:
   - Android package name: `com.parentalmonitor`
   - App nickname: `Parental Monitor Child`
   - Debug signing certificate SHA-1: **Kosongkan dulu** (opsional)
4. Klik **"Register app"**

### Step 2.6: Download google-services.json

1. Setelah register app, klik **"Download google-services.json"**
2. **PENTING:** Simpan file ini, nanti akan ditaruh di project

---

## 3. Build Aplikasi

### Step 3.1: Buka Project di Android Studio

1. Buka **Android Studio**
2. Klik **"Open"** (bukan "New Project")
3. Navigate ke folder `D:\Project\ParentalMonitor`
4. Klik **"OK"**
5. Tunggu sampai Gradle sync selesai (bisa 5-10 menit pertama kali)

### Step 3.2: Taruh google-services.json

1. Copy file `google-services.json` yang didownload tadi
2. Paste ke folder `D:\Project\ParentalMonitor\app\`
3. **Bukan** di dalam `src/`, tapi langsung di folder `app/`

```
ParentalMonitor/
├── app/
│   ├── google-services.json  ← TARUH DI SINI
│   ├── build.gradle.kts
│   └── src/
```

### Step 3.3: Get Google Maps API Key

1. Buka **https://console.cloud.google.com**
2. Pastikan project Firebase yang tadi dipilih
3. Menu **"APIs & Services"** → **"Credentials"**
4. Klik **"Create Credentials"** → **"API Key"**
5. Copy API Key yang muncul
6. Buka file `app/src/main/AndroidManifest.xml`
7. Tambahkan sebelum `</application>`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE"/>
```

8. Replace `YOUR_API_KEY_HERE` dengan API key yang didapat
9. Enable **Maps SDK for Android**:
   - Menu **"APIs & Services"** → **"Library"**
   - Search **"Maps SDK for Android"**
   - Klik → **"Enable"**

### Step 3.4: Sync & Build

1. Di Android Studio → klik **"File"** → **"Sync Project with Gradle Files"**
2. Tunggu sampai selesai (lihat progress bar di bawah)
3. Jika ada error, coba:
   - **"File"** → **"Invalidate Caches / Restart"**
   - Atau klik **"Build"** → **"Clean Project"** → **"Rebuild Project"**

### Step 3.5: Generate APK

#### Option A: Debug APK (untuk testing)

1. Menu: **"Build"** → **"Build Bundle(s) / APK(s)"** → **"Build APK(s)"**
2. Tunggu sampai selesai
3. Klik **"locate"** di notification yang muncul
4. APK ada di: `app/build/outputs/apk/debug/app-debug.apk`

#### Option B: Release APK (untuk distribusi)

1. Menu: **"Build"** → **"Generate Signed Bundle / APK"**
2. Pilih **"APK"** → **"Next"**
3. Buat keystore baru:
   - Key store path: `D:\Project\ParentalMonitor\keystore.jks`
   - Password: buat password (misal: `parental123`)
   - Alias: `parental`
   - Validity: `25` tahun
   - First/Last name: isi bebas
4. Klik **"Next"**
5. Pilih **"release"** → **"Finish"**
6. APK ada di: `app/build/outputs/apk/release/app-release.apk`

---

## 4. Install & Setup di HP Anak

### Step 4.1: Install APK

1. Copy file APK ke HP anak (via USB, Bluetooth, atau Google Drive)
2. Di HP anak → buka **File Manager** → cari file APK
3. Tap APK → **"Install"**
4. Jika muncul "Install from unknown sources":
   - Tap **"Settings"** → toggle **"Allow from this source"**
   - Kembali → **"Install"**

### Step 4.2: Buka Aplikasi (Hidden)

Aplikasi **TIDAK** muncul di launcher! Untuk membuka:

1. Buka **aplikasi Telepon/Dialer**
2. Ketik: `*#*#1234#*#*`
   - Gunakan tombol `*` dan `#` di dialer
   - Setelah angka terakhir diketik, app akan terbuka otomatis
3. Muncul **"Setup Monitoring"**

### Step 4.3: Grant Permissions

Di halaman Setup:

1. Tap **"Grant Permissions"**
2. Muncul popup permission → tap **"Allow"** untuk semua:
   - ✅ Location (Lokasi)
   - ✅ SMS
   - ✅ Call logs (Panggilan)
   - ✅ Phone state
   - ✅ Notifications
3. Untuk **Background Location**:
   - Muncul popup → pilih **"Allow all the time"** (bukan "Only while using")

### Step 4.4: Enable Special Permissions

Masih di halaman Setup:

1. Tap **"Accessibility Service"** → aktifkan toggle
2. Tap **"Notification Listener"** → aktifkan toggle
3. Tap **"Usage Stats Access"** → aktifkan untuk Parental Monitor

### Step 4.5: Disable Battery Optimization

1. Tap **"Disable Battery Optimization"**
2. Cari **"Parental Monitor"** di daftar
3. Tap → pilih **"Don't optimize"**
4. Tap **"Done"**

### Step 4.6: Start Monitoring

1. Tap tombol **"Mulai Monitoring"** 🔵
2. Muncul notifikasi "Monitoring aktif"
3. Aplikasi akan berjalan di background terus

### Step 4.7: Verifikasi

1. Cek notifikasi bar → pastikan ada icon "Parental Monitor"
2. Restart HP → pastikan notifikasi muncul kembali (auto-start)
3. Buka Firebase Console → Firestore → cek collection `devices`
4. Harusnya ada data device baru

---

## 5. Dashboard di HP Orang Tua

### Step 5.1: Install APK yang Sama

1. Install APK yang sama ke HP orang tua
2. Buka dari launcher (aplikasi "Parental Monitor")

### Step 5.2: Buat Akun

1. Muncul halaman **"Login Orang Tua"**
2. Tap **"Belum punya akun? Daftar"**
3. Isi:
   - Email: email Anda
   - Password: minimal 6 karakter
4. Tap **"Daftar"**

### Step 5.3: Lihat Dashboard

Setelah login, muncul Dashboard dengan:

- 📍 **Lokasi Terakhir** — tap untuk lihat peta
- 📊 **Statistik** — jumlah SMS, Panggilan, WhatsApp
- 💬 **Pesan Terbaru** — tap "Lihat Semua"
- 📞 **Panggilan Terbaru** — tap "Lihat Semua"
- 🟢 **WhatsApp Terbaru** — tap "Lihat Semua"

### Menu Lengkap:

| Menu | Fungsi |
|------|--------|
| 📍 **Lokasi** | Peta + riwayat lokasi GPS |
| 📱 **Aplikasi** | Penggunaan aplikasi per hari |
| 💬 **Pesan** | Tab SMS + WhatsApp |
| 🔋 **Baterai** | Status baterai + app paling boros |
| ⚙️ **Pengaturan** | Status monitoring, permissions, kode rahasia |

---

## 6. Troubleshooting

### Masalah: Gradle Sync Gagal

```
Solusi:
1. Pastikan Android Studio versi terbaru (2023.1+)
2. File → Invalidate Caches → Restart
3. Hapus folder .gradle di project, sync ulang
4. Pastikan koneksi internet stabil
```

### Masalah: google-services.json Error

```
Solusi:
1. Pastikan file di taruh di folder app/ (bukan di src/)
2. Pastikan package name = com.parentalmonitor
3. Download ulang dari Firebase Console
```

### Masalah: Aplikasi Tidak Muncul di Dialer

```
Solusi:
1. Pastikan ketik: *#*#1234#*#* (bukan *#1234#*)
2. Gunakan dialer bawaan HP (bukan dialer Google)
3. Beberapa HP: coba *#*#12345#*#* atau ##1234##
```

### Masalah: Monitoring Berhenti Setelah Restart

```
Solusi:
1. Pastikan Boot Receiver terdaftar di AndroidManifest
2. Disable Battery Optimization untuk app ini
3. Di beberapa HP (Xiaomi, OPPO, Vivo):
   - Settings → Apps → Parental Monitor → Autostart → ON
   - Settings → Battery → App Battery Saver → Don't restrict
```

### Masalah: Data Tidak Muncul di Firebase

```
Solusi:
1. Cek koneksi internet di HP anak
2. Cek Firestore Rules (pastikan allow read/write)
3. Cek Logcat di Android Studio (filter: "MainForeground")
4. Pastikan google-services.json benar
```

### Masalah: WhatsApp Tidak Terbaca

```
Solusi:
1. Pastikan Notification Listener aktif:
   Settings → Notifications → Notification access → Parental Monitor → ON
2. Pastikan WhatsApp notifikasi diaktifkan:
   WhatsApp → Settings → Notifications → ON
3. Jangan mute chat yang ingin dipantau
4. Jangan buka WhatsApp langsung (biarkan pesan jadi notifikasi)
```

### Masalah: Lokasi Tidak Akurat

```
Solusi:
1. Pastikan GPS aktif (mode High Accuracy)
2. Allow "All the time" untuk background location
3. Tunggu beberapa menit setelah setup
4. Indoor kadang kurang akurat (normal)
```

---

## 📱 Daftar HP yang Perlu Setup Khusus

Beberapa HP Android punya fitur "battery saver" yang agresif:

### Xiaomi / Redmi / POCO
```
Settings → Apps → Manage apps → Parental Monitor:
  ✅ Autostart: ON
  ✅ Battery Saver: No restrictions
Settings → Battery → Battery Saver:
  ✅ Off, atau whitelist Parental Monitor
```

### OPPO / Realme
```
Settings → App Management → Parental Monitor:
  ✅ Allow Auto Launch: ON
  ✅ Allow Background Running: ON
Settings → Battery → Power Saving Mode:
  ✅ Disable, atau whitelist
```

### Vivo / iQOO
```
Settings → Apps → Parental Monitor:
  ✅ Autostart: ON
Settings → Battery → Background Power Consumption:
  ✅ High Background Power Consumption: ON
```

### Samsung
```
Settings → Apps → Parental Monitor → Battery:
  ✅ Unrestricted
Settings → Device Care → Battery → Background Usage Limits:
  ✅ Hapus dari "Sleeping apps" dan "Deep sleeping apps"
```

### Huawei / Honor
```
Settings → Battery → App Launch:
  ✅ Parental Monitor → Manage Manually
  ✅ Auto-launch: ON
  ✅ Secondary Launch: ON
  ✅ Run in Background: ON
```

---

## ✅ Checklist Sebelum Selesai

### HP Anak:
- [ ] APK terinstall
- [ ] Semua permission granted
- [ ] Accessibility Service aktif
- [ ] Notification Listener aktif
- [ ] Usage Stats access aktif
- [ ] Battery optimization disabled
- [ ] Autostart enabled (untuk Xiaomi/OPPO/Vivo)
- [ ] Notifikasi "Monitoring aktif" muncul
- [ ] Data muncul di Firebase Console

### HP Orang Tua:
- [ ] APK terinstall
- [ ] Akun terdaftar dan login
- [ ] Dashboard menampilkan data
- [ ] Peta lokasi berfungsi
- [ ] SMS, Panggilan, WhatsApp terlihat

---

## 🔐 Keamanan

- Data dienkripsi sebelum upload ke Firebase
- Hanya akun orang tua yang terotentikasi bisa akses
- Aplikasi tersembunyi dari launcher HP anak
- Akses hanya via kode rahasia `*#*#1234#*#*`
- Security Rules Firebase membatasi akses

---

## ⚖️ Legalitas

Aplikasi ini sah untuk:
- ✅ Kontrol orang tua atas anak di bawah umur
- ✅ Dengan sepengetahuan anak
- ✅ Tujuan keamanan anak

**PENTING:** Beri tahu anak bahwa HP-nya dipantau. Transparansi penting untuk kepercayaan.
