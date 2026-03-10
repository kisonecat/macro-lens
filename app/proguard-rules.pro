# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.phood.**$$serializer { *; }
-keepclassmembers class com.phood.** {
    *** Companion;
}
-keepclasseswithmembers class com.phood.** {
    kotlinx.serialization.KSerializer serializer(...);
}
