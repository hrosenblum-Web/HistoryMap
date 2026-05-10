# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**HistoryMap** is a Java application that converts CSV data about historical martial arts figures into interactive HTML relationship graphs using Mermaid.js diagrams. It generates a main visualization, individual per-person relationship pages, and template HTML for new entries.

## Build System

**Eclipse Java project** — no Maven or Gradle.

- Java 21 (configured in `.classpath` and `.settings/org.eclipse.jdt.core.prefs`)
- Source: `src/`, test source: `test/`, compiled output: `bin/`
- External JARs at hardcoded paths (update `.classpath` if different):
  - `C:/OpenCsv/opencsv-5.9.jar`
  - `C:/OpenCsv/commons-lang3-3.17.0/commons-lang3-3.17.0.jar`

**Compile sources:**
```
javac -cp "bin;C:/OpenCsv/opencsv-5.9.jar;C:/OpenCsv/commons-lang3-3.17.0/commons-lang3-3.17.0.jar" src/*.java -d bin
```

**Compile tests (after sources):**
```
javac -cp "bin;C:/OpenCsv/opencsv-5.9.jar;C:/OpenCsv/commons-lang3-3.17.0/commons-lang3-3.17.0.jar;C:/eclipse/plugins/junit-jupiter-api_5.14.2.jar" test/*.java -d bin
```

**Run all stages:**
```
java -cp "bin;C:/OpenCsv/opencsv-5.9.jar;C:/OpenCsv/commons-lang3-3.17.0/commons-lang3-3.17.0.jar" BuildAll [optional-base-path]
```
`BuildAll` accepts an optional base path argument; stages fall back to their hardcoded defaults if omitted.

**Run all tests:**
```
java -cp "bin;C:/OpenCsv/opencsv-5.9.jar;C:/OpenCsv/commons-lang3-3.17.0/commons-lang3-3.17.0.jar;C:/eclipse/plugins/junit-jupiter-api_5.14.2.jar;C:/eclipse/plugins/junit-jupiter-engine_5.14.2.jar;C:/eclipse/plugins/junit-platform-launcher_1.14.2.jar;C:/eclipse/plugins/junit-platform-engine_1.14.2.jar;C:/eclipse/plugins/junit-platform-commons_1.14.2.jar;C:/eclipse/plugins/org.opentest4j_1.3.0.jar" TestRunner
```
`TestRunner` discovers and runs all 5 test classes; exits with code 1 on any failure.

## Architecture

### Pipeline Stages (run in order via `BuildAll`)

| Class | Role |
|---|---|
| `HistoryClean` | Normalizes and sorts `History.csv` in-place; warns to stderr on possible first/last name transpositions |
| `HistoryGraph` | Generates main Mermaid.js visualization (`index.html`) |
| `HistoryRelationships` | Creates one HTML page per person in `Relationships/` |
| `CreateTemplates` | Generates stub biography and relationship pages; **never overwrites existing files** |
| `ConvertIframe` | Inlines `<iframe>` content into root HTML pages; **only handles single-line `<iframe src="...">` tags** |

### Reader Pattern (`RelationshipReader` hierarchy)

`RelationshipReader` (abstract) handles CSV parsing via OpenCSV and builds a `Map<id, GraphNode>`. URL-precedence rule: when a person appears multiple times, the version with an explicit external URL replaces the URL-less version. Subclasses only implement two factory methods:

- `MermaidReader` — returns `MermaidNode` objects; emits Mermaid flowchart edge syntax for the main graph
- `SimpleReader` — returns plain `GraphNode`; stores bidirectional relationships in `Map<personId, Map<label, List<personIds>>>` and writes per-person HTML pages
- `CsvNodeReader` (inner class in `CreateTemplates`) — minimal subclass that collects nodes only, discards relationships

### Output Writers (`GraphWriter` interface)

`HistoryGraph` instantiates one of two implementations:

- **`MermaidWriter`** — wraps diagram in `<pre class="mermaid">` and injects the Mermaid CDN script for client-side rendering
- **`SvgWriter`** — buffers the diagram, **strips `@{}` image-metadata lines** (unsupported by mermaid.ink), compresses with zlib/base64url, fetches pre-rendered SVG from `mermaid.ink/svg/pako:{encoded}`, and embeds it directly in HTML. Uses 30s connection / 60s request timeouts; throws `RuntimeException` on HTTP errors.

### Data Model

- **`GraphNode`** — represents a person. `convertToId()` strips text after `(` or newline, replaces spaces and apostrophes with `_`, and transliterates diacritics (30+ character mappings). **`GraphNode.PATH` is a static field that must be set once via `setPath()` before constructing any nodes** — it controls where the constructor checks for `Images/{id}.jpg`.
- **`MermaidNode`** — extends `GraphNode`; `toString()` produces the full Mermaid node definition, click directive (`_blank`), blue-border style directive, and optional `@{}` image annotation when a portrait file exists.
- **`HistoryFileProcessor`** — interface holding CSV column index constants and the `DEFAULT_PATH` value.

### Relationship Normalization (`SimpleReader.createRelationship`)

The CSV `Relationship` column value is normalized before storage:
- `"trained"` expands to `"Trained by"`; `"maybe"` expands to `"Maybe taught by"`
- `"family"` is stored as `"Earlier generation"` from the senior's perspective
- Relationships are stored bidirectionally with mirrored labels:
  - `Sensei` ↔ `Deshi`
  - `Trained by` ↔ `Trained`
  - `Earlier generation` ↔ `Later generation`
  - `Maybe taught by` ↔ `Maybe taught`

## Mermaid Arrow Styles (`MermaidReader`, case-insensitive)

| Relationship value | Mermaid syntax |
|---|---|
| `sensei` | `A --> B` (solid arrow) |
| `family` | `A ==> B` (thick arrow) |
| `partner` | `A <-.-> B` (bidirectional dashed) |
| any other non-empty | `A -.label.-> B` (dashed with label) |
| *(empty)* | no edge emitted |

## Input CSV Format

File: `History.csv` — columns (0-indexed, defined in `HistoryFileProcessor`):

| Index | Name | Notes |
|---|---|---|
| 0 | `SeniorPerson` | Teacher, parent, or earlier-generation figure |
| 1 | `JuniorPerson` | May be blank for standalone entries |
| 2 | `Relationship` | Type string; see arrow styles above |
| 3 | `SeniorUrl` | Optional external URL for the senior person |

## Hardcoded Paths

Every processing stage has hardcoded paths:
```java
String path = "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
String fileName = "C:\\Users\\user\\Desktop\\Demo\\Website\\History.csv";
```

Expected directory structure under the base path:
- `History.csv` — input data
- `Relationships/` — output per-person pages (created automatically by `SimpleReader`)
- `Images/` — portrait files named `{id}.jpg` (id derived from person name via `convertToId()`)
- `Timeline/` — optional; `{id}.html` files linked from biography pages when present

## Testing

JUnit 5 tests in `test/` cover:

- `GraphNodeTest` — `convertToId()` name-to-ID conversion, diacritic transliteration, URL behavior
- `HistoryCleanTest` — CSV normalization, whitespace trimming, transposition detection
- `MermaidReaderTest` — arrow-style mapping for each relationship type including case-insensitivity
- `SimpleReaderTest` — bidirectional relationship storage and label mirroring
- `PipelineStageTest` — end-to-end stage integration using `@TempDir`; the `historyGraphCreatesIndexHtml` test is skipped automatically if mermaid.ink is unreachable

`SanityTest.main()` recursively validates that all filenames in the output directory contain only `[A-Za-z0-9_.]`; run it after adding new entries to catch characters that would break links. Default path: `C:\Users\user\Desktop\Demo\Website\`.

`MermaidToImage.java` is a standalone demo of the mermaid.ink API — it is **not part of the build pipeline**.
