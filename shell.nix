{ pkgs ? import <nixpkgs> {
    config.android_sdk.accept_license = true;
    config.allowUnfree = true;
  }
}:

let
  androidComposition = pkgs.androidenv.composeAndroidPackages {
    buildToolsVersions = [ "34.0.0" ];
    platformVersions = [ "35" ];
    includeEmulator = false;
    includeNDK = false;
    includeSources = false;
    includeSystemImages = false;
  };
  androidSdk = androidComposition.androidsdk;
in
pkgs.mkShell {
  packages = with pkgs; [
    jdk17
  ];
  buildInputs = [
    androidSdk
    pkgs.glibc
  ];
  ANDROID_HOME = "${androidSdk}/libexec/android-sdk";
  ANDROID_SDK_ROOT = "${androidSdk}/libexec/android-sdk";
  # Point gradle at the nix-provided aapt2 rather than trying to download its own
  GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${androidSdk}/libexec/android-sdk/build-tools/34.0.0/aapt2";
  shellHook = ''
    export PATH="${androidSdk}/libexec/android-sdk/platform-tools:$PATH"
    echo "sdk.dir=${androidSdk}/libexec/android-sdk" > local.properties
  '';
}
