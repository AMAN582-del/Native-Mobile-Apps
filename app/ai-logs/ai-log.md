# AI log (Claude)

## Error 1: wrong import in generated code
- AI wrote `import androidx.compose.foundation.layout.Modifier` in MainActivity.kt.
- Result: "Unresolved reference 'Modifier'" build error.
- Fix: the correct import is `androidx.compose.ui.Modifier`.

## Error 2: missing dependency
- AI used `Icons.Default.Home` for the bottom bar but did not tell me to add `material-icons-core` to build.gradle.kts.
- Result: "Unresolved reference 'Icons'" (7 errors).
- Fix: added `implementation("androidx.compose.material:material-icons-core")` and synced Gradle.

## Error 3: build setup incompatible with AGP 9
- AI's Room + KSP setup failed with "Using kotlin.sourceSets DSL ... is not allowed with built-in Kotlin".
- Fix: added `android.disallowKotlinSourceSets=false` to gradle.properties after checking the error text.

## Error 4: steps given out of order
- AI sent the navigation code before the History files existed, causing red errors in MainActivity.
- Fix: created the History files first, then synced Gradle.