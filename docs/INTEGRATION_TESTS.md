# Integration tests

Integration tests are intentionally part of `main/integration`, so they can run inside the Forge/GTCEu runtime and exercise the real dynamic pack lifecycle.

Enable them in the generated Forge common config:

```properties
[integration_tests]
enabled=true
```

The config file is normally:

```text
config/gtmqol-common.toml
```

With the option enabled, the mod registers two visible examples:

- `gtmqol:runtime_single_block`, a registered single-block GTCEu machine;
- `gtmqol:runtime_multiblock`, a registered controller with a small stone structure pattern.

During client setup, GTCEu's model and blockstate builders generate the controller and item assets into `GTDynamicResourcePack`. The examples reuse GTCEu's existing machine model and texture assets; no test JSON is hand-written and no Gradle data-generation output is required. Place the items in a test world and assemble the multiblock to inspect the result.

To obtain the examples in a test world:

```text
/give @p gtmqol:runtime_single_block
/give @p gtmqol:runtime_multiblock
```

## Why there is no late Mixin loader yet

The first tests target public lifecycle hooks and GTCEu's dynamic pack APIs. A late Mixin loader would add another class-loading and compatibility failure point without testing more of the current feature.

If a later test must observe a private GTCEu method, add that Mixin only behind a separate test config option. It should be loaded conditionally and its test must fail loudly when the target method is absent; it must not be part of the normal runtime path.
