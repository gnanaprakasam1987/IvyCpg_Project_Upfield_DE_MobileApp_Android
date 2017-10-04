# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\vinodh.r\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
# -keep class !android.support.v7.internal.view.menu.**,** {*;}
-dontwarn
-ignorewarnings
# -dontshrink
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keep class **.R$*

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}


-dontwarn **CompatHoneycomb.**
-keep public class * extends android.support.v4.app.Fragment

-dontwarn **CompatHoneycomb.**
-keep class android.support.v4.** { *; }

-dontwarn android.support.**
-dontwarn com.ivy.lib.**

-dontwarn org.xmlpull.v1.**
-ignorewarnings

-keep class org.xmlpull.v1.** { *; }

-keep class android.support.v7.widget.SearchView { *; }

-dontwarn org.apache.poi.**
-dontwarn com.amazonaws.**
-dontwarn com.google.android.gms.**
-keep class android.support.design.widget.** { *; }
-keep interface android.support.design.widget.** { *; }
-dontwarn android.support.design.**

# Crashlytics rules

-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
# Following line should be commented for crashlytics to work
# -printmapping mapping.txt

