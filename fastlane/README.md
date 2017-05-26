fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

## Choose your installation method:

<table width="100%" >
<tr>
<th width="33%"><a href="http://brew.sh">Homebrew</a></td>
<th width="33%">Installer Script</td>
<th width="33%">Rubygems</td>
</tr>
<tr>
<td width="33%" align="center">macOS</td>
<td width="33%" align="center">macOS</td>
<td width="33%" align="center">macOS or Linux with Ruby 2.0.0 or above</td>
</tr>
<tr>
<td width="33%"><code>brew cask install fastlane</code></td>
<td width="33%"><a href="https://download.fastlane.tools">Download the zip file</a>. Then double click on the <code>install</code> script (or run it in a terminal window).</td>
<td width="33%"><code>sudo gem install fastlane -NV</code></td>
</tr>
</table>

# Available Actions
## Android
### android build
```
fastlane android build
```
Build a specific app
### android rc
```
fastlane android rc
```
Build a release candidate and deploy to HockeyApp
### android deploy
```
fastlane android deploy
```
Build and deploy to the Play Store (alpha track)
### android build_all_apps
```
fastlane android build_all_apps
```
Builds all the apps
### android espresso
```
fastlane android espresso
```
Run parent Espresso tests. Example: fastlane espresso app:teacher run:2 video:true class:CoursesListPageTest method:displaysPageObjects
### android espresso_gcloud
```
fastlane android espresso_gcloud
```
Run Espresso tests on Google Cloud. Only works on Bitrise. fastlane espresso_gcloud app:teacher class:ProfilePageTest method:displaysPageObjects
### android parent_unit_gcloud
```
fastlane android parent_unit_gcloud
```
Run parent Unit tests on Google Cloud. Only works on Bitrise.
### android robo_gcloud
```
fastlane android robo_gcloud
```
Run Robo test on Google Cloud. Only works on Bitrise.
### android seed
```
fastlane android seed
```
Seed data into Canvas LMS for testing. Example: fastlane seed app:teacher class:CoursesListPageTest method:displaysPageObjects
### android unit_tests_jvm
```
fastlane android unit_tests_jvm
```


----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
