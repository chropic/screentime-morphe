package dev.screentime.runtime;

import android.util.AtomicFile;
import dev.screentime.runtime.core.EnforcementSnapshot;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;

/** Atomically persists only the startup projection; SQLite remains authoritative. */
final class SnapshotStore {
    private static final int FORMAT_VERSION = 1;
    private final AtomicFile file;

    SnapshotStore(File directory) { file = new AtomicFile(new File(directory, "screentime.snapshot")); }

    void write(EnforcementSnapshot snapshot) throws IOException {
        FileOutputStream output = file.startWrite();
        try (DataOutputStream data = new DataOutputStream(output)) {
            data.writeInt(FORMAT_VERSION);
            data.writeUTF(snapshot.decision().name());
            data.writeLong(snapshot.consumedMillis());
            data.writeLong(snapshot.nextReset().toEpochMilli());
            data.writeLong(snapshot.policyRevision());
            data.flush();
            file.finishWrite(output);
        } catch (IOException | RuntimeException failure) {
            file.failWrite(output);
            throw failure;
        }
    }

    EnforcementSnapshot read() throws IOException {
        try (DataInputStream data = new DataInputStream(new FileInputStream(file.getBaseFile()))) {
            if (data.readInt() != FORMAT_VERSION) throw new IOException("Unsupported snapshot format");
            return new EnforcementSnapshot(
                EnforcementSnapshot.Decision.valueOf(data.readUTF()),
                data.readLong(),
                Instant.ofEpochMilli(data.readLong()),
                data.readLong());
        }
    }
}
