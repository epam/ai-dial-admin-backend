#!/bin/bash
set -Ee

# If the environment variable `USE_SYSTEM_CA_CERTS` is set, the Docker container will automatically import 
# mounted private or self-signed certificates during startup. 
# The value of this variable can be any character or word. For example: 1, yes, true, YES, TRUE.
# Private or self-signed certificates must be mounted into the container in the /certificates/ directory.
if [ -x /__cacert_entrypoint.sh ]; then
  (/__cacert_entrypoint.sh)
fi

# Check if DEBUG_OPTS is set, if not, set it to an empty string
DEBUG_OPTS=${DEBUG_OPTS:-}

# Check if JAVA_OPTS is set, if not, set it to an empty string
JAVA_OPTS=${JAVA_OPTS:-}

# Execute the Java application with the provided options.
# If arguments are passed, treat this as a CLI invocation: activate the cli profile and forward all args.
# Otherwise start the application normally as a web server.
if [ $# -gt 0 ]; then
    exec java $DEBUG_OPTS $JAVA_OPTS -jar app.jar --spring.profiles.active=cli "$@"
else
    exec java $DEBUG_OPTS $JAVA_OPTS -jar app.jar
fi
