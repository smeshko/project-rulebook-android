# Epic Structure Plan

## Epic Design Strategy

**Guiding Principles:**
1. **User-Value First** - Each epic delivers something users can accomplish
2. **Architecture Alignment** - Epics respect the 16-module structure
3. **Incremental Delivery** - Each epic builds on previous, independently valuable
4. **Natural Dependencies** - Foundation → Core Features → Enhancement

**Module-to-Epic Mapping:**

| Architecture Module | Epic Coverage |
|---------------------|---------------|
| `app/` | Epic 1 (shell), Epic 2 (navigation) |
| `core/designsystem` | Epic 1 |
| `core/data`, `core/database`, `core/network` | Epic 1 |
| `core/model`, `core/common` | Epic 1 |
| `core/analytics` | Epic 1 |
| `core/datastore` | Epic 1, Epic 3 |
| `core/billing` | Epic 8 |
| `feature/onboarding` | Epic 3 |
| `feature/camera` | Epic 4 |
| `feature/rules` | Epic 5, Epic 6 |
| `feature/library` | Epic 2, Epic 7 |
| `feature/settings` | Epic 2, Epic 9 |
| `feature/purchase` | Epic 8 |

---

## Epic Overview

| Epic | Title | User Value | FRs Covered |
|------|-------|------------|-------------|
| **1** | Foundation & Design System | Technical enablement | Infrastructure |
| **2** | App Shell & Navigation | Launch and navigate app | FR27, FR33, FR50 |
| **3** | Onboarding Experience | Understand app, get free credits | FR1-4, FR34 |
| **4** | Photo Capture Flow | Take/select game box photos | FR5-11, FR51-52 |
| **5** | Game Recognition & Rules Generation | Photo becomes rules | FR12-18, FR26 |
| **6** | Rules Display & Reference | Read and reference rules | FR19-25, FR32 |
| **7** | Library Management | Manage game collection | FR27-31 |
| **8** | Credit System & Purchases | Buy and manage credits | FR34-41 |
| **9** | Settings & Platform Polish | Customize and polish app | FR42-49 |

---
