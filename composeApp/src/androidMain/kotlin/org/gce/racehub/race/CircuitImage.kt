package org.gce.racehub.race

import androidx.annotation.DrawableRes
import org.gce.racehub.R

@DrawableRes
fun circuitDrawable(circuitName: String): Int? {
    val key = circuitName.lowercase()
    return when {
        "bahrain"    in key || "sakhir"    in key -> R.drawable.circuit_bahrain
        "jeddah"     in key || "corniche"  in key -> R.drawable.circuit_jeddah
        "albert"     in key || "melbourne" in key -> R.drawable.circuit_australia
        "suzuka"     in key || "japan"     in key -> R.drawable.circuit_japan
        "shanghai"   in key || "china"     in key -> R.drawable.circuit_china
        "miami"      in key                       -> R.drawable.circuit_miami
        "imola"      in key || "ferrari"   in key -> R.drawable.circuit_imola
        "monaco"     in key                       -> R.drawable.circuit_monaco
        "barcelona"  in key || "catalunya" in key -> R.drawable.circuit_spain
        "villeneuve" in key || "canada"    in key -> R.drawable.circuit_canada
        "red bull"   in key || "austria"   in key || "spielberg" in key -> R.drawable.circuit_austria
        "silverstone" in key || "britain"  in key -> R.drawable.circuit_silverstone
        "hungaroring" in key || "hungary"  in key -> R.drawable.circuit_hungary
        "spa"        in key || "belgium"   in key -> R.drawable.circuit_belgium
        "zandvoort"  in key || "netherlands" in key -> R.drawable.circuit_netherlands
        "monza"      in key || "italy"     in key -> R.drawable.circuit_italy
        "baku"       in key || "azerbaijan" in key -> R.drawable.circuit_azerbaijan
        "marina bay" in key || "singapore" in key -> R.drawable.circuit_singapore
        "americas"   in key || "cota"      in key || "austin" in key -> R.drawable.circuit_usa
        "hermanos"   in key || "mexico"    in key -> R.drawable.circuit_mexico
        "interlagos" in key || "brazil"    in key || "pace" in key -> R.drawable.circuit_brazil
        "las vegas"  in key || "strip"     in key -> R.drawable.circuit_las_vegas
        "losail"     in key || "lusail"    in key || "qatar"     in key -> R.drawable.circuit_qatar
        "yas"        in key || "abu dhabi" in key -> R.drawable.circuit_abu_dhabi
        "madrid"     in key                       -> R.drawable.circuit_madrid
        else -> null
    }
}
