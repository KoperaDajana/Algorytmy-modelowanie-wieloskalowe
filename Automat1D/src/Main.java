// Modelowanie wieloskalowe/Laboratoria/Automaty komorkowe 1D
// implementacja na podstawie wykładu 6 (http://home.agh.edu.pl/~lmadej/wp-content/uploads/wyklad_5b.pdf)
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        int wybor = 1;
        while (wybor != 0){
            // 1) wprowadzenie numeru reguły przez użytkownika
            int nrReguly;
            Scanner scanner = new Scanner(System.in);
            System.out.println("Podaj nr reguły: ");

            nrReguly = scanner.nextInt();
            System.out.println("Reguła " + nrReguly + "\n");

            // 2) zapis podanej liczby do postaci binarnej
            System.out.print("Po przejsciu na system binarny liczba: " + nrReguly + " = ");
            int[] tablicaBinarnej = new int[8];     // stworzenie tablicy, w której będzie przechowywana binarna
            int[] pomocniczaBinarnej = new int[32];
            int k = 7;
            int z = 0;
            for (byte wartosc = Integer.SIZE - 1; wartosc >= 0; --wartosc)
                pomocniczaBinarnej[z++] = Integer.parseInt(String.valueOf(Character.forDigit((nrReguly >>> wartosc) & 1, 2)));
            int dlugoscTablicy = pomocniczaBinarnej.length;

            for (int i = 0; i < 8; i++) {
                tablicaBinarnej[k--] = pomocniczaBinarnej[--dlugoscTablicy];
            }
            // wypisuje liczbę w postci binarnej, w odpowiednie kolejne miejsca w tablicy
            for (int i = 0; i < tablicaBinarnej.length; i++) {
                System.out.print(tablicaBinarnej[i]);
            }

            // 3) użycie funkcji, która sprawdza warunki odpowiednio dla każdej z liczby binarnej zamieszczonej w tablicy
            automat1D(tablicaBinarnej);

            // sprawdzenie warunku dotyczącego pętli
            if(nrReguly == 0) {
                wybor = 0;
                System.out.println("Wybrano 0, koniec programu");
            }
        }
    }


    public static void automat1D(int tab[]) {
    // automat składający się z 31 miejsc w tablicy, początkowe wypełnianie, gdzie wartość środkowa
    // dla 1 wiersza jest równa 1, pozostałe to 0 (liczba iteracji podana jako drugi rozmiar tablicy = 31)
        int[][] tabAutomat1D = new int[30][31];
        for (int j = 0; j < 30; j++) {
            for (int i = 0; i < 30; i++) {
                // wypełnienie macierzy zerami
                tabAutomat1D[j][i] = 0;
                // ale dla środkowej wartosci 1 wiersza, wartosc = 1
                if (j == 0 && i == 15) { tabAutomat1D[j][i] = 1; }
            }
        }

        // ustawienie sąsiadów na początku na 0
        int lewy = 0;
        int prawy = 0;

            // zapisanie argumentu funkcji do tablicy wynikowej (arguemntem jest tablica liczby po przejsciu na binarną
            int[] wynik = tab;

            for (int j = 1; j < 30; j++) {
                for (int i = 0; i < 31; i++) {
                    // sąsiedzi na brzegach tablicy (przejście na poprzedni wiersz)
                    if ((i == 0) || i == 30) {
                        if (i == 0) { lewy =  tabAutomat1D[j - 1][29]; }
                        if (i == 29) { prawy = tabAutomat1D[j - 1][0]; }
                    }
                    // jeśli sąsiedzi nie są skrajni
                    if (i != 0 && i != 30) {
                        lewy = tabAutomat1D[j - 1][i - 1];
                        prawy = tabAutomat1D[j - 1][i + 1];
                    }

                    // wprowadzenie warunków dla reguł
                    if (lewy == 1 && tabAutomat1D[j - 1][i] == 1 && prawy == 1) { // 1 1 1
                        tabAutomat1D[j][i] = wynik[0];
                    }
                    if (lewy == 1 && tabAutomat1D[j - 1][i] == 1 && prawy == 0) { // 1 1 0
                        tabAutomat1D[j][i] = wynik[1];
                    }
                    if (lewy == 1 && tabAutomat1D[j - 1][i] == 0 && prawy == 1) { // 1 0 1
                        tabAutomat1D[j][i] = wynik[2];
                    }
                    if (lewy == 1 && tabAutomat1D[j - 1][i] == 0 && prawy == 0) { // 1 0 0
                        tabAutomat1D[j][i] = wynik[3];
                    }
                    if (lewy == 0 && tabAutomat1D[j - 1][i] == 1 && prawy == 1) { // 0 1 1
                        tabAutomat1D[j][i] = wynik[4];
                    }
                    if (lewy == 0 && tabAutomat1D[j - 1][i] == 1 && prawy == 0) { // 0 1 0
                        tabAutomat1D[j][i] = wynik[5];
                    }
                    if (lewy == 0 && tabAutomat1D[j - 1][i] == 0 && prawy == 1) { // 0 0 1
                        tabAutomat1D[j][i] = wynik[6];
                    }
                    if (lewy == 0 && tabAutomat1D[j - 1][i] == 0 && prawy == 0) { // 0 0 0
                        tabAutomat1D[j][i] = wynik[7];
                    }
                }
            }

            // wypisanie wyniku
            System.out.println();
            for (int j = 0; j < 14; j++) {
                for (int i = 0; i < 31; i++) {
                    //System.out.print(tabAutomat1D[j][i] + "  ");
                    if (tabAutomat1D[j][i] == 1) {
                        System.out.print("●");
                    } else { System.out.print("◌");}
                }
                System.out.println();
            }
            System.out.println();
        }
}

