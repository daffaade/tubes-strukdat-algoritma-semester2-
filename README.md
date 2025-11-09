# 🚖 OjolUNSApp - Optimasi Rute Ojol Kampus UNS

**OjolUNSApp** adalah aplikasi JavaFX yang mensimulasikan sistem pencarian rute optimal untuk transportasi ojol (ojek online) di area Kampus UNS.  
Aplikasi ini memvisualisasikan **graf kampus UNS**, dan menggunakan **algoritma graf populer** seperti:

- Dijkstra → untuk mencari rute terpendek  
- Kruskal → untuk menentukan jalur shuttle kampus minimal (MST)  
- Rute Manual (heuristik TSP sederhana) → untuk menghitung rute multi-stop berdasarkan urutan lokasi yang dimasukkan pengguna  

---

## 🧠 Fitur Utama

### 1️⃣ Rute Terpendek (Dijkstra)
- Pilih lokasi awal dan tujuan.  
- Aplikasi menampilkan jalur terpendek dan total bobot (estimasi waktu).  
- Jalur diwarnai **oranye** di graf.

### 2️⃣ Rute Shuttle (Kruskal's MST)
- Menghasilkan **Minimum Spanning Tree** (rute terhubung dengan bobot total minimal).  
- Visualisasi garis berwarna **hijau tua (dark green)**.

### 3️⃣ Rute Multi-Stop (Manual / TSP Heuristik)
- Pengguna dapat mengetik urutan lokasi yang ingin dilewati, misalnya:
