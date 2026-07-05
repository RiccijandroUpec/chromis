package uk.chromis.pos.invoice.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

/**
 * Restringe permisos de archivos/directorios que contienen datos fiscales sensibles
 * (comprobantes firmados, RIDE, certificados) al propietario del proceso.
 */
public final class FileSecurity {

    private FileSecurity() {
    }

    public static void restrictToOwner(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        try {
            Set<PosixFilePermission> perms = file.isDirectory()
                    ? EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)
                    : EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE);
            Files.setPosixFilePermissions(file.toPath(), perms);
        } catch (UnsupportedOperationException notPosix) {
            // Sistemas de archivos no-POSIX (p. ej. Windows sin ACL POSIX): usar el mecanismo estándar de File.
            file.setReadable(false, false);
            file.setWritable(false, false);
            file.setExecutable(false, false);
            file.setReadable(true, true);
            file.setWritable(true, true);
            if (file.isDirectory()) {
                file.setExecutable(true, true);
            }
        } catch (Exception e) {
            System.err.println("Advertencia: no se pudieron restringir permisos de " + file + ": " + e.getMessage());
        }
    }
}
