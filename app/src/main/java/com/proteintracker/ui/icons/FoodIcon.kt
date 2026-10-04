package com.proteintracker.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BakeryDining
import androidx.compose.material.icons.filled.BreakfastDining
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.KebabDining
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.OutdoorGrill
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The preloaded icon picker. Only [key] is persisted, so the list can be
 * reordered or extended later without touching stored data.
 */
enum class FoodIcon(val key: String, val label: String, val imageVector: ImageVector) {
    PASTA("pasta", "Pasta", Icons.Filled.DinnerDining),
    LOCAL_DINING("local_dining", "Plato", Icons.Filled.LocalDining),
    LUNCH("lunch", "Almuerzo", Icons.Filled.LunchDining),
    DINNER("dinner", "Cena", Icons.Filled.DinnerDining),
    BREAKFAST("breakfast", "Desayuno", Icons.Filled.BreakfastDining),
    FASTFOOD("fastfood", "Comida rápida", Icons.Filled.Fastfood),
    PIZZA("pizza", "Pizza", Icons.Filled.LocalPizza),
    RAMEN("ramen", "Ramen", Icons.Filled.RamenDining),
    MEAL("meal", "Plato completo", Icons.Filled.SetMeal),
    KEBAB("kebab", "Kebab", Icons.Filled.KebabDining),
    GRILL("grill", "Barbacoa", Icons.Filled.OutdoorGrill),
    SOUP("soup", "Sopa", Icons.Filled.SoupKitchen),
    RICE("rice", "Arroz", Icons.Filled.RiceBowl),
    BAKERY("bakery", "Panadería", Icons.Filled.BakeryDining),
    CAKE("cake", "Pastel", Icons.Filled.Cake),
    ICE_CREAM("ice_cream", "Helado", Icons.Filled.Icecream),
    COOKIE("cookie", "Galleta", Icons.Filled.Cookie),
    EGG("egg", "Huevo", Icons.Filled.Egg),
    COFFEE("coffee", "Café", Icons.Filled.LocalCafe),
    DRINK("drink", "Bebida", Icons.Filled.LocalDrink),
    VEGGIE("veggie", "Vegetal", Icons.Filled.Eco),
    FLATWARE("flatware", "Cubierto", Icons.Filled.Flatware),
    MENU("menu", "Menú", Icons.Filled.RestaurantMenu),
    ;

    companion object {
        val default: FoodIcon = MENU

        private val byKey: Map<String, FoodIcon> = entries.associateBy { it.key }

        fun byKey(key: String): FoodIcon = byKey[key] ?: default
    }
}
