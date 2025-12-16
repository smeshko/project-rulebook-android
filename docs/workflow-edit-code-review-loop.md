---
stepsCompleted: [1, 2, 3, 4, 5]
targetWorkflow: code-review-loop
status: complete
completionDate: 2025-12-16
---

# Workflow Edit: code-review-loop

## Workflow Analysis

### Target Workflow

- **Path**: .bmad/bmm/workflows/4-implementation/code-review-loop
- **Name**: code-review-loop
- **Module**: bmm (4-implementation)
- **Format**: Standalone (step-file architecture)

### Structure Analysis

- **Type**: Autonomous meta-workflow
- **Total Steps**: 3
- **Step Flow**: step-01-init → step-02-loop → step-03-finalize
- **Files**:
  - workflow.md
  - workflow-plan-code-review-loop.md
  - steps/step-01-init.md
  - steps/step-02-loop.md
  - steps/step-03-finalize.md

### Content Characteristics

- **Purpose**: Automate code review cycles using Codex for adversarial review
- **Instruction Style**: Prescriptive for loop control, intent-based for validation
- **User Interaction**: Fully autonomous - no user input required
- **Complexity**: Medium-high (dual-agent system with loop logic)

### Initial Assessment

#### Strengths

- Well-structured autonomous workflow
- Clear separation of concerns across steps
- Good tracking of issues fixed/skipped
- Proper commit strategy with per-cycle commits
- Comprehensive PR creation with summary

#### Potential Issues

- Assumes user is already on correct story branch
- No option to provide story number as input
- No worktree detection or navigation
- Could fail silently if run from wrong branch

#### Format-Specific Notes

- Uses new standalone format correctly
- Proper frontmatter in all step files
- Auto-proceed patterns appropriate for autonomous workflow

### Best Practices Compliance

- **Step File Structure**: Compliant
- **Frontmatter Usage**: Compliant
- **Menu Implementation**: N/A (autonomous)
- **Variable Consistency**: Compliant

---

## Improvement Goals

### User Goal

Add intelligent worktree detection and navigation when a story number is provided but user is not on the correct branch.

### Desired Behavior

1. Accept optional story parameter (e.g., `3-1`, `T005`, or story key)
2. If story provided → check if current branch matches that story
3. If NOT on correct branch → query `git worktree list`
4. If matching worktree exists → change to that worktree directory
5. Continue with existing initialization flow from that directory

### Priority

**CRITICAL** - This is a new feature that improves workflow usability

### Implementation Location

Primary file: `steps/step-01-init.md`
- Modify section "1. Detect Current Story"
- Add new section before current detection for worktree handling

---

## Improvement Log

### Change 1: Add worktree detection to step-01-init.md

**Files Modified:**
- `.bmad/bmm/workflows/4-implementation/code-review-loop/workflow.md`
- `.bmad/bmm/workflows/4-implementation/code-review-loop/steps/step-01-init.md`

**What Changed:**

1. **workflow.md frontmatter**: Added optional `story_id` input parameter documentation

2. **step-01-init.md frontmatter**:
   - Added `worktree_base` config reference
   - Added `story_id` input parameter documentation

3. **step-01-init.md INITIALIZATION SEQUENCE**: Replaced "1. Detect Current Story" with new "1. Resolve Story and Environment" section containing:
   - **1a. Check for Story Parameter**: Checks if story_id was provided
   - **1b. Verify Environment for Provided Story**: If story provided, verifies correct branch or finds matching worktree using `git worktree list --porcelain`
   - **1c. Auto-Detect Story from Current Environment**: Falls back to original behavior (detect from branch or worktree directory)

**Rationale:**
- Allows running code review on a specific story without manually switching to that worktree
- Uses `git worktree list --porcelain` for reliable parsing
- Provides clear error message with actionable options if worktree not found
- Preserves backward compatibility - still auto-detects if no story parameter provided

---

## Validation Results

### File Structure Validation
- [x] All required files present
- [x] Directory structure correct
- [x] File names follow conventions
- [x] Path references resolve correctly

### Configuration Validation
- [x] workflow.md frontmatter complete
- [x] All variables properly formatted
- [x] Path variables use correct syntax ({project-root}, {workflow_path})
- [x] No hardcoded paths exist

### Step File Compliance
- [x] Step follows template structure
- [x] Mandatory rules included
- [x] Menu handling: N/A (autonomous workflow)
- [x] Step numbering sequential (1-7)
- [x] Step file size: 7.8KB (within 5-10KB target)

### Cross-File Consistency
- [x] Variable names match across files
- [x] No orphaned references
- [x] worktree_base reference added and consistent with dev-begin workflow
- [x] story_id parameter documented in both workflow.md and step file

### Best Practices Adherence
- [x] Error handling included (HALT with actionable message if worktree not found)
- [x] Naming conventions followed (snake_case for variables)
- [x] Instructions clear and specific
- [x] Backward compatible (auto-detect still works if no parameter provided)

**Result: ✅ ALL VALIDATION CHECKS PASSED**

---

## Compliance Check Results

| Category | Status | Notes |
|----------|--------|-------|
| Template Adherence | ✅ PASS | Follows step-template structure, frontmatter complete |
| File Size | ✅ PASS | step-01-init.md is 7.8KB (within 5-10KB target) |
| Path Variables | ✅ PASS | Uses {project-root}, {workflow_path}, no hardcoded paths |
| Variable Consistency | ✅ PASS | worktree_base matches dev-begin pattern |
| Intent Spectrum | ✅ PASS | Prescriptive instructions for autonomous flow |
| Error Handling | ✅ PASS | HALT with actionable error message |
| Backward Compatibility | ✅ PASS | Auto-detect still works if no parameter |

**Overall Compliance: ✅ COMPLIANT**

---

## Completion Summary

### What Was Changed

The code-review-loop workflow now supports optional story ID input with intelligent worktree detection:

1. **New Input Parameter**: `story_id` - optional parameter to specify which story to review
2. **Worktree Detection**: If story provided and not on correct branch, automatically finds and switches to matching worktree
3. **Backward Compatible**: Still auto-detects from current branch/worktree if no parameter provided

### Files Modified

| File | Change |
|------|--------|
| `workflow.md` | Added story_id parameter documentation to frontmatter |
| `steps/step-01-init.md` | Added worktree_base config, rewrote section 1 with worktree detection logic |

### Usage

```
# Auto-detect story from current branch (existing behavior)
/bmad:bmm:workflows:code-review-loop

# Specify story - auto-navigates to worktree if needed (new behavior)
/bmad:bmm:workflows:code-review-loop 3-1
```

### Next Steps

1. Test the workflow with a story that has an active worktree
2. Verify worktree detection works with `git worktree list --porcelain`
3. Confirm error message displays correctly when worktree not found

---

_Workflow edit completed on 2025-12-16_
