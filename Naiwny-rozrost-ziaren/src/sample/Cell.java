package sample;

import javafx.scene.paint.Color;

// klasa odpowiadająca za pojedynczą komórkę siatki
public class Cell {
    public Color color;         // kolor komórki
    public boolean stan;        // stan komórki, czy jest "pusta", czy "pełna"

    public Cell() { this.color = Color.WHITE; this.stan = false; }

    public Color getColor() {
        return color;
    }
    public boolean isStan() {
        return stan;
    }
}