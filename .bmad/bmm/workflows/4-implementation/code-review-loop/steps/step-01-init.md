---
name: 'step-01-init'
description: 'Initialize code review loop by loading story context, identifying code changes, and preparing tracking state'

# Path Definitions
workflow_path: '{project-root}/.bmad/bmm/workflows/4-implementation/code-review-loop'

# File References
thisStepFile: '{workflow_path}/steps/step-01-init.md'
nextStepFile: '{workflow_path}/steps/step-02-loop.md'
workflowFile: '{workflow_path}/workflow.md'

# Config References
config_source: '{project-root}/.bmad/bmm/config.yaml'
sprint_artifacts: '{config_source}:sprint_artifacts'
output_folder: '{config_source}:output_folder'

# Template References
# (none required for this step)

# Task References
# (none required for this step)
---

# Step 1: Initialize Code Review Loop

## STEP GOAL:

To load all necessary context for the code review loop: story file, acceptance criteria, code changes, and architecture context. Initialize tracking state for the review cycles.

## MANDATORY EXECUTION RULES (READ FIRST):

### Universal Rules:

- 📖 CRITICAL: Read the complete step file before taking any action
- 🔄 CRITICAL: When loading next step, ensure entire file is read
- 🤖 This is an AUTONOMOUS workflow - proceed without user interaction

### Role Reinforcement:

- ✅ You are a senior developer and code quality guardian
- ✅ You orchestrate review by delegating to Codex, then validating and fixing
- ✅ Work autonomously to deliver clean, reviewed code

### Step-Specific Rules:

- 🎯 Focus ONLY on loading context and initializing state
- 🚫 FORBIDDEN to start any review or fixes in this step
- 📋 Auto-detect story from git branch pattern

## EXECUTION PROTOCOLS:

- 🎯 Load all required context before proceeding
- 💾 Initialize tracking state in memory
- 📖 Auto-proceed to step 2 after initialization
- 🚫 FORBIDDEN to skip any initialization tasks

## INITIALIZATION SEQUENCE:

### 1. Detect Current Story

Detect the story from git branch:

```bash
git branch --show-current
```

Expected pattern: `story/{story-key}-*` (e.g., `story/3-1-create-parameter-input-widget`)

Extract story key from branch name.

### 2. Load Story File

Find and load the story file from sprint artifacts:

```bash
# Pattern: {sprint_artifacts}/{story-key}-*.md
```

Read the complete story file including:
- Acceptance criteria (Given/When/Then)
- Dev Notes and implementation guidance
- Tasks and subtasks
- Any existing review notes

### 3. Identify Code Changes

Get ALL changes on this branch compared to staging.

**This includes both committed AND uncommitted changes.**

```bash
# Base branch is always staging
BASE_BRANCH="origin/staging"

# Get merge-base (where this branch diverged from staging)
MERGE_BASE=$(git merge-base $BASE_BRANCH HEAD)

# Get ALL changed files since branch diverged from base
# This captures: committed changes + staged changes + unstaged changes
git diff --name-only $MERGE_BASE HEAD    # Committed changes
git diff --name-only HEAD                 # Uncommitted changes (if any)
```

**Combine and deduplicate** the file lists to get all files that need review.

Also get the git log for context on what was changed:

```bash
# Show commits on this branch not in base
git log --oneline $BASE_BRANCH..HEAD
```

Store:
- `base_branch` - the branch we're comparing against
- `changed_files` - deduplicated list of all changed files
- `commit_count` - number of commits on this branch

### 4. Load Architecture Context

Load project architecture for validation context:

- Read `{output_folder}/architecture.md` or `{output_folder}/*architecture*/*.md`
- Read `{output_folder}/project-context.md` if exists

### 5. Initialize Tracking State

Initialize in-memory state:

```
cycle_count = 0
max_cycles = 2
issues_fixed = []
issues_skipped = []
exit_reason = null
```

### 6. Display Initialization Summary

Print to terminal:

```
═══════════════════════════════════════════════════════════════
  CODE REVIEW LOOP - Initialized
═══════════════════════════════════════════════════════════════
  Story: {story-key}
  Base Branch: {base_branch}
  Commits on Branch: {commit_count}
  Changed Files: {file_count} files
  Max Cycles: 2

  Starting review loop...
═══════════════════════════════════════════════════════════════
```

### 7. Auto-Proceed to Review Loop

After initialization complete, immediately load and execute `{workflow_path}/steps/step-02-loop.md`.

## CRITICAL STEP COMPLETION NOTE

This is an auto-proceed step. After all initialization tasks are complete, immediately load, read entire file, and execute step-02-loop.md to begin the review cycle.

---

## 🚨 SYSTEM SUCCESS/FAILURE METRICS

### ✅ SUCCESS:

- Story file loaded and parsed
- Code changes identified
- Architecture context loaded
- Tracking state initialized
- Auto-proceeded to step 2

### ❌ SYSTEM FAILURE:

- Could not detect story from branch
- Story file not found
- Stopping to ask user questions (this is autonomous)
- Not auto-proceeding to step 2

**Master Rule:** This is an AUTONOMOUS workflow. Do not stop for user input. Proceed automatically through all steps.
