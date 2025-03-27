# Test IT Management plugin

<!-- Plugin description -->
The **Test IT Management plugin** is a powerful tool for managing work items. It provides an ability to browse the
project's work items and hierarchies, generate unit tests for selected work item, locate non-automated work items.
<!-- Plugin description end -->
> **Note**
>
> Click the <kbd>Watch</kbd> button on the top to be notified about releases containing new features and fixes.


### We added PyCharm and pytest support recently (1.0.12)! 

## Compatibility

| Test IT | Plugin Test IT Management |
|---------|---------------------------|
| 4.6     | v1.0.1                    | 
| 5.0     | v1.0.7+                   |
| 5.1     | v1.0.12.1                 |
| 5.2     | v1.0.13                   |
| 5.2.2   | 1.1.0-TMS-5.2.2           |
| Cloud   | 1.2.0-TMS-CLOUD           |

## Download

You can download the latest version of the Test IT Management plugin from
the [releases](https://github.com/testit-tms/testit-management/releases) page.

## Setup

1. Configure connection in the `File -> Settings -> Tools -> Test IT` menu.
2. Test connection using `Verify Setup` button.
3. Apply settings.
4. Go to `View -> Tool Windows -> Test IT`.
5. Sync your project if needed.

```json
{
  "Url": "https://team-okp8.testit.software/",
  "Project Id": "3a651dfc-51a9-49aa-a8ee-0e50a6efcf36",
  "Private Token": "b241M2s1N1VrRUhwYTNLaWZP"
}
```

## Features

### Automation type

Non-automated work items are blue colored in tree.

### Code snippet

Copy work item's code snippet using context menu, then paste it from clipboard in java file.

### Go to sources

Double-click on the tree item opens an editor with the autotest's first line focused (if exists).

## Contributing

You can help to develop the project. Any contributions are **greatly appreciated**.

* If you have suggestions for adding or removing features, feel free
  to [open an issue](https://github.com/testit-tms/testit-management/issues/new) to discuss it, or create a direct pull
  request.
* Make sure to check your spelling and grammar.
* Create individual PR for each suggestion.
* Read the [Code Of Conduct](https://github.com/testit-tms/testit-management/blob/main/CODE_OF_CONDUCT.md) before
  posting your first idea as well.

## License

Distributed under the Apache-2.0 License.
See [LICENSE](https://github.com/testit-tms/testit-management/blob/main/LICENSE.txt) for more information.
