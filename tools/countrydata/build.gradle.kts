plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.project.livechat.tools.countrydata.CountryDataGeneratorKt")
}

dependencies {
    implementation(libs.libphonenumber)
}

kotlin {
    jvmToolchain(17)
}
