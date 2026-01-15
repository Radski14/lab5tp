package com.example;

/**
 * Reprezentuje kamień używany w grze Go.
 * Określa kolor kamienia lub brak kamienia na polu.
 */
public enum Stone {

    /** Czarny kamień. */
    BLACK,

    /** Biały kamień. */
    WHITE,

    /** Puste pole na planszy. */
    EMPTY;

    /**
     * Zwraca kamień przeciwnika.
     * Dla EMPTY zwracany jest EMPTY.
     *
     * @return Przeciwny kolor kamienia lub EMPTY.
     */
    public Stone opposite() {
        return this == BLACK ? WHITE : BLACK;
    }
}
