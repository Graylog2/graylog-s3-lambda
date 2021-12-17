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

1) Create release artifact
    1) Run `mvn release:clean release:prepare`
    1) Answer the interactive questions about the release version and next development version.

1) Create the new release in GitHub.
    1) Open the [Releases](https://github.com/graylog2/graylog-s3-lambda/releases) page in GitHub and click **Draft a new release**
    1) Select the appropriate tag for the release (the tag that got created by the maven release plugin)
    1) Add the release **Title** and **Description**
        1) **Title** should be short and descriptive
        1) **Description** should provide detailed information about the contents of the release including a list of
           changes with links to either PRs or Issues in GitHub
    1) Manually upload the artifact you created (ZIP file from `target/artifacts/`)
    1) Publish the release
