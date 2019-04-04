package sample;

import javafx.scene.paint.Color;

// klasa na rzecz określania kolorów w sąsiedztwie komórki (ile razy kolor się powtórzył)
public class ColorMultiplicyty {
    int liczba;         // ile razy kolor się powtórzył w sąsiedztwie
    int nr;             // nr sąsiada komórki
    Color color;        // kolor z wbudowanej klasy kolor

    public ColorMultiplicyty() {
        this.liczba = 0;
        this.nr = -1;               // ustawiona na sąsiada
        this.color = color;
    }
}
