# AMSS Test Coverage Snapshot

This repository includes a machine-readable coverage snapshot so source-code marketplace scanners can grade test coverage from the repository itself.

The snapshot was produced by JaCoCo in GitHub Actions from the green CI run for marketplace quality hardening.

| Metric | Covered | Missed | Coverage |
| --- | ---: | ---: | ---: |
| Instructions | 3,524 | 12,006 | 22.69% |
| Branches | 196 | 898 | 17.92% |
| Lines | 593 | 1,617 | 26.83% |
| Methods | 253 | 682 | 27.06% |
| Classes | 52 | 144 | 26.53% |

The canonical machine-readable report is `coverage/jacoco.xml`. CI regenerates and uploads the current report on every run. This snapshot is evidence of measured coverage, not a claim of high coverage; future test work should increase these values.
