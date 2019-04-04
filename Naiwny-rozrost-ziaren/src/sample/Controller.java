package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    // odnośniki do rzeczy w scenebuilderze
    public Canvas canvas_id;
    public TextField size_id;
    public TextField grainNumber_id;
    public ComboBox comboNgbh_id;
    public ComboBox comboSowingGrains_id;
    public Button drawBtton_id;
    public Button grainMakerButton_id;
    public Button startButton_id;
    public CheckBox checkPeriodical_id;
    public CheckBox showMesh_id;
    public CheckBox checkInteraction_id;
    public TextField generationField_id;

    // zmienne do pobierania
    int rozmiar;
    int iloscZiaren;
    int liczbaIteracji;
    String metodaSasiedztwa, typZasiewu;
    boolean check_periodyczny, check_pokaSiate, check_iteracje;

    public GraphicsContext gc;              // rysuje na canvasie
    Cell[][] cells;                         // tablica komórek
    Cell[][] cells_tmp;               // tablica komórek pomocnicza na rzecz sąsiedztwa (znaczy które później zamalować)
    public List<Color> colorsList;          // do sprawdzania w planszy czy kolor można jeszcze wykorzystać
    public List<PositionXY> positionXYList;     // czy pozycję, która nie została usunięta można jeszcze wykorzystać

    // uzupełnianie rozwijalnych list dotyczących wyboru sąsiedztwa oraz typu zasiewu
    ObservableList<String> optionsMethods = FXCollections.observableArrayList(
            "Von Neumann", "Moore", "Hexagonalne L", "Hexagonalne P", "Hexagonalne Losowe", "Pentagonalne Losowe");

    ObservableList<String> optionsTypes = FXCollections.observableArrayList(
            "Randomowe", "Kliknięcie myszy");//, "Promień", "Równomierne");


    // pobieranie wartości z elementów scene builder
    private void pobierzRozmiarSiatki() {
        rozmiar = Integer.parseInt(size_id.getText());
       // System.out.println(rozmiar);
    }
    private void pobierzIloscZiaren() {
        iloscZiaren = Integer.parseInt(grainNumber_id.getText());
        // System.out.println(iloscZiaren);
    }
    private void pobierzGeneracje() {
        liczbaIteracji = Integer.parseInt(generationField_id.getText());
       // System.out.println(liczbaIteracji);
    }
    private void pobierzMetodeSasiedztwa() {
        metodaSasiedztwa = comboNgbh_id.getSelectionModel().getSelectedItem().toString();
       // System.out.println(metodaSasiedztwa);
    }
    private void pobierzTypZasiewu() {
        typZasiewu = comboSowingGrains_id.getSelectionModel().getSelectedItem().toString();
       // System.out.println(typZasiewu);
    }
    private void pobierzCheckWB() {
        check_periodyczny = checkPeriodical_id.isSelected();
       // System.out.println(check_periodyczny);
    }
    private void pobierzCheckPokaSiate() {
        check_pokaSiate = showMesh_id.isSelected();
       // System.out.println(check_pokaSiate);
    }
    private void pobierzCheckIteracje() {
        check_iteracje = checkInteraction_id.isSelected();
       // System.out.println(check_iteracje);
    }
    private void pobierzWszystkieDane() {
        pobierzRozmiarSiatki();
        pobierzIloscZiaren();
        pobierzMetodeSasiedztwa();
        pobierzTypZasiewu();
        pobierzCheckWB();
        pobierzCheckPokaSiate();
        pobierzCheckIteracje();
        pobierzGeneracje();
    }       // metoda robiąca wszystkie pobierania wyżej

    // wyczyszczenie sceny (używane w rysowaniu siatki)
    private void wyczyscCanvas() {
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
    }

    // rysowanie siatki na planszy
    private void rysujSiatke() {
        pobierzCheckPokaSiate();            // pobieranie czy check z poka siatkę jest zaznaczony
        if (check_pokaSiate) {
            gc.beginPath();
            gc.setLineWidth(1);
            gc.setStroke(Color.LIGHTPINK);

            double x, y;
            x = canvas_id.getWidth() / rozmiar;     // "dzieli" szerokość
            y = canvas_id.getHeight() / rozmiar;    // "dzieli" wysokość

            for (int i = 0; i <= rozmiar; i++) {
                gc.moveTo(x * i, 0);
                gc.lineTo(x * i, gc.getCanvas().getWidth());
            }

            for (int i = 0; i <= rozmiar; i++) {
                gc.moveTo(0, y * i);
                gc.lineTo(gc.getCanvas().getHeight(), y * i);
            }
            gc.stroke();
            gc.closePath();
        }
    }

    // wypełnia komórki
    private void wypelnijKomorki() {
        double width, height;
        width = canvas_id.getWidth() / rozmiar;
        height = canvas_id.getHeight() / rozmiar;
        gc.beginPath();
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = 0; i < rozmiar; i++) {
            for (int j = 0; j < rozmiar; j++) {
                if (cells[i][j].isStan()) { // jeśli stan komórki jest 1, czyli wypełniony
                    // pobranie koloru komórki
                    gc.setFill(cells[i][j].getColor());         // ustawia wypełnienie komórki poprzez pobranie koloru
                    gc.fillRect(width * i, height * j, width, height);
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------- typy zasiewu
    // randomowe
    private void randomZasiew() {
        pobierzIloscZiaren();
        int x;      // do losowania randomowej pozycji
        gc.setLineWidth(1);
        gc.setFill(Color.BLACK);
        Random random = new Random();
        PositionXY positionNow;
        float r, g, b;              // zmienne do losowania koloru

        for (int i = 0; i < iloscZiaren; i++) {
            r = random.nextFloat();
            g = random.nextFloat();
            b = random.nextFloat();
            x = random.nextInt(positionXYList.size());
            positionNow = positionXYList.get(x);
            while (colorsList.contains(Color.color(r, g, b))) {
                // sprawdza czy kolor istnieje już na planszy, jeśli istnieje to ponownie losuje
                r = random.nextFloat();
                g = random.nextFloat();
                b = random.nextFloat();
            }
            colorsList.add(Color.color(r, g, b));                   // dodanie koloru do listy użytych
            cells[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
            cells[positionNow.x][positionNow.y].stan = (true);      // zamalowana
            cells_tmp[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
            cells_tmp[positionNow.x][positionNow.y].stan = (true);
            positionXYList.remove(x);                               // usuwa pozycję z "dostępnych" do wylosowania
            if (positionXYList.size() < 1) break;
        }
    }

    // za pomocą wciśnięci przycisku myszy
    private void mouseZasiew() {
        pobierzTypZasiewu();
        canvas_id.addEventHandler(MouseEvent.MOUSE_PRESSED, (EventHandler<MouseEvent>) event -> {
            Random random = new Random();
            PositionXY positionNow;
            float r, g, b;
            // pobieranie po kliknięciu z planszy
            double x = event.getX();
            double y = event.getY();
            r = random.nextFloat();
            g = random.nextFloat();
            b = random.nextFloat();
            double width, height;   // miejsce gdzie powinno wrzucić
            width = (x / canvas_id.getWidth()) * rozmiar;
            height = (y / canvas_id.getHeight()) * rozmiar;
            positionNow = new PositionXY((int) width, (int) height);
            if (typZasiewu.equals("Kliknięcie myszy")) {
                while (colorsList.contains(Color.color(r, g, b))) {
                    // sprawdza czy kolor istnieje, jeśli istnieje to ponownie losuje
                    r = random.nextFloat();
                    g = random.nextFloat();
                    b = random.nextFloat();
                }
                if (!cells[positionNow.x][positionNow.y].isStan()) {
                    colorsList.add(Color.color(r, g, b));
                    // przypisanie koloru do ustawianej komórki
                    cells[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
                    cells[positionNow.x][positionNow.y].stan = (true);      // ustawienie, że teraz jest "pełna"
                    cells_tmp[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
                    cells_tmp[positionNow.x][positionNow.y].stan = (true);
                    positionXYList.remove(positionNow);
                    wypelnijKomorki();
                } else { System.out.println("Zamalowane, spróbuj gdzie indziej"); }
            }
        });
    }

    // równomierny ----> nie dziaua
    private void rownomierneZasiew() {
        pobierzIloscZiaren();
        gc.setLineWidth(1);
        gc.setFill(Color.BLACK);
        Random random = new Random();
        PositionXY positionNow;
        float r, g, b;              // zmienne do losowania koloru
        //if (iloscZiaren % 2 != 0) iloscZiaren++;
        int sq = (int)Math.round(Math.sqrt(iloscZiaren));
        int step = rozmiar/sq;
        double x = 0;
        double y = 0;

        for (int i = 0; i < iloscZiaren; i++) {
            r = random.nextFloat();
            g = random.nextFloat();
            b = random.nextFloat();
            for (int k = step; k<= rozmiar - step; k++) {
                for (int l = step; l <= rozmiar - step; l++) {
                    x = l * step;
                }
                y = k * step;
            }
//            double width, height;   // miejsce gdzie powinno wrzucić
//            width = (x / canvas_id.getHeight()) * rozmiar;
//            height = (y / canvas_id.getHeight()) * rozmiar;
            positionNow = new PositionXY((int) x, (int) y);

            while (colorsList.contains(Color.color(r, g, b))) {
                // sprawdza czy kolor istnieje już na planszy, jeśli istnieje to ponownie losuje
                r = random.nextFloat();
                g = random.nextFloat();
                b = random.nextFloat();
            }
//            if (!cells[positionNow.x][positionNow.y].isStan()) {
//                colorsList.add(Color.color(r, g, b));
//                // przypisanie koloru do ustawianej komórki
//                cells[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
//                cells[positionNow.x][positionNow.y].stan = (true);      // ustawienie, że teraz jest "pełna"
//                cells_tmp[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
//                cells_tmp[positionNow.x][positionNow.y].stan = (true);
//                positionXYList.remove(positionNow);
//                wypelnijKomorki();
//            }

            colorsList.add(Color.color(r, g, b));                   // dodanie koloru do listy użytych
            cells[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
            cells[positionNow.x][positionNow.y].stan = (true);      // zamalowana
            cells_tmp[positionNow.x][positionNow.y].color = (Color.color(r, g, b));
            cells_tmp[positionNow.x][positionNow.y].stan = (true);
            positionXYList.remove(x);                               // usuwa pozycję z "dostępnych" do wylosowania
            if (positionXYList.size() < 1) break;
        }
    }

    // ------------------------------------------------------------------------------------------------- typy sąsiedztwa
    // w wątku, by obliczenia były robione osobno, a rysowanie osobno - zwiekszenie wydajności niż funkcjami
    public class UruchomienieMetod extends Thread {
        @Override
        public void run() {
            super.run();
            // w zależności od liczby iteracji
            if (check_iteracje) {                   // jeśli checkbox zaznaczony, to robimy ilość iteracji
                for (int i = 0; i < liczbaIteracji; i++) {
                    for (int j = 0; j < rozmiar; j++) {
                        for (int k = 0; k < rozmiar; k++) {
                            PositionXY positionXY = new PositionXY(j, k);
                            if (!cells[j][k].isStan()) {
                                mooreSasiedztwa(positionXY);
                            }
                        }
                    }
                    rysujPoSasiedztwie();
                }
            } else {                               // dopóki istnieją niezapełnione komórki
                while(positionXYList.size() > 1) {
                    for (int j = 0; j < rozmiar; j++) {
                        if(positionXYList.size() < 1) break;
                        for (int k = 0; k < rozmiar; k++) {
                            if(positionXYList.size() < 1) break;
                            PositionXY positionXY = new PositionXY(j, k);
                            if (!cells[j][k].isStan()) {
                                mooreSasiedztwa(positionXY);
                            }
                        }
                    }
                    rysujPoSasiedztwie();
                }
            }
        }
    }

    // ustawienie sąsiadów komórek
    // ┌──────┬───────┬──────┐
    // │  4  │   3  │  7  │
    // ├──────┼───────┼──────┤
    // │  0  │ i, j │  1  │
    // ├──────┼───────┼──────┤
    // │  6  │   2  │  5  │
    // └──────┴───────┴──────┘

    public void rysujPoSasiedztwie() {
        double width, height;
        width = canvas_id.getWidth() / rozmiar;
        height = canvas_id.getHeight() / rozmiar;
        gc.beginPath();
        gc.setFill(Color.BLACK);
        gc.setLineWidth(1);
        for (int i = 0; i < rozmiar; i++) {
            for (int j = 0; j < rozmiar; j++) {
                if (cells_tmp[i][j].isStan()) {
                    gc.setFill(cells_tmp[i][j].getColor());
                    gc.fillRect(width * i, height * j, width, height);
                }
            }
        }
        // użycie tablicy pomocniczej
        cells = cells_tmp;
        cells_tmp = new Cell[rozmiar][rozmiar];
        for (int i = 0; i < rozmiar; i++) {
            for (int j = 0; j < rozmiar; j++) {
                cells_tmp[i][j] = new Cell();
            }
        }
    }

    public void mooreSasiedztwa(PositionXY positionXY) {
        int i, j;
        i = positionXY.x;
        j = positionXY.y;
        // wstępne ustawienie sąsiadów
        boolean check0 = true; boolean check1 = true; boolean check2 = true; boolean check3 = true;
        boolean check4 = true; boolean check5 = true; boolean check6 = true; boolean check7 = true;
        // ... oraz pozycji
        PositionXY s0 = null; PositionXY s1 = null; PositionXY s2 = null; PositionXY s3 = null;
        PositionXY s4 = null; PositionXY s5 = null; PositionXY s6 = null; PositionXY s7 = null;

        List<ColorMultiplicyty> cN = new ArrayList<>();

        // ustawienie warunków brzegowych periodycznych
        if (j == 0) {
            if (check_periodyczny) {
                     s0 = new PositionXY(i, (rozmiar - 1));
            } else { check0 = false; }
        } else {
            s0 = new PositionXY(i, (j - 1));
        }

        if (j == (rozmiar - 1)) {
            if (check_periodyczny) {
                     s1 = new PositionXY(i, 0);
            } else { check1 = false; }
        } else {
            s1 = new PositionXY(i, (j + 1));
        }

        if (i == (rozmiar - 1)) {
            if (check_periodyczny) {
                     s2 = new PositionXY(0, j);
            } else { check2 = false; }
        } else {
            s2 = new PositionXY((i + 1), j);
        }

        if (i == 0) {
            if (check_periodyczny) {
                     s3 = new PositionXY((rozmiar - 1), j);
            } else { check3 = false; }
        } else {
            s3 = new PositionXY((i - 1), j);
        }

    // sprawdzanie wartości "srodkowych" nie krawędziowych
        if (((i > 0) && (i < (rozmiar - 1))) && ((j > 0) && (j < (rozmiar - 1)))) {
            s4 = new PositionXY(i + 1, j + 1);
            s5 = new PositionXY(i - 1, j - 1);
            s6 = new PositionXY(i - 1, j + 1);
            s7 = new PositionXY(i + 1, j - 1);
        } else {
            if (check_periodyczny) {
                if (i == (rozmiar - 1)) {
                    if (j == (rozmiar - 1)) {
                        s7 = new PositionXY(0, j - 1);
                        s6 = new PositionXY(i - 1, 0);
                        s4 = new PositionXY(0, 0);
                    } else {
                        if (j != 0) {
                            s7 = new PositionXY(0, j - 1);
                        } else {
                            s7 = new PositionXY(0, (rozmiar - 1));
                        }
                        s6 = new PositionXY(i - 1, j + 1);
                        s4 = new PositionXY(0, j + 1);
                    }
                } else {
                    if (j == (rozmiar - 1)) {
                        s7 = new PositionXY(i + 1, j - 1);
                        if (i > 0) {
                            s6 = new PositionXY(i - 1, 0);
                        } else {
                            s6 = new PositionXY((rozmiar - 1), 0);
                        }
                        s4 = new PositionXY(i + 1, 0);
                    } else {
                        if (j > 0) {
                            s7 = new PositionXY(i + 1, j - 1);
                        } else {
                            s7 = new PositionXY(i + 1, (rozmiar - 1));
                        }
                        if (i > 0) {
                            s6 = new PositionXY(i - 1, j + 1);
                        } else {
                            s6 = new PositionXY((rozmiar - 1), j + 1);
                        }
                        s4 = new PositionXY(i + 1, j + 1);
                    }
                }

                if (i == 0 && j == 0) {
                    s5 = new PositionXY((rozmiar - 1), (rozmiar - 1));
                    s6 = new PositionXY((rozmiar - 1), j + 1);
                }
                if (i == 0 && j != 0) {
                    s5 = new PositionXY((rozmiar - 1), j - 1);
                    if (j != (rozmiar - 1)) {
                        s6 = new PositionXY((rozmiar - 1), j + 1);
                    } else {
                        s6 = new PositionXY((rozmiar - 1), 0);
                    }
                }
                if (i != 0 && j != 0) {
                    s5 = new PositionXY(i - 1, j - 1);
                    if (j != (rozmiar - 1)) {
                        s6 = new PositionXY(i - 1, j + 1);
                    } else {
                        s6 = new PositionXY(i - 1, 0);
                    }
                }
                if (i != 0 && j == 0) {
                    s5 = new PositionXY(i - 1, (rozmiar - 1));
                    s6 = new PositionXY(i - 1, j + 1);
                } //koniec periodycznego
            } else {
                if (i < (rozmiar - 1) && j < (rozmiar - 1)) {
                    s4 = new PositionXY(i + 1, j + 1);
                } else { check4 = false; }

                if (i == 0 && j == 0) {
                    check5 = false;
                    check6 = false;
                    check7 = false;
                }
                if (i == 0 && j != 0) {
                    check5 = false;
                    check6 = false;
                    s7 = new PositionXY(i + 1, j - 1);
                }
                if (i != 0 && j != 0) {
                    if (i != (rozmiar - 1)) {
                        s7 = new PositionXY(i + 1, j - 1);
                    } else {
                        check7 = false;
                    }
                    s5 = new PositionXY(i - 1, j - 1);
                    if (j == (rozmiar - 1)) {
                        check6 = false;
                    } else {
                        s6 = new PositionXY(i - 1, j + 1);
                    }
                }
                if (i != 0 && j == 0) {
                    s6 = new PositionXY(i - 1, j + 1);
                    check7 = false;
                    check5 = false;
                }
            }
        }

        if (metodaSasiedztwa.equals("Von Neumann")) {
            check4 = false;
            check7 = false;
            check6 = false;
            check5 = false;
        }

        if (metodaSasiedztwa.equals("Hexagonalne L")) {
            check6 = false;
            check7 = false;
        }
        if (metodaSasiedztwa.equals("Hexagonalne P")) {
            check4 = false;
            check5 = false;
        }

        if (metodaSasiedztwa.equals("Hexagonalne Losowe")) {
            Random random = new Random();
            int hexRand2 = random.nextInt(2);
            if (hexRand2 == 0) {
                check6 = false;
                check7 = false;
            } else {
                check4 = false;
                check5 = false;
            }
        }
        if (metodaSasiedztwa.equals("Pentagonalne Losowe")) {
            Random random = new Random();
            int pentRand2 = random.nextInt(4);
            if (pentRand2 == 0) {   // lewo
                check6 = false;
                check3 = false;
                check5 = false;
            }
            if (pentRand2 == 1) {   // prawo
                check4 = false;
                check2 = false;
                check7 = false;
            }
            if (pentRand2 == 2) {    // góra
                check4 = false;
                check1 = false;
                check6 = false;
            }
            if (pentRand2 == 3) {    // dół
                check7 = false;
                check0 = false;
                check5 = false;
            }
        }


        if (check0) {
            if (cells[s0.x][s0.y].isStan() && cells[s0.x][s0.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s0.x][s0.y].color;
                ccn.nr = 0;
                cN.add(ccn);
            }
        }
        if (check1) {
            if (cells[s1.x][s1.y].isStan() && cells[s1.x][s1.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s1.x][s1.y].color;
                ccn.nr = 1;
                cN.add(ccn);
            }
        }
        if (check2) {
            if (cells[s2.x][s2.y].isStan() && cells[s2.x][s2.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s2.x][s2.y].color;
                ccn.nr = 2;
                cN.add(ccn);
            }
        }
        if (check3) {
            if (cells[s3.x][s3.y].isStan() && cells[s3.x][s3.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s3.x][s3.y].color;
                ccn.nr = 3;
                cN.add(ccn);
            }
        }
        if (check4) {
            if (cells[s4.x][s4.y].isStan() && cells[s4.x][s4.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s4.x][s4.y].color;
                ccn.nr = 4;
                cN.add(ccn);
            }
        }
        if (check5) {
            if (cells[s5.x][s5.y].isStan() && cells[s5.x][s5.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s5.x][s5.y].color;
                ccn.nr = 5;
                cN.add(ccn);
            }
        }
        if (check6) {
            if (cells[s6.x][s6.y].isStan() && cells[s6.x][s6.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s6.x][s6.y].color;
                ccn.nr = 6;
                cN.add(ccn);
            }
        }
        if (check7) {
            if (cells[s7.x][s7.y].isStan() && cells[s7.x][s7.y].color != Color.WHITE) {
                ColorMultiplicyty ccn = new ColorMultiplicyty();
                ccn.color = cells[s7.x][s7.y].color;
                ccn.nr = 7;
                cN.add(ccn);
            }
        }

        if (cN.size() > 0) {
            for (int l = 0; l < cN.size(); l++) {
                for (int h = 0; h < cN.size(); h++) {
                    if (l != h) {
                        if (cN.get(l).color.equals(cN.get(h).color)) {
                            cN.get(l).liczba = (cN.get(l).liczba + 1);
                        }
                    }
                }
            }

            int index = 0;
            int tmp = -1;
            for (int l = 0; l < cN.size(); l++) {
                if (tmp < cN.get(l).liczba) {
                    tmp = cN.get(l).liczba;
                    index = l;
                }
            }
            ColorMultiplicyty cn1 = cN.get(index);
            if (cN.size() > 1) {
                cN.remove(index);

                int index2 = 0;
                tmp = -1;
                for (int l = 0; l < cN.size(); l++) {
                    if (tmp < cN.get(l).liczba) {
                        tmp = cN.get(l).liczba;
                        index2 = l;
                    }
                }
                ColorMultiplicyty cn2 = cN.get(index2);
                if (cn1.liczba == cn2.liczba) {
                    Random random = new Random();
                    int choose = random.nextInt(2);
                    if (choose == 0) {
                        znaczKomorkeNaPozycji(positionXY, cn1.color);
                    } else {
                        znaczKomorkeNaPozycji(positionXY, cn2.color);
                    }
                }
            } else {
                znaczKomorkeNaPozycji(positionXY, cn1.color);
            }
        }
    }

    public void znaczKomorkeNaPozycji(PositionXY positionXY, Color color) {
        boolean zamaluj = false;
        int index = -1;
        for (int d = 0; d < positionXYList.size(); d++) {
            if (positionXYList.get(d).x == positionXY.x && positionXYList.get(d).y == positionXY.y) {
                zamaluj = true;
                index = d;
                break;
            }
        }
        if (zamaluj && !cells_tmp[positionXY.x][positionXY.y].isStan() && !cells[positionXY.x][positionXY.y].isStan()) {
            cells_tmp[positionXY.x][positionXY.y].color = (color);
            cells_tmp[positionXY.x][positionXY.y].stan = (true);
            positionXYList.remove(index);
           // System.out.println(positionXYList.size());
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas_id.getGraphicsContext2D();
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
        colorsList = new ArrayList<>();
        colorsList.add(Color.WHITE);        // dodanie białego, żeby było widać (wykluczenie go z puli kolorów)
        positionXYList = new ArrayList<>(); // żeby nie postawić dwa razy w tym samym miejscu na planszy

        // ustawienie rozwijalnych opcji
        comboNgbh_id.setItems(optionsMethods);          // metod sąsiedztwa
        comboSowingGrains_id.setItems(optionsTypes);    // typów zasiewu nasion

        // ustawienie początkowych wartości w rozwijalnym menu
        comboSowingGrains_id.getSelectionModel().selectFirst();
        // comboSowingGrains_id.getSelectionModel().selectNext();
        comboNgbh_id.getSelectionModel().selectFirst();

        // wywołanie funkcji, która reprezentuje opcje z przycisku myszy
        mouseZasiew();

        // przycisk rysujący siatkę
        drawBtton_id.setOnAction(event -> {
            pobierzRozmiarSiatki();
            cells = new Cell[rozmiar][rozmiar];
            cells_tmp = new Cell[rozmiar][rozmiar];
            PositionXY position;
            for (int i = 0; i < rozmiar; i++) {
                for (int j = 0; j < rozmiar; j++) {
                    cells[i][j] = new Cell();
                    cells_tmp[i][j] = new Cell();
                    position = new PositionXY(i, j);
                    positionXYList.add(position);
                }
            }
            wyczyscCanvas();          // wyczyszczenie planszy
            rysujSiatke();            // narysowanie siatki
        });

        // przycisk tworzący ziarna
        grainMakerButton_id.setOnAction(event -> {
            pobierzTypZasiewu();
            if (typZasiewu.equals("Randomowe")) {
                randomZasiew();
                wypelnijKomorki();
            } else if (typZasiewu.equals("Równomierne")) {
                rownomierneZasiew();
                wypelnijKomorki();
            }
        });

        // przycisk uruchamiający
        startButton_id.setOnAction(event -> {
            pobierzWszystkieDane();
            UruchomienieMetod uruchomienieMetod = new UruchomienieMetod();
            uruchomienieMetod.start();
        });
    }
}
