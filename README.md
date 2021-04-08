# art-environment

ART Environment is an aggregation project for all art-* modules.

To use it do next: 

1. Clone project: `git clone https://github.com/art-community/art-environment`
2. Configure environment:
 * `cd art-environment`
 * `gradlew`
 * `gradlew prepareLocalEnvironment` - this will craete local Git ignored directory "local"
 * `gradlew configure` - this will clone all projects that are declared in local/build.gradle.kts
3. Check cloned projects Gradle configuration: 
* `cd local`
* `cd projects`
* `gradlew`
4. Open in youre IDE and import Gradle projects `art-environment` and `projects`
