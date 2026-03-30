# Add project specific ProGuard rules here.
-keep class com.google.ai.client.generativeai.** { *; }
-keep class com.tripgenie.data.models.** { *; }
-keepattributes *Annotation*
-dontwarn okhttp3.**
