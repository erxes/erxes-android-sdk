# Publishing `messenger-sdk` to Maven Central

The library publishes to **Maven Central via the Sonatype Central Portal** using the
[vanniktech maven-publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/).

- **Coordinates:** `io.github.munkhorgilb:messenger-sdk:<version>`
- **Consumers** (incl. the Flutter / React Native plugins) just need `mavenCentral()` —
  no extra repository, no credentials.

  ```kotlin
  implementation("io.github.munkhorgilb:messenger-sdk:0.30.4")
  ```

## One-time setup

### 1. Central Portal account + namespace

1. Sign in at <https://central.sonatype.com> with GitHub.
2. Register the namespace **`io.github.munkhorgilb`**. Because it matches the GitHub
   account, it is verified automatically (no DNS record needed).
3. Generate a **user token**: _Account → Generate User Token_. You get a username +
   password pair — these are `mavenCentralUsername` / `mavenCentralPassword`.

### 2. GPG signing key

Central requires every artifact to be signed.

```bash
# Create a key (use a real name/email; remember the passphrase)
gpg --gen-key

# Find the key id (the long hex after 'sec')
gpg --list-secret-keys --keyid-format=long

# Publish the public key so Central can verify signatures
gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>

# Export the private key in the in-memory (ASCII-armored) form the plugin wants
gpg --export-secret-keys --armor <KEY_ID> > private.key
```

### 3. Credentials in `~/.gradle/gradle.properties`

**Never commit these** — they live in your home Gradle file, not the repo.

```properties
mavenCentralUsername=<central-portal-token-username>
mavenCentralPassword=<central-portal-token-password>

# In-memory signing key: paste the full armored block with \n for newlines,
# OR point at a file-based key (signing.keyId/.password/.secretKeyRingFile).
signingInMemoryKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
signingInMemoryKeyPassword=<your-gpg-passphrase>
```

> Tip: turn `private.key` into one escaped line:
> `awk 'NF {sub(/\r/,""); printf "%s\\n",$0;}' private.key`

## Releasing a version

1. Bump `version` in `messenger-sdk/build.gradle.kts` (e.g. `0.30.4` → `0.30.5`).
   Use a non-`SNAPSHOT` version — Central only accepts release versions.
2. Smoke-test locally first (installs into `~/.m2`):
   ```bash
   ./gradlew :messenger-sdk:publishToMavenLocal
   ```
3. Upload to Central:
   ```bash
   ./gradlew :messenger-sdk:publishToMavenCentral
   ```
   This builds + signs the AAR, sources jar, and javadoc jar, then uploads the bundle.
4. To publish automatically without the manual "Publish" click in the portal UI:
   ```bash
   ./gradlew :messenger-sdk:publishAndReleaseToMavenCentral
   ```
   Otherwise, go to <https://central.sonatype.com/publishing> and click **Publish**
   on the staged deployment. Propagation to `repo1.maven.org` takes ~10–30 min.

## Notes

- A published version is **immutable** — you cannot overwrite `0.30.4`, only release a new one.
- Tag releases in git to match (`git tag v0.30.2`) so the `scm` POM metadata lines up.
- The same artifact serves native Android, Flutter, and React Native consumers; publish once here.
