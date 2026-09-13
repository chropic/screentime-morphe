# Runtime source boundary

`dev.screentime.runtime` is the injected runtime package. The template package task currently compiles its Java source from `extensions/extension/src/main/java` and emits `extensions/extension.mpe`; that task is intentionally retained to preserve upstream bundle machinery.

This directory is the architectural home for runtime design and future source-set extraction. Do not create a second packaged runtime until the template's extension task has been verified with the new source path.
