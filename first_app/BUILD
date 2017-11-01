android_library(
  name = "activities",
  srcs = glob(["src/main/java/com/example/firstandroidapp/*.java"]),
  custom_package = "com.example.firstandroidapp",
  manifest = "src/main/AndroidManifest.xml",
  resource_files = glob(["src/main/res/**"]),
)

android_binary(
    name = "firstandroidapp",
    custom_package = "com.example.firstandroidapp",
    manifest = "src/main/AndroidManifest.xml",
    resource_files = glob(["src/main/res/**"]),
    deps = [":activities"],
)
