package de.bananaco.magnaipsum;

import java.util.Comparator;

public class aObjComparator implements Comparator<aObj> {

    public int compare(aObj o1, aObj o2) {
        return -o1.getVal().compareTo(o2.getVal());
    }
    
}
