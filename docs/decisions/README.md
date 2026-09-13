# Decision records

Record each decision as a dated Markdown file with: status, context, verified facts, hypotheses, user decisions, rejected approaches, evidence links, and consequences.

Initial facts: the template is the starting point; `dev.screentime` is the project namespace; SQLite in an isolated control process is authoritative; full blocking is a measured feasibility experiment. Initial hypotheses: delegating `AppComponentFactory` can gate enough initialization, and instrumented processes can be stopped and kept from restarting. No hypothesis is a compatibility claim.

The current patch declares a provider and activity and replaces `appComponentFactory`, preserving the original factory name as metadata. Delegation and early snapshot access have not been implemented or verified; do not use the declaration as evidence that factory compatibility works.
