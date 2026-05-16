# Codex + OMX + ECC Global SOP

## Purpose

This SOP standardizes the stable integration model for:

- Codex CLI
- oh-my-codex (OMX)
- Everything Claude Code (ECC)

Target architecture:

```text
Codex = host/execution
OMX   = orchestration/runtime/hooks
ECC   = content layer only
```

## Ownership Rules

Only one system may own each control surface.

| Surface | Owner | Notes |
| --- | --- | --- |
| root `AGENTS.md` | OMX | Top-level operating contract |
| `.codex/hooks.json` | OMX | Native hook ownership |
| `.omx/state/` | OMX | Runtime state and workflow persistence |
| `.omx/plans/` | OMX | PRD, plan, verification artifacts |
| `.codex/agents/*` | OMX | Active native subagent catalog |
| `.codex/prompts/*` | OMX | Active prompt surfaces |
| `.codex/skills/*` | OMX | Workflow skills, plus approved host/helper exceptions |
| `.ai/ecc/**` | ECC | Rules, memory, archived inventories, reusable content |
| Codex host policy | Codex | approval, sandbox, web search, model host behavior |

Non-negotiable rules:

- Do not let ECC write active runtime hooks.
- Do not let ECC replace root `AGENTS.md`.
- Do not let ECC spawn runtime workers under OMX team/runtime mode.
- Do not keep duplicate autonomy directives in both OMX and ECC runtime surfaces.

## Required Directory Shape

```text
project/
├── .omx/
│   ├── state/
│   ├── logs/
│   └── plans/
├── .codex/
│   ├── config.toml
│   └── hooks.json
├── AGENTS.md
├── .ai/
│   └── ecc/
│       ├── README.md
│       ├── rules/
│       └── memory/
└── docs/
```

## Install / Migration Procedure

### 1. Install OMX as runtime owner

- Run `omx setup`.
- Keep OMX-generated hooks and active runtime files under `.codex/` and `.omx/`.
- Use `omx doctor` to verify the install.

### 2. Move ECC to content layer

- Keep ECC artifacts under `.ai/ecc/` inside the project.
- Archive any global ECC runtime residue under `~/.codex/ecc/`.
- Do not expose ECC content through active `.codex/agents`, `.codex/prompts`, or git hook paths.

### 3. Remove dual-runtime residue

Review project and global config for:

- extra `profiles.*` injected by ECC
- supplement `agents.*` entries owned outside OMX
- old ECC MCP entries still living in active `.codex/config.toml`
- global `core.hooksPath` overrides
- root `AGENTS.md` blocks that give ECC runtime or hook authority

### 4. Record ownership explicitly

Keep these repository artifacts current:

- `.ai/ecc/README.md`
- `.ai/ecc/rules/ownership-contract.md`
- `.ai/ecc/mcp-inventory.md`
- `.omx/plans/allowlists/runtime-agents.txt`
- `.omx/plans/allowlists/runtime-prompts.txt`
- `.omx/plans/allowlists/runtime-workflow-skills.txt`
- `.omx/plans/allowlists/runtime-helper-skill-exceptions.txt`
- `.omx/plans/allowlists/runtime-host-skill-exceptions.txt`

## Validation Checklist

Run these checks after any migration or upgrade.

### Runtime health

```bash
omx doctor
```

Expected result:

- no failed checks
- warnings only when they do not affect runtime ownership

### Residue scan

```bash
rg -n "^\[profiles\.(strict|yolo)\]|^\[agents\.(explorer|reviewer|docs_researcher)\]|^\[mcp_servers\.(supabase|playwright|exa|github|memory|sequential-thinking|context7|mysql)\]" \
  ~/.codex/config.toml .codex/config.toml
```

Expected result:

- no matches unless a new ADR intentionally reassigns ownership

### Hook ownership

```bash
git config --global --get core.hooksPath
```

Expected result:

- empty output

### Active runtime surface parity

Project `.codex/agents` and `.codex/prompts` should match the allowlists in `.omx/plans/allowlists/`.

### ECC inactivity check

Search these locations for active ECC injection:

- `~/.codex/AGENTS.md`
- `~/.codex/skills/.system`
- `~/.codex/skills/codex-primary-runtime`

Expected result:

- at most a neutral reference statement
- no ECC runtime hooks, ownership blocks, or injected prompt wrappers

## Upgrade Rule

When upgrading OMX, Codex, or ECC:

1. Re-run `omx setup` only if OMX runtime files need refresh.
2. Re-run the validation checklist.
3. Compare active `.codex` runtime surfaces against allowlists.
4. Re-check `.ai/ecc/mcp-inventory.md` and update ownership notes if a tool changes class.

## Recovery Rule

If runtime behavior regresses after an upgrade:

1. Freeze active `.codex/config.toml`, `AGENTS.md`, and `.omx/plans/allowlists/`.
2. Identify whether the regression came from hooks, prompts, agents, or MCP ownership.
3. Remove the second owner instead of layering more compatibility glue.
4. Re-run `omx doctor` and residue scans before declaring recovery complete.

## Decision Rule For New Capabilities

Before adding a new tool, prompt pack, or memory layer, decide:

1. Is it runtime or content?
2. If runtime, should OMX own it?
3. If content, can it live entirely under `.ai/ecc/`?
4. What command or scan proves there is still only one owner?

If the answer is unclear, do not activate it in `.codex/`.
