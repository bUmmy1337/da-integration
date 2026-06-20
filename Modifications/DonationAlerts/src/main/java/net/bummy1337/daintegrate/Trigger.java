package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.sensitives.ISensitive;

import java.util.ArrayList;
import java.util.Iterator;

public class Trigger {
    private final ArrayList<ISensitive> triggers;

    public Trigger(Iterator<ISensitive> triggers) {
        this.triggers = new ArrayList<>();
        triggers.forEachRemaining(this.triggers::add);
    }
}
