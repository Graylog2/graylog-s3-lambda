package org.graylog.integrations.s3.config;

/**
 * Abstract class which has shared configuration methods.
 *
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/tutorial-env_cli.html">S3 Environment Variables</a>
 */
class AbstractConfiguration {

    static boolean readBoolean(String property, boolean defaultValue) {
        return System.getenv(property) != null ? Boolean.parseBoolean(System.getenv(property)) : defaultValue;
    }

    /**
     * @return Get the indicated string environment variable or return the default value if not present.
     */
    static String getStringEnvironmentVariable(String envVarName, String defaultValue) {
        return System.getenv(envVarName) != null && !System.getenv(envVarName).trim().isEmpty() ? System.getenv(envVarName) : defaultValue;
    }

    /**
     * Read the specified environment variable and attempt to convert it to an integer. Handle error condition.
     *
     * @param envVariableName The environment variable name to parse.
     * @return The parsed integer.
     */
    static Integer safeParseInteger(String envVariableName) {

        final String envValue = System.getenv(envVariableName);
        // Safely ignore blank values.
        if (envValue == null || envValue.equals(envVariableName)) {
            return null;
        }
        try {
            return Integer.valueOf(envValue);
        } catch (NumberFormatException e) {
            final String errorMessage = String.format("The specified value [%s] for field [%s] is not a valid integer.",
                                                      envValue, envVariableName);
            e.printStackTrace();
            throw new RuntimeException(errorMessage);
        }
    }
}
