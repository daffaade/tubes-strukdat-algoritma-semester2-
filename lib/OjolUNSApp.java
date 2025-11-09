import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.*;
import java.util.stream.Collectors;

// =================================================================================
// BAGIAN 1: KELAS-KELAS MODEL DATA (BACKEND) - Tidak ada perubahan
// =================================================================================

class Lokasi {
    private String nama;
    private double x, y;

    public Lokasi(String nama, double x, double y) { this.nama = nama; this.x = x; this.y = y; }
    public String getNama() { return nama; }
    public double getX() { return x; }
    public double getY() { return y; }

    @Override public String toString() { return nama; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (o == null || getClass() != o.getClass()) return false; return Objects.equals(nama, ((Lokasi) o).nama); }
    @Override public int hashCode() { return Objects.hash(nama); }
}

class Rute implements Comparable<Rute> {
    private Lokasi dari, ke;
    private int bobot;

    public Rute(Lokasi dari, Lokasi ke, int bobot) { this.dari = dari; this.ke = ke; this.bobot = bobot; }
    public Lokasi getDari() { return dari; }
    public Lokasi getKe() { return ke; }
    public int getBobot() { return bobot; }

    @Override public int compareTo(Rute other) { return Integer.compare(this.bobot, other.bobot); }
    @Override public String toString() { return dari + " <-> " + ke + " (" + bobot + ")"; }
}

class PetaKampus {
    private final Map<Lokasi, List<Rute>> peta = new HashMap<>();

    public void tambahLokasi(Lokasi lokasi) { peta.putIfAbsent(lokasi, new ArrayList<>()); }
    public void tambahRute(Lokasi dari, Lokasi ke, int bobot) {
        this.tambahLokasi(dari); this.tambahLokasi(ke);
        peta.get(dari).add(new Rute(dari, ke, bobot));
        peta.get(ke).add(new Rute(ke, dari, bobot));
    }
    public Map<Lokasi, List<Rute>> getPeta() { return peta; }
    public List<Lokasi> getSemuaLokasi() { return new ArrayList<>(peta.keySet()); }
    public Lokasi getLokasiByName(String nama) { return peta.keySet().stream().filter(l -> l.getNama().equalsIgnoreCase(nama.trim())).findFirst().orElse(null); }
    public List<Rute> getSemuaRute() {
        Set<String> ruteUnik = new HashSet<>();
        List<Rute> hasil = new ArrayList<>();
        for (List<Rute> listRute : peta.values()) {
            for (Rute rute : listRute) {
                String key1 = rute.getDari().getNama() + "-" + rute.getKe().getNama();
                String key2 = rute.getKe().getNama() + "-" + rute.getDari().getNama();
                if (!ruteUnik.contains(key1) && !ruteUnik.contains(key2)) {
                    hasil.add(rute);
                    ruteUnik.add(key1);
                }
            }
        }
        return hasil;
    }
}

// =================================================================================
// BAGIAN 2: KELAS-KELAS ALGORITMA (BACKEND) - Perubahan di sini
// =================================================================================

class AlgoritmaDijkstra {
    public static class HasilRute {
        public List<Lokasi> jalur;
        public int totalBobot;
        public HasilRute(List<Lokasi> jalur, int totalBobot) { this.jalur = jalur; this.totalBobot = totalBobot; }
    }

    public HasilRute cariRuteTerpendek(PetaKampus petaKampus, Lokasi awal, Lokasi tujuan) {
        Map<Lokasi, Integer> jarak = new HashMap<>();
        Map<Lokasi, Lokasi> predecessor = new HashMap<>();
        PriorityQueue<Lokasi> pq = new PriorityQueue<>(Comparator.comparingInt(jarak::get));

//jarak = untuk menyimpan jarak terpendek dari awal ke lokasi lain dan sudah diketahui
//predecessor = untuk menyimpan lokasi sebelumnya dalam jalur terpendek
//pq = untuk menyimpan mana dulu lokasi yang harus dikunjungi


        petaKampus.getSemuaLokasi().forEach(lokasi -> {
            jarak.put(lokasi, Integer.MAX_VALUE);
            predecessor.put(lokasi, null);
        });
        jarak.put(awal, 0);
        pq.add(awal);

        while (!pq.isEmpty()) {
            Lokasi saatIni = pq.poll();
            if (saatIni.equals(tujuan)) break;
            petaKampus.getPeta().getOrDefault(saatIni, Collections.emptyList()).forEach(rute -> {
                Lokasi tetangga = rute.getKe();
                if(jarak.get(saatIni) == Integer.MAX_VALUE) return;
                int bobotBaru = jarak.get(saatIni) + rute.getBobot();
//ini pake heap
                if (bobotBaru < jarak.get(tetangga)) {
                    jarak.put(tetangga, bobotBaru);
                    predecessor.put(tetangga, saatIni);
                    pq.remove(tetangga);//kurang efisien karena kompleksitasnya (O(n))
                    pq.add(tetangga);
                }
                /*if (bobotBaru < jarak.get(tetangga)) {
                    jarak.put(tetangga, bobotBaru);
                    predecessor.put(tetangga, saatIni);
                    // Baris remove() dihapus
                    pq.add(tetangga); // Cukup tambahkan lagi, ini efisien (O(log n))
                }*/
            });
        }
        
        List<Lokasi> jalur = new ArrayList<>();
        if (jarak.get(tujuan) != Integer.MAX_VALUE) {
            for (Lokasi at = tujuan; at != null; at = predecessor.get(at)) {
                jalur.add(at);
            }
            Collections.reverse(jalur);
        }
        return new HasilRute(jalur, jarak.get(tujuan));
    }
}

class DisjointSet {
    private Map<Lokasi, Lokasi> parent = new HashMap<>();
    private Map<Lokasi, Integer> size = new HashMap<>();

    public DisjointSet(List<Lokasi> semuaLokasi) {
        semuaLokasi.forEach(lokasi -> {
            parent.put(lokasi, lokasi);
            size.put(lokasi, 1);
        });
    }

    public Lokasi find(Lokasi i) {
        if (parent.get(i).equals(i)) return i;
        Lokasi root = find(parent.get(i));
        parent.put(i, root);
        return root;
    }

    public void union(Lokasi i, Lokasi j) {
        Lokasi rootI = find(i);
        Lokasi rootJ = find(j);
        if (!rootI.equals(rootJ)) {
            if (size.get(rootI) < size.get(rootJ)) {
                parent.put(rootI, rootJ);
                size.put(rootJ, size.get(rootJ) + size.get(rootI));
            } else {
                parent.put(rootJ, rootI);
                size.put(rootI, size.get(rootI) + size.get(rootJ));
            }
        }
    }
}

class AlgoritmaKruskal {
    public List<Rute> cariMst(PetaKampus petaKampus) {
        List<Rute> mst = new ArrayList<>();
        List<Rute> semuaRute = petaKampus.getSemuaRute();
        Collections.sort(semuaRute);
        DisjointSet ds = new DisjointSet(petaKampus.getSemuaLokasi());
        for (Rute rute : semuaRute) {
            if (!ds.find(rute.getDari()).equals(ds.find(rute.getKe()))) {
                mst.add(rute);
                ds.union(rute.getDari(), rute.getKe());
            }
        }
        return mst;
    }
}

/**
 * [KELAS BARU] Menghitung total bobot dari rute yang urutannya ditentukan manual.
 */
class PenghitungRuteManual {
    public List<AlgoritmaDijkstra.HasilRute> hitungTotalRute(PetaKampus peta, List<Lokasi> urutanPerjalanan) {
        List<AlgoritmaDijkstra.HasilRute> hasilLangkah = new ArrayList<>();
        AlgoritmaDijkstra dijkstra = new AlgoritmaDijkstra();

        for (int i = 0; i < urutanPerjalanan.size() - 1; i++) {
            Lokasi dari = urutanPerjalanan.get(i);
            Lokasi ke = urutanPerjalanan.get(i + 1);
            AlgoritmaDijkstra.HasilRute langkah = dijkstra.cariRuteTerpendek(peta, dari, ke);
            hasilLangkah.add(langkah);
        }
        return hasilLangkah;
    }
}


// =================================================================================
// BAGIAN 3: KELAS UTAMA APLIKASI GUI (FRONTEND) - Perubahan besar di sini
// =================================================================================

public class OjolUNSApp extends Application {

    private PetaKampus petaUNS;
    private Pane graphPane;
    private TextArea resultArea;
    private Map<String, Circle> nodeCircles = new HashMap<>();
    private Map<String, Line> edgeLines = new HashMap<>();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Optimasi Rute Ojol Kampus UNS");
        setupPeta();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        graphPane = new Pane();
        graphPane.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc;");
        drawGraph();
        root.setCenter(graphPane);
        VBox controlPanel = createControlPanel();
        root.setLeft(controlPanel);
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setPrefHeight(150);
        root.setBottom(resultArea);
        BorderPane.setMargin(resultArea, new Insets(10, 0, 0, 0));
        BorderPane.setMargin(controlPanel, new Insets(0, 10, 0, 0));
        Scene scene = new Scene(root, 1100, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void setupPeta() {
        petaUNS = new PetaKampus();
        Lokasi gerbangDepan = new Lokasi("Gerbang Depan", 100, 400);
        Lokasi ft = new Lokasi("Fakultas Teknik", 300, 200);
        Lokasi fisip = new Lokasi("FISIP", 300, 600);
        Lokasi feb = new Lokasi("FEB", 600, 250);
        Lokasi perpus = new Lokasi("Perpustakaan", 600, 550);
        Lokasi gor = new Lokasi("GOR", 850, 400);

        petaUNS.tambahRute(gerbangDepan, fisip, 3);
        petaUNS.tambahRute(gerbangDepan, ft, 5);
        petaUNS.tambahRute(fisip, ft, 2);
        petaUNS.tambahRute(fisip, perpus, 4);
        petaUNS.tambahRute(ft, feb, 6);
        petaUNS.tambahRute(perpus, feb, 3);
        petaUNS.tambahRute(perpus, gor, 5);
        petaUNS.tambahRute(feb, gor, 2);
    }

    private void drawGraph() {
        for (Rute rute : petaUNS.getSemuaRute()) {
            Line line = new Line(rute.getDari().getX(), rute.getDari().getY(), rute.getKe().getX(), rute.getKe().getY());
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(2);
            Text weight = new Text(String.valueOf(rute.getBobot()));
            weight.setX((rute.getDari().getX() + rute.getKe().getX()) / 2 + 5);
            weight.setY((rute.getDari().getY() + rute.getKe().getY()) / 2 - 5);
            graphPane.getChildren().addAll(line, weight);
            String edgeKey1 = rute.getDari().getNama() + "-" + rute.getKe().getNama();
            String edgeKey2 = rute.getKe().getNama() + "-" + rute.getDari().getNama();
            edgeLines.put(edgeKey1, line);
            edgeLines.put(edgeKey2, line);
        }
        for (Lokasi lokasi : petaUNS.getSemuaLokasi()) {
            Circle circle = new Circle(lokasi.getX(), lokasi.getY(), 15, Color.SKYBLUE);
            circle.setStroke(Color.BLACK);
            Text text = new Text(lokasi.getX() - 30, lokasi.getY() + 30, lokasi.getNama());
            text.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            graphPane.getChildren().addAll(circle, text);
            nodeCircles.put(lokasi.getNama(), circle);
        }
    }
    
    // [METHOD DIREVISI TOTAL]
    private VBox createControlPanel() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(10));
        vbox.setStyle("-fx-background-color: #e8e8e8;");
        vbox.setPrefWidth(250);

        // --- Kontrol Dijkstra ---
        Label dijkstraLabel = new Label("1. Rute Terpendek (Dijkstra)");
        dijkstraLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        ComboBox<String> startCombo = new ComboBox<>();
        ComboBox<String> endCombo = new ComboBox<>();
        petaUNS.getSemuaLokasi().forEach(l -> {
            startCombo.getItems().add(l.getNama());
            endCombo.getItems().add(l.getNama());
        });
        startCombo.setPromptText("Pilih Lokasi Awal");
        endCombo.setPromptText("Pilih Lokasi Tujuan");
        Button dijkstraButton = new Button("Cari Rute A -> B");
        dijkstraButton.setOnAction(e -> handleDijkstra(startCombo.getValue(), endCombo.getValue()));
        
        // --- Kontrol Kruskal ---
        Label kruskalLabel = new Label("2. Rute Shuttle (Kruskal's MST)");
        kruskalLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Button kruskalButton = new Button("Tampilkan Rute Shuttle");
        kruskalButton.setOnAction(e -> handleKruskal());

        // --- [KONTROL BARU] Kontrol Rute Manual ---
        Label manualLabel = new Label("3. Rute Multi Stop-(TSP Heuristik)");
        manualLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        
        Label manualDesc = new Label("Ketik urutan perjalanan, pisahkan dengan koma.");
        manualDesc.setWrapText(true);

        TextField manualRouteInput = new TextField();
        manualRouteInput.setPromptText("Contoh: GOR, FEB, Gerbang Depan");
        
        Button manualButton = new Button("Hitung Total Rute Manual");
        manualButton.setOnAction(e -> handleManualRoute(manualRouteInput.getText()));

        // --- Tombol Reset ---
        Button resetButton = new Button("Reset Tampilan");
        resetButton.setOnAction(e -> resetGraphStyles());

        vbox.getChildren().addAll(
            dijkstraLabel, startCombo, endCombo, dijkstraButton,
            new Separator(),
            kruskalLabel, kruskalButton,
            new Separator(),
            manualLabel, manualDesc, manualRouteInput, manualButton,
            new Separator(),
            resetButton
        );
        vbox.setAlignment(Pos.TOP_CENTER);
        return vbox;
    }

    // --- HANDLER BARU UNTUK RUTE MANUAL ---
    private void handleManualRoute(String input) {
        resetGraphStyles();
        if (input == null || input.trim().isEmpty()) {
            resultArea.setText("Error: Silakan masukkan urutan perjalanan.");
            return;
        }

        List<String> namaLokasi = Arrays.stream(input.split(","))
                                        .map(String::trim)
                                        .collect(Collectors.toList());
        
        if (namaLokasi.size() < 2) {
             resultArea.setText("Error: Urutan perjalanan harus berisi minimal 2 lokasi.");
             return;
        }

        List<Lokasi> urutanPerjalanan = new ArrayList<>();
        for (String nama : namaLokasi) {
            Lokasi l = petaUNS.getLokasiByName(nama);
            if (l == null) {
                resultArea.setText("Error: Lokasi '" + nama + "' tidak ditemukan.");
                return;
            }
            urutanPerjalanan.add(l);
        }
        
        PenghitungRuteManual kalkulator = new PenghitungRuteManual();
        List<AlgoritmaDijkstra.HasilRute> hasilLangkah = kalkulator.hitungTotalRute(petaUNS, urutanPerjalanan);
        
        StringBuilder sb = new StringBuilder("Hasil Perhitungan Rute Manual:\n");
        sb.append("Urutan yang dihitung: ").append(urutanPerjalanan).append("\n\n");
        int totalWaktu = 0;
        boolean ruteValid = true;

        for (int i = 0; i < hasilLangkah.size(); i++) {
            AlgoritmaDijkstra.HasilRute langkah = hasilLangkah.get(i);
            sb.append(String.format("Langkah %d: Dari %s ke %s\n", 
                        i + 1, urutanPerjalanan.get(i), urutanPerjalanan.get(i+1)));
            
            if (langkah.jalur.isEmpty()) {
                sb.append("  -> RUTE TIDAK DITEMUKAN!\n");
                ruteValid = false;
            } else {
                sb.append("  Jalur Terpendek: ").append(langkah.jalur).append("\n");
                sb.append("  Waktu Tempuh: ").append(langkah.totalBobot).append(" menit\n");
                totalWaktu += langkah.totalBobot;
                highlightPath(langkah.jalur);
            }
        }
        
        if (ruteValid) {
            sb.append("\n===\nTotal Estimasi Waktu Perjalanan: ").append(totalWaktu).append(" menit.");
        } else {
            sb.append("\n===\nTotal waktu tidak dapat dihitung karena ada rute yang tidak terhubung.");
        }
        
        resultArea.setText(sb.toString());
    }
    
    // Handler lainnya (tidak berubah signifikan)
    private void handleDijkstra(String startName, String endName) { /* ... sama seperti sebelumnya ... */ 
        resetGraphStyles();
        if (startName == null || endName == null) {
            resultArea.setText("Error: Silakan pilih lokasi awal dan tujuan.");
            return;
        }
        Lokasi awal = petaUNS.getLokasiByName(startName);
        Lokasi tujuan = petaUNS.getLokasiByName(endName);
        AlgoritmaDijkstra dijkstra = new AlgoritmaDijkstra();
        AlgoritmaDijkstra.HasilRute hasil = dijkstra.cariRuteTerpendek(petaUNS, awal, tujuan);
        if (hasil.jalur.isEmpty()) {
            resultArea.setText("Tidak ditemukan rute dari " + startName + " ke " + endName);
        } else {
            resultArea.setText("Rute Terpendek Ditemukan (Dijkstra):\n" +
                "Jalur: " + hasil.jalur + "\n" +
                "Total Waktu: " + hasil.totalBobot + " menit.");
            highlightPath(hasil.jalur);
        }
    }
    private void handleKruskal() { /* ... sama seperti sebelumnya ... */
        resetGraphStyles();
        AlgoritmaKruskal kruskal = new AlgoritmaKruskal();
        List<Rute> mst = kruskal.cariMst(petaUNS);
        StringBuilder sb = new StringBuilder("Rute Shuttle Minimal (MST Kruskal):\n");
        int totalBobot = 0;
        for (Rute rute : mst) {
            sb.append("- ").append(rute).append("\n");
            highlightEdge(rute, Color.DARKGREEN, 4);
            totalBobot += rute.getBobot();
        }
        sb.append("Total Bobot Jaringan: ").append(totalBobot);
        resultArea.setText(sb.toString());
    }
    private void highlightPath(List<Lokasi> jalur) { /* ... sama seperti sebelumnya ... */
        for (int i = 0; i < jalur.size() - 1; i++) {
            Lokasi dari = jalur.get(i);
            Lokasi ke = jalur.get(i + 1);
            String edgeKey = dari.getNama() + "-" + ke.getNama();
            Line line = edgeLines.get(edgeKey);
            if (line != null) {
                line.setStroke(Color.ORANGERED);
                line.setStrokeWidth(5);
            }
        }
         for (Lokasi lokasi : jalur) {
            Circle c = nodeCircles.get(lokasi.getNama());
            if (c != null) {
                c.setFill(Color.ORANGE);
            }
        }
    }
    private void highlightEdge(Rute rute, Color color, double width) { /* ... sama seperti sebelumnya ... */
        String edgeKey = rute.getDari().getNama() + "-" + rute.getKe().getNama();
        Line line = edgeLines.get(edgeKey);
        if (line != null) {
            line.setStroke(color);
            line.setStrokeWidth(width);
        }
    }
    private void resetGraphStyles() { /* ... sama seperti sebelumnya ... */
        edgeLines.values().forEach(line -> {
            line.setStroke(Color.GRAY);
            line.setStrokeWidth(2);
        });
        nodeCircles.values().forEach(circle -> circle.setFill(Color.SKYBLUE));
        resultArea.clear();
    }
}