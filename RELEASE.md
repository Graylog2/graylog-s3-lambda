# Release instructions for Graylog S3 Lambda  

## Overview

The Graylog S3 Lambda project is a standalone project outside of the normal Graylog build ecosystem. 
Therefore, it must be built and released on its own.

#### A Note on Versioning
We follow the standard [Semantic Versioning](https://semver.org/) *Major.Minor.Patch* pattern for version numbers where:
* *Patch* is incremented for backwards compatible bug fixes
* *Minor* is incremented for backwards compatible functionality
* *Major* is incremented for incompatible API changes (e.g., adding a new required configuration value)

## Release Steps
All steps below should be performed on the `master` branch.

1) Prepare the package for release.
    1) Update the [version element](https://github.com/Graylog2/graylog-s3-lambda/blob/8c5fecaf667bf5f44f2de43f981e93681b8fa97a/pom.xml#L6) 
    in the POM file to the release version number (e.g., change from “1.0.2-SNAPSHOT” to "1.0.2"). This ensures the correct 
    version number is included in the JAR file name
    1) Commit the version change with message "Prepare for release 1.0.2" (or similar)
    1) Add a tag for the new version: `git tag 1.0.2` 
    1) Push the commit and the tag: `git push && git push --tags`

1) Create the artifacts for release
    1) Run `mvn clean package` to build the JAR file.
    1) Create a new zip archive with the JAR file and the contents of the `/content-packs` directory. We always ship 
    with the latest content packs, so that any existing integrations that rely on them can be used (for example Cloudflare). 
        1) Ensure you are working in the `graylog-s3-project` directory
        1) `zip -r graylog-s3-lambda-1.0.2.zip content-packs/*` 
        1) `zip -g -j graylog-s3-lambda-1.0.2.zip target/graylog-s3-lambda-1.0.2.jar`

1) Create the new release in GitHub.
    1) Open the [Releases](https://github.com/graylog2/graylog-s3-lambda/releases) page in GitHub and click **Draft a new release**
    1) Select the appropriate tag for the release (the tag you created and pushed above)
    1) Add the release **Title** and **Description**
        1) **Title** should be short and descriptive
        1) **Description** should provide detailed information about the contents of the release including a list of 
        changes with links to either PRs or Issues in GitHub
    1) Manually upload the artifacts (.zip file) you created
    1) Publish the release

1) Prepare the package for development
    1) Update the POM version to the next development version (eg 1.0.3-SNAPSHOT)
    1) Commit with message "Prepare for next development iteration" or similar
    1) Push the commit. Similar to [this](https://github.com/Graylog2/graylog-s3-lambda/commit/092e62d43af23fef800574b679498a5a14eea61f) example commit