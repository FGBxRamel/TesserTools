package de.bydora.tesserTools.enchantment.util;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public final class MaterialArrayMerger {

    /**
     * Kombiniert zwei Material-Arrays zu einem neuen Array.
     *
     * @param array1 Das erste Material-Array
     * @param array2 Das zweite Material-Array
     * @return Ein neues Array, das die Elemente beider Arrays enthält
     */
    public static Material @NotNull [] merge(Material[] array1, Material[] array2) {
        return Stream.concat(Arrays.stream(array1), Arrays.stream(array2))
                .toArray(Material[]::new);
    }

}
