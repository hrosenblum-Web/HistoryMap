# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**HistoryMap** is a Java application that converts CSV data about historical martial arts figures into interactive HTML relationship graphs using Mermaid.js diagrams. It generates a main visualization, individual per-person relationship pages, and template HTML for new entries.

## Build System

**Eclipse Java project** — no Maven or Gradle.

- Java 21 (configured in `.classpath` and `.settings/org.eclipse.jdt.core.prefs`)
- Source: `src/`, compiled output: `bin/`
- Two external JARs must exist at these hardcoded paths (update `.classpath` if different):
  - `C:/OpenCsv/opencsv-5.9.jar`
  - `C:/OpenCsv/commons-lang3-3.17.0/commons-lang3-3.17.0.jar`

**CLI compile:**
```
javac -cp "bin;C:/OpenCsv/opencsv-5.9.jar;C:/OpenCsv/commons-lang3-3.17.0/commons-lang3-3.17.0.jar" src/*.java -d bin
```

**Run all stages:** `BuildAll.main()`

## Architecture

### Pipeline Stages (run in order via `BuildAll`)

| Class | Role |
|---|---|
| `HistoryClean` | Normalizes and sorts `History.csv` |
| `HistoryGraph` | Generates main Mermaid.js visualization (`index.html`) |
| `HistoryRelationships` | Creates one HTML page per person in `Relationships/` |
| `CreateTemplates` | Generates blank template HTML for new entries |
| `ConvertIframe` | Inlines `<iframe>` content into HTML files |

### Reader Pattern (`RelationshipReader` hierarchy)

`RelationshipReader` (abstract) handles CSV parsing and builds a `GraphNode` map; subclasses implement output format:
- `MermaidReader` — emits Mermaid flowchart syntax
- `SimpleReader` — emits HTML relationship lists

This avoids duplicating CSV logic across output formats.

### Data Model

- `GraphNode` — represents a person; converts names to IDs (spaces/apostrophes → `_`, special chars transliterated, text after `(` or newline stripped)
- `MermaidNode` — extends `GraphNode` with Mermaid diagram node formatting
- `HistoryFileProcessor` — interface holding CSV column index constants

### Relationship Normalization (`SimpleReader.createRelationship`)

Relationships are bidirectional with mirrored role labels:
- `Sensei` ↔ `Deshi`
- `Trained by` ↔ `Trained`
- `Earlier generation` ↔ `Later generation`
- `Maybe taught by` ↔ `Maybe taught`

## Input CSV Format

File: `History.csv` — columns defined in `HistoryFileProcessor`:

```
SeniorPerson, JuniorPerson, Relationship, SeniorUrl
```

Special Mermaid relationship types: `sensei`, `family`, `partner` (get distinct arrow styles). All others render as dashed lines with labels.

## Hardcoded Paths

Every processing stage contains hardcoded paths like:
```java
String path = "C:\\Users\\user\\Desktop\\Demo\\WebsiteTesting\\";
String fileName = "C:\\Users\\user\\Desktop\\Demo\\Website\\History.csv";
```

To use in a different environment, update the path string in each main class. Expected directory structure:
- `History.csv` — input data
- `Relationships/` — output per-person pages
- `Images/` — portrait files named `{id}.jpg` (id derived from person name)

## Testing

No formal test suite. `SanityTest.main()` recursively validates that all filenames in the working directory contain only word characters (`[A-Za-z0-9_.]`). Run individual stages directly by invoking `ClassName.main(new String[]{})` after updating the hardcoded path.
