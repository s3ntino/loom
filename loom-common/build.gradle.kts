plugins {
    `loom-java`
    `loom-publish`
    `loom-repositories`
    `loom-unit`
}

dependencies {
    api(libs.bundles.scylla.driver)
}

loomPublish {
    artifactId = "loom-common"
}