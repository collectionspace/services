This lib directory contains binaries required to run the common layer
in JBoss container. Corresponding binaries (if present) in JBoss domain are
either upgraded/replaced with a one-time execution task.

For jpa upgrade in mercury 0.4, the task is 'ant jpa' executed at
service/common level.
