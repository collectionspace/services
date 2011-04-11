This lib directory contains binaries required to run the common layer
in JBoss container. Corresponding binaries (if present) in JBoss cspace domain are
either upgraded/replaced with a one-time execution task.

For jpa upgrade in mercury 0.4, the task is 'ant jpa' executed at
service/common level. From 0.5 the following tasks are added.

ant deploy_jpa (replaces jpa task, deploys jpa jars to JBoss cspace domain)
ant dist_jpa (copies required jpa jars to dist)
ant deploy_spring (deploys spring framework and spring security jars to JBoss cspace domain)
ant dist_spring (copies required spring framework and security jars to dist)
