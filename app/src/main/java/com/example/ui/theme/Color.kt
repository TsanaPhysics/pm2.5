package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Bento Grid Theme Primary & Brand Colors
val BentoPrimary = Color(0xFF2563EB)        // Tailwind blue-600
val BentoPrimaryDark = Color(0xFF1D4ED8)    // Tailwind blue-700
val BentoPrimaryLight = Color(0xFFDBEAFE)   // Tailwind blue-100
val BentoHeroBg = Color(0xFF0F172A)         // Tailwind slate-900
val BentoSurfaceDark = Color(0xFF1E1E1E)    // Matte dark bento tile
val BentoBg = Color(0xFFF3F4F9)             // Cool slate-lavender bento canvas
val BentoSurface = Color(0xFFFFFFFF)        // Crisp white card
val BentoSurfaceVariant = Color(0xFFF8FAFC) // Slate-50
val BentoBorder = Color(0xFFE2E8F0)         // Slate-200 border
val BentoBorderLight = Color(0xFFF1F5F9)    // Slate-100 subtle border

// Typography Slate Scale
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate500 = Color(0xFF64748B)
val Slate400 = Color(0xFF94A3B8)
val Slate300 = Color(0xFFCBD5E1)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)

// Legacy compatibility aliases for existing references
val AeroPrimary = BentoPrimary
val AeroPrimaryVariant = BentoPrimaryDark
val AeroSecondary = Color(0xFF0284C7)
val AeroTertiary = Color(0xFF4F46E5)

// Air Quality Standards (Thai PCD & US AQI)
val AqiVeryGood = Color(0xFF0284C7)   // 0 - 15 µg/m³ (Blue)
val AqiGood = Color(0xFF10B981)       // 15.1 - 25 µg/m³ (Green)
val AqiModerate = Color(0xFFF59E0B)   // 25.1 - 37.5 µg/m³ (Yellow)
val AqiUnhealthySensitive = Color(0xFFEA580C) // 37.6 - 75 µg/m³ (Orange - Thai Alert)
val AqiUnhealthy = Color(0xFFDC2626)  // 75.1 - 150 µg/m³ (Red)
val AqiHazardous = Color(0xFF7C3AED)  // > 150 µg/m³ (Purple)

// Dark Theme Surfaces
val DarkBg = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkBorder = Color(0xFF475569)

// Light Theme Surfaces
val LightBg = BentoBg
val LightSurface = BentoSurface
val LightSurfaceVariant = BentoSurfaceVariant
val LightBorder = BentoBorder

// Accents
val NeonCyan = Color(0xFF38BDF8)
val WarningAmber = Color(0xFFF59E0B)


