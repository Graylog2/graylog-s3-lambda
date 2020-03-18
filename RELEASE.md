# Release instructions for Graylog S3 Lambda  

## Overview

The Graylog S3 Lambda project is currently a standalone project outside of the normal Graylog build ecosystem. 
Therefore, it must be built and released on it's own.

## Release Steps

1) Update the [version element](https://github.com/Graylog2/graylog-s3-lambda/blob/8c5fecaf667bf5f44f2de43f981e93681b8fa97a/pom.xml#L6) 
   in the POM file to the version number to be released. For example, if currently “1.0.1-SNAPSHOT”,
   change the version number to "1.0.1", so that the correct version number is included in the JAR file name.
 
2) Commit the change to the version (usually with message "Prepare for release 1.0.1"). Now add a tag for that version (`git tag 1.0.1`). 
   Push the commit and the tag (`git push && git push --tags`).

3) Run `mvn package` to build the JAR file.

4) Manually create a new zip archive with the JAR file and the contents of the `/content-packs` directory. We always 
   ship with the latest content packs, so that any existing integrations that rely on them can be used (for example Cloudflare). 
   Follow the same format as the existing [1.0.0 release](https://github.com/Graylog2/graylog-s3-lambda/releases). 

5) Manually upload a new release to the releases page for the correct new tag (eg. 1.0.1).

6) Update the POM version to the next development version (eg 1.0.2-SNAPSHOT). Commit with message "Prepare for next development iteration"
   or similar. Push the commit. Similar to [this](https://github.com/Graylog2/graylog-s3-lambda/commit/092e62d43af23fef800574b679498a5a14eea61f) example commit.