package io.github.luanfrombrazil.secondbrain.vault;

import java.io.IOException;
import java.nio.file.Path;

/** Another window already has this vault open. */
public final class VaultInUseException extends IOException {

    public VaultInUseException(Path root) {
        super("Vault already open: " + root);
    }
}
