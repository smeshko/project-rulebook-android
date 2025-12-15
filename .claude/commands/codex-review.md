---
description: Run BMAD code-review workflow in Codex headless mode
argument-hint: "[mode] | Examples: /codex-review | /codex-review fix | /codex-review action-items"
allowed-tools: Bash, Read
---

## Codex Code Review (Headless)

Execute the BMAD code-review workflow using Codex CLI in headless mode.

### Argument Parsing

Parse `$ARGUMENTS` for optional mode:
- If empty or "report-only" → `auto_fix_mode="report-only"` (default - just report, don't modify files)
- If "fix" → `auto_fix_mode="fix"` (automatically fix issues)
- If "action-items" → `auto_fix_mode="action-items"` (create task items in story file)

### Execution

Define the report output file:
```
REPORT_FILE="/tmp/codex-review-report-$(date +%s).md"
```

Run the appropriate command based on the mode. **IMPORTANT**: Instruct Codex to write the final review report to the report file.

**Default (report-only mode):**
```bash
codex exec --full-auto -m gpt-5.1-codex-max -c 'headless=true' -c 'auto_fix_mode="report-only"' "Run the /bmad:bmm:workflows:code-review workflow on the current branch. Write ONLY the final code review report (findings table, summary, and recommendations) to ${REPORT_FILE} - do not include exploration steps or thinking."
```

**Fix mode:**
```bash
codex exec --full-auto -m gpt-5.1-codex-max -c 'headless=true' -c 'auto_fix_mode="fix"' "Run the /bmad:bmm:workflows:code-review workflow on the current branch. Write ONLY the final code review report (findings table, summary, fixes applied, and recommendations) to ${REPORT_FILE} - do not include exploration steps or thinking."
```

**Action-items mode:**
```bash
codex exec --full-auto -m gpt-5.1-codex-max -c 'headless=true' -c 'auto_fix_mode="action-items"' "Run the /bmad:bmm:workflows:code-review workflow on the current branch. Write ONLY the final code review report (findings table, summary, action items created, and recommendations) to ${REPORT_FILE} - do not include exploration steps or thinking."
```

### Behavior

1. Runs in headless mode - no step confirmations or user prompts
2. Auto-detects story from current git branch (pattern: `story/{key}-*`)
3. Performs adversarial code review finding 3-10 issues minimum
4. Based on mode:
   - **fix**: Automatically fixes HIGH and MEDIUM issues
   - **report-only**: Outputs findings without modifying any files
   - **action-items**: Adds review items to story Tasks/Subtasks
5. Updates story status and sprint-status.yaml when complete
6. **Writes final report to temp file** for clean output

### Output

After Codex completes:
1. Read the report file using the Read tool: `${REPORT_FILE}`
2. Display only the report contents to the user
3. Clean up: `rm ${REPORT_FILE}` (optional)

If the report file doesn't exist or is empty, fall back to displaying the Codex exit status and any error messages.
