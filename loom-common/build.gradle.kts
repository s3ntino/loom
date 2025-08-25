plugins {
    `loom-java`
    `loom-publish`
    `loom-repositories`
}

dependencies {
    api(libs.bundles.scylla.driver)
}

loomPublish {
    artifactId = "loom"
}