/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

public enum LevelItem {
    OBSTACLE('#'), BASKET('b'), RANGER('r'), PLAYER('y'), EMPTY(' ');

    LevelItem(char rep) {
        representation = rep;
    }
    public final char representation;
}
