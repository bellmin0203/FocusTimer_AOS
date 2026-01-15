# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Preserve the line number information for debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name.
-renamesourcefileattribute SourceFile

# ===============================
# Hilt / Dagger
# ===============================
-keepnames @dagger.hilt.android.lifecycle.HiltViewModel class * extends androidx.lifecycle.ViewModel

# ===============================
# Kotlin Serialization (if used)
# ===============================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# ===============================
# Vico Chart Library
# ===============================
#-keep class com.patrykandpatrick.vico.** { *; }
#-dontwarn com.patrykandpatrick.vico.**

# ===============================
# Application specific
# ===============================
#-keep class com.jm.harufocus.** { *; }
#-keep class com.jm.logutil.** { *; }
