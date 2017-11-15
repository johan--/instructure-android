# Builds

## Overview

Android builds run in [Docker containers](https://github.com/bitrise-docker/android)
maintained by Bitrise. Image updates are published weekly. Builds have a maximum run time of 45m.
There's no limit to the total number of builds, although only a fixed amount run concurrently.

### Android parent testing local

Note the build variant in Android Studio must be set to qaDebug.

![](img/qa_debug.png)

qaDebug is used to set the `IS_TESTING` build config field. This impacts app reset and other testing related features.
If qaDebug isn't used then `IS_TESTING` will be false and the tests will fail.

The app and test apk may be built for coverage by passing the `-Pcoverage` flag.
Code coverage defaults to off to improve build times.

> ./gradlew -Pcoverage :app:assembleQaDebug :app:assembleQaDebugAndroidTest

Running the tests locally with `connectedQaDebugAndroidTest`

> ./gradlew :app:connectedQaDebugAndroidTest

Creating a coverage report will run the tests before generating the HTML.

> ./gradlew -Pcoverage :app:createQaDebugCoverageReport

The report is saved in `parent/app/build/reports/coverage/qa/debug/index.html`

### Android parent testing cloud

[Firebase test lab](https://firebase.google.com/docs/test-lab/#implementation_path)
is able to run the tests from Android Studio and the command line. On Bitrise,
the gcloud CLI is used to trigger builds.

```
gcloud beta test android run \
--type instrumentation \
--app "$APP_APK" \
--test "$TEST_APK" \
--results-bucket android-parent \
--device-ids Nexus9 \
--os-version-ids 25 \
--locales en \
--orientations portrait \
--timeout 25m \
--environment-variables coverage=true,coverageFile=/sdcard/coverage.ec \
--directories-to-pull=/sdcard
```

The [gcloud docs](https://cloud.google.com/sdk/gcloud/reference/beta/test/android/run)
explain all the parameters. For non-coverage runs, `--environment-variables` and `--directories-to-pull`
can be omitted.

#### Android parent testing bitrise

The [android-parent-espresso](https://www.bitrise.io/app/2724be0b444b271e/workflow/editor)
job on Bitrise is responsible for running the android parent espresso tests.

ENV | Description
--- | ---
GCLOUD_USER    | Google cloud user
GCLOUD_PROJECT | Google cloud project name
GCLOUD_KEY     | Google cloud key base64 encoded (`base64 gcloudkey.json | pbcopy`).
APP_APK        | Test apk (`build/outputs/apk/app-qa-debug.apk`)
TEST_APK       | App apk (`build/outputs/apk/app-qa-debug-androidTest.apk`)

`gcloud.rb` in `fastlane/utils` is used to install and run the gcloud SDK.
The `parent_espresso_gcloud` lane is used to run gcloud on Bitrise.

Firebase test lab runs the Espresso tests. The `coverage.ec` file is downloaded
from Google cloud storage to Bitrise via `gsutil`.

```gradle
// ./gradlew -Pcoverage firebaseJacoco
task firebaseJacoco(type: JacocoReport) {
    group = "Reporting"
    description = "Generate Jacoco coverage reports for Firebase test lab."

    def flavor = 'qa'

    classDirectories = fileTree(
            dir: "${project.buildDir}/intermediates/classes/${flavor}",
            excludes: ['**/R.class',
                       '**/R$*.class',
                       '**/*$ViewInjector*.*',
                       '**/*$ViewBinder*.*',
                       '**/BuildConfig.*',
                       '**/Manifest*.*']
    )

    // project.buildDir is /android-uno/parent/app/build
    sourceDirectories = files(['src/main/java'].plus(android.sourceSets[flavor].java.srcDirs))
    executionData = files("${project.buildDir}/firebase.ec")

    reports {
        // default path: /android-uno/parent/app/build/reports/jacoco/firebaseJacoco/html/
        html.enabled true
        csv.enabled  false
        xml.enabled  false
    }
}
```

A custom gradle task `firebaseJacoco` generates the code coverage report using the Firebase
execution data. Finally Bitrise archives the report as a zip.

> /bitrise/src/parent/app/build/reports/jacoco/firebaseJacoco/html/
