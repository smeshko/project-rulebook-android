---
stepsCompleted: [1, 2, 3]
target_workflow: _bmad/bmm/workflows/4-implementation/create-story
format: legacy-xml
---

# Workflow Edit: create-story

## Analysis Summary

**Target:** `_bmad/bmm/workflows/4-implementation/create-story/`
**Format:** Legacy XML (workflow.yaml + instructions.xml)
**Purpose:** Create implementation-ready story files from epics/stories

## Improvement Goals

### CRITICAL: ADW SDK Integration

**Goal:** Enable workflow to run under ADW SDK environment

**Requirements:**
1. Check for `ADW_ISSUE_BODY` env var at start of Step 1
2. If set, parse story ID (format: `1.2` or `1-2`)
3. Write `story_id` to `$ADW_STATE_FILE` JSON
4. Set internal variables and skip normal discovery
5. If not set, proceed with existing flow unchanged

## Changes Applied

### File: `instructions.xml`

**Location:** Beginning of Step 1 (line 20)

**Change:** Added ADW environment check block before existing story discovery logic

```xml
<!-- ADW SDK Integration: Check for ADW environment first -->
<check if="environment variable ADW_ISSUE_BODY is set and non-empty">
  <critical>Running in ADW environment - parse story ID from ADW_ISSUE_BODY</critical>
  <action>Parse story ID from ADW_ISSUE_BODY (expected format: "1.2" or "1-2")</action>
  <action>Extract epic_num (first number) and story_num (second number)</action>
  <action>Set {{epic_num}} and {{story_num}} from parsed values</action>
  <action>Derive {{story_key}} by looking up the story in epics file or sprint-status matching epic_num-story_num pattern</action>
  <action>Set {{story_id}} = "{{epic_num}}.{{story_num}}"</action>

  <!-- Update ADW state file with story_id for subsequent commands -->
  <action>Read current state from $ADW_STATE_FILE</action>
  <action>Add "story_id": "{{story_id}}" to the state JSON</action>
  <action>Write updated state back to $ADW_STATE_FILE using:
    jq '. + {"story_id": "{{story_id}}"}' $ADW_STATE_FILE > tmp.json && mv tmp.json $ADW_STATE_FILE
  </action>
  <output>🤖 ADW Mode: Story {{story_id}} loaded from ADW_ISSUE_BODY</output>
  <action>GOTO step 2a</action>
</check>
```

**Behavior:**
- If `ADW_ISSUE_BODY` is set → parse story ID, update state file, proceed to step 2
- If not set → fall through to existing logic (user input or sprint-status auto-discovery)
