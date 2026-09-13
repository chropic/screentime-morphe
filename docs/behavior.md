# Behavior specification

## Activation and counting

Policy activates only after an initial `hh:mm:ss` budget, daily local reset time, and warning/blocking mode are explicitly chosen. Count focused host-app use on an unlocked screen using injected monotonic elapsed time. Exclude background audio, picture-in-picture, unfocused split-screen, and screen-time settings. Checkpoint each second and on lifecycle transitions.

## Exhaustion

Warning mode warns immediately at exhaustion and once in each later foreground session. Blocking mode enforces immediately; settings remain accessible and there is no enforcement-disable switch. The target is enforcement within one second and at most one checkpoint interval lost on ordinary abrupt termination.

## Reset and changes

Budget replenishes at the configured local daily time without carryover. Stricter changes apply immediately. Budget increases, blocking-to-warning changes, and reset-time changes become a replaceable pending policy that activates in 24 hours; replacing it restarts the 24-hour delay. UI shows the pending value and activation time.

## Shortcut

Recognize volume up, down, up within two seconds, ignoring held-key repeats. Keep ordinary volume actions intact. Open controls once per completed sequence.

## Local limitations

Data clearing, reinstalling, and clock manipulation are local-only limitations and must be documented in test evidence. Reboot and process death must retain policy and usage.
