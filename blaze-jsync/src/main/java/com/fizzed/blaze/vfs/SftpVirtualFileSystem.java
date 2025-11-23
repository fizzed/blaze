package com.fizzed.blaze.vfs;

import com.fizzed.blaze.core.UnexpectedExitValueException;
import com.fizzed.blaze.jsync.Checksum;
import com.fizzed.blaze.ssh.*;
import com.fizzed.blaze.system.Exec;
import com.fizzed.blaze.util.StreamableInput;
import com.fizzed.blaze.vfs.util.Checksums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static com.fizzed.blaze.SecureShells.sshSftp;
import static com.fizzed.blaze.util.Streamables.nullOutput;

public class SftpVirtualFileSystem extends AbstractVirtualFileSystem {
    static private final Logger log = LoggerFactory.getLogger(SftpVirtualFileSystem.class);

    private final SshSession ssh;
    private final SshSftpSession sftp;
    private int maxCommandLength;
    private final boolean windows;

    protected SftpVirtualFileSystem(String name, VirtualPath pwd, SshSession ssh, SshSftpSession sftp, boolean windows) {
        // everything but windows is case sensitive
        super(name, pwd, !windows);
        this.ssh = ssh;
        this.sftp = sftp;
        this.maxCommandLength = 7000;       // windows shell limit is 8,191, linux/mac/bsd is effectively unlimited
        this.windows = windows;
    }

    static public SftpVirtualFileSystem open(SshSession ssh) {
        final String name = ssh.uri().toString().replace("ssh://", "sftp://");

        log.info("Opening filesystem {}...", name);

        final SshSftpSession sftp = sshSftp(ssh)
            .run();

        final String pwd2 = sftp.pwd2();

        final VirtualPath pwd = VirtualPath.parse(pwd2, true);

        log.debug("Detected filesystem {} has pwd {}", name, pwd);

        boolean windows = false;

        // this is likely a "windows" system if the 2nd char is :
        if (pwd2.length() > 2 && pwd2.charAt(2) == ':') {
            // TODO: should we confirm by running a command that exists only windows to confirm?
            // for now we'll just assume it is
            windows = true;
            log.debug("Detected filesystem {} is running on windows (changes standard checksums, native filepaths, case sensitivity, etc.)", name);
        }

        return new SftpVirtualFileSystem(name, pwd, ssh, sftp, windows);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    @Override
    public boolean isSupported(Checksum checksum) throws IOException {
        if (this.windows) {
            switch (checksum) {
                case MD5:
                case SHA1:
                    return true;
                default:
                    return false;
            }
        }

        // othwerwise, we are on posix and we can actually check whether these would work or not
        switch (checksum) {
            case CK:
                try {
                    this.ssh.newExec().command("which")
                        .arg("cksum")
                        .pipeOutput(nullOutput())
                        .pipeError(nullOutput())
                        .run();
                    return true;
                } catch (UnexpectedExitValueException e) {
                    return false;
                }
            case MD5:
                try {
                    this.ssh.newExec().command("which")
                        .arg("md5sum")
                        .pipeOutput(nullOutput())
                        .pipeError(nullOutput())
                        .run();
                    return true;
                } catch (UnexpectedExitValueException e) {
                    return false;
                }
            case SHA1:
                try {
                    this.ssh.newExec().command("which")
                        .arg("sha1sum")
                        .pipeOutput(nullOutput())
                        .pipeError(nullOutput())
                        .run();
                    return true;
                } catch (UnexpectedExitValueException e) {
                    return false;
                }
            default:
                return false;
        }
    }

    public int getMaxCommandLength() {
        return maxCommandLength;
    }

    public SftpVirtualFileSystem setMaxCommandLength(int maxCommandLength) {
        this.maxCommandLength = maxCommandLength;
        return this;
    }

    private VirtualPath toVirtualPathWithStats(VirtualPath path, SshFileAttributes attributes) throws IOException {
        boolean isDirectory = attributes.isDirectory();
        VirtualStats stats = null;
        if (!isDirectory) {
            long size = attributes.size();
            long modifiedTime = attributes.lastModifiedTime().toMillis();
            stats = new VirtualStats(size, modifiedTime);
        }
        return new VirtualPath(path.getParentPath(), path.getName(), isDirectory, stats);
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public VirtualPath stat(VirtualPath path) throws IOException {
        try {
            // TODO: safer using full path resolved against pwd?
            final SshFileAttributes file = this.sftp.lstat(path.toString());

            return this.toVirtualPathWithStats(path, file);
        } catch (SshSftpNoSuchFileException e) {
            throw new FileNotFoundException();
        }
    }

    @Override
    public List<VirtualPath> ls(VirtualPath path) throws IOException {
        // TODO: safer using full path resolved against pwd?
        final List<SshFile> files = this.sftp.ls(path.toString());

        final List<VirtualPath> childPaths = new ArrayList<>();

        for (SshFile file : files) {
            // dir true/false doesn't matter, stats call next will correct it
            VirtualPath childPathWithoutStats = path.resolve(file.fileName(), false);
            VirtualPath childPath = this.toVirtualPathWithStats(childPathWithoutStats, file.attributes());
            childPaths.add(childPath);
        }

        return childPaths;
    }

    @Override
    public void mkdir(VirtualPath path) throws IOException {
        this.sftp.mkdir(path.toString());
    }

    @Override
    public void rm(VirtualPath path) throws IOException {
        this.sftp.rm(path.toString());
    }

    @Override
    public void rmdir(VirtualPath path) throws IOException {
        this.sftp.rmdir(path.toString());
    }

    @Override
    public StreamableInput readFile(VirtualPath path, boolean progress) throws IOException {
        throw new UnsupportedOperationException("readFile");
    }

    @Override
    public void writeFile(StreamableInput input, VirtualPath path, boolean progress) throws IOException {
        this.sftp.put()
            .source(input)
            .target(path.toString())
//            .progress(progress)
//            .verbose()
            .run();
    }

    @Override
    public void cksums(List<VirtualPath> paths) throws IOException {
        if (this.windows) {
            throw new UnsupportedChecksumException("Checksum CK is not supported on windows", null);
        } else {
            this.hashFilesOnPosix(Checksum.CK, paths);
        }
    }

    @Override
    public void md5sums(List<VirtualPath> paths) throws IOException {
        if (this.windows) {
            this.hashFilesOnWindows(Checksum.MD5, paths);
        } else {
            this.hashFilesOnPosix(Checksum.MD5, paths);
        }
    }

    @Override
    public void sha1sums(List<VirtualPath> paths) throws IOException {
        if (this.windows) {
            this.hashFilesOnWindows(Checksum.SHA1, paths);
        } else {
            this.hashFilesOnPosix(Checksum.SHA1, paths);
        }
    }

    protected void hashFilesOnPosix(Checksum checksum, List<VirtualPath> paths) throws IOException {
        // name of the executable we will run
        final String exeName;
        switch (checksum) {
            case CK:
                exeName = "cksum";
                break;
            case MD5:
                exeName = "md5sum";
                break;
            case SHA1:
                exeName = "sha1sum";
                break;
            default:
                throw new UnsupportedChecksumException("Unsupported checksum '" + checksum + "' on posix is not supported", null);
        }

        // we need to be smart about how many files we request in bulk, as the command line can only be so long
        final Map<String,VirtualPath> fileMappings = new HashMap<>();
        Exec exec = null;
        int commandLength = 0;

        for (int i = 0; i < paths.size(); i++) {
            final VirtualPath path = paths.get(i);

            // create new or add to request?
            if (exec == null) {
                exec = this.ssh.newExec().command(exeName)
                    .pipeErrorToOutput();
            }

            final String fullPath = path.toString();

            // TODO: it turns out $s are interpreted on the server!, we need to escape the fullPath
            // alternatively, we need to handle smart commands better
            if (fullPath.contains("$")) {
                exec.arg("'" + fullPath + "'");
            } else {
                exec.arg(fullPath);
            }


            fileMappings.put(fullPath, path);
            commandLength += fullPath.length();

            // should we send this request?
            if (commandLength >= this.maxCommandLength || (i == paths.size() - 1)) {
                final String output;
                try {
                    output = exec.runCaptureOutput(false)
                        .toString();
                } catch (UnexpectedExitValueException e) {
                    // this likely means the command doesn't exist, either way we will need to flag this isn't supported
                    throw new UnsupportedChecksumException("Checksum algorithm " + checksum + " is not supported on virtual filesystem " + this.getName(), e);
                }

                // parse output into entries
                final List<Checksums.HashEntry> entries;
                switch (checksum) {
                    case CK:
                        entries = Checksums.parsePosixCksumOutput(output);
                        break;
                    case MD5:
                    case SHA1:
                        entries = Checksums.parsePosixHashOutput(output);
                        break;
                    default:
                        throw new UnsupportedChecksumException("Unsupported checksum '" + checksum + "' on posix is not supported", null);
                }

                /*log.error("fileMappings: {}", fileMappings);
                log.error("entries: {}", entries);*/

                for (Checksums.HashEntry entry : entries) {
                    final VirtualPath entryPath = fileMappings.get(entry.getFile());

                    if (entryPath == null) {
                        //log.error("Something may be wrong parsing this output: {}", output);
                        throw new IllegalStateException("Unable to associate hash result with virtual path for '" + entry.getFile() + "'");
                    }

                    if (checksum == Checksum.CK) {
                        entryPath.getStats().setCksum(entry.getCksum());
                    } else if (checksum == Checksum.MD5) {
                        entryPath.getStats().setMd5(entry.getHash());
                    } else if (checksum == Checksum.SHA1) {
                        entryPath.getStats().setSha1(entry.getHash());
                    } else {
                        throw new UnsupportedChecksumException("Unsupported checksum '" + checksum + "' on posix is not supported", null);
                    }
                }

                // reset everything for next run
                exec = null;
                commandLength = 0;
                fileMappings.clear();
            }
        }
    }

    protected void hashFilesOnWindows(Checksum checksum, List<VirtualPath> paths) throws IOException {
        // we need to be smart about how many files we request in bulk, as the command line can only be so long
        final Map<String,VirtualPath> fileMapping = new HashMap<>();
        Exec exec = null;
        final StringBuilder fileListBuilder = new StringBuilder();

        for (int i = 0; i < paths.size(); i++) {
            final VirtualPath path = paths.get(i);

            // create new or add to request?
            if (exec == null) {
                exec = this.ssh.newExec().command("powershell")
                    .arg("-Command")
                    ;
            }

            // convert full path into a valid Windows path?
            // the powershell Get-FileHash will ALWAYS return the native windows path, not any special version we feed in
            final String fullPath = path.toString().substring(1).replace('/', '\\');   // chop off leading '/', and swap / with \
            fileMapping.put(fullPath, path);

            if (fileListBuilder.length() > 0) {
                fileListBuilder.append(",");
            }
            fileListBuilder.append("'").append(fullPath).append("'");

            // should we send this request?
            if (fileListBuilder.length() >= this.maxCommandLength || (i == paths.size() - 1)) {
                final String output;
                try {
                    // now we'll build the complete powershell command we want to send
                    StringBuilder commandBuilder = new StringBuilder();
                    commandBuilder.append("Get-FileHash -Algorithm ").append(checksum.toString()).append(" ");
                    commandBuilder.append(fileListBuilder);
                    commandBuilder.append(" | Select-Object Hash, Path | Format-List");
                    // powershell -Command "Get-FileHash -Algorithm MD5 'C:\Path\To\File1.iso', 'D:\Data\File2.zip' | Select-Object Hash, Path | Format-List"
                    output = exec.arg(commandBuilder)
                        .runCaptureOutput(false)
                        .toString();

//                    log.debug("md5sum output: {}", output);
                } catch (UnexpectedExitValueException e) {
                    // this likely means the command doesn't exist, either way we will need to flag this isn't supported
                    throw new UnsupportedChecksumException("Checksum strategy 'md5' on windows is not supported", e);
                }

                // parse output into entries
                final List<Checksums.HashEntry> entries = Checksums.parsePowershellHashFileOutput(output);
                for (Checksums.HashEntry entry : entries) {
                    final VirtualPath entryPath = fileMapping.get(entry.getFile());

                    if (entryPath == null) {
                        throw new IllegalStateException("Unable to associate hash result with virtual path for '" + entry.getFile() + "'");
                    }

                    if (checksum == Checksum.MD5) {
                        entryPath.getStats().setMd5(entry.getHash());
                    } else if (checksum == Checksum.SHA1) {
                        entryPath.getStats().setSha1(entry.getHash());
                    } else {
                        throw new UnsupportedChecksumException("Checksum '" + checksum + "' on windows is not supported", null);
                    }
                }

                // reset everything for next run
                exec = null;
                fileListBuilder.setLength(0);
                fileMapping.clear();
            }
        }
    }

}