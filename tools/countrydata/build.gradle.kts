plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("com.edufelip.livechat.tools.countrydata.CountryDataGeneratorKt")
}

dependencies {
    implementation(libs.libphonenumber)
}

kotlin {
    jvmToolchain(17)
}
