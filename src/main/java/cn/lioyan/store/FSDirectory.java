package cn.lioyan.store;

import cn.lioyan.index.IndexFileNames;

import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;


public  abstract class FSDirectory extends BaseDirectory{


    protected final Path directory;

    /**
     * 首次删除是，删除异常的文件记录。
     */
    private final Set<String> pendingDeletes = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());


    public Path getDirectory() {
        ensureOpen();
        return directory;
    }



    private final AtomicInteger opsSinceLastDelete = new AtomicInteger();

    private final AtomicLong nextTempFileCounter = new AtomicLong();


    public static FSDirectory open(Path path) throws IOException {
        return open(path, FSLockFactory.getDefault());
    }

    /** Just like {@link #open(Path)}, but allows you to
     *  also specify a custom {@link LockFactory}. */
    public static FSDirectory open(Path path, LockFactory lockFactory) throws IOException {
        return new NIOFSDirectory(path, lockFactory);
    }
    protected FSDirectory(Path path) throws IOException {
       this(path,FSLockFactory.getDefault());
    }
    protected FSDirectory(Path path, LockFactory lockFactory) throws IOException {
        super(lockFactory);
        // If only read access is permitted, createDirectories fails even if the directory already exists.
        if (!Files.isDirectory(path)) {
            Files.createDirectories(path);  // create directory, if it doesn't exist
        }
        directory = path.toRealPath();
    }

    @Override
    public String[] listAll() throws IOException {
        ensureOpen();
        return listAll(directory, pendingDeletes);
    }



    @Override
    public void deleteFile(String name) throws IOException {
        if (pendingDeletes.contains(name)) {
            throw new NoSuchFileException("file \"" + name + "\" is already pending delete");
        }
        privateDeleteFile(name, false);
        maybeDeletePendingFiles();
    }

    @Override
    public long fileLength(String name) throws IOException {
        ensureOpen();
        if (pendingDeletes.contains(name)) {
            throw new NoSuchFileException("file \"" + name + "\" is pending delete");
        }
        return Files.size(directory.resolve(name));
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        ensureOpen();
        maybeDeletePendingFiles();
        // If this file was pending delete, we are now bringing it back to life:
        if (pendingDeletes.remove(name)) {
            privateDeleteFile(name, true); // try again to delete it - this is best effort
            pendingDeletes.remove(name); // watch out - if the delete fails it put
        }
        return new FSIndexOutput(name);
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        ensureOpen();
        maybeDeletePendingFiles();
        while (true) {
            try {
                String name = getTempFileName(prefix, suffix, nextTempFileCounter.getAndIncrement());
                if (pendingDeletes.contains(name)) {
                    continue;
                }
                return new FSIndexOutput(name,
                        StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
            } catch (FileAlreadyExistsException faee) {
                // Retry with next incremented name
            }
        }
    }
    protected void ensureCanRead(String name) throws IOException {
        if (pendingDeletes.contains(name)) {
            throw new NoSuchFileException("file \"" + name + "\" is pending delete and cannot be opened for read");
        }
    }

    @Override
    public void rename(String source, String dest) throws IOException {
        ensureOpen();
        if (pendingDeletes.contains(source)) {
            throw new NoSuchFileException("file \"" + source + "\" is pending delete and cannot be moved");
        }
        maybeDeletePendingFiles();
        if (pendingDeletes.remove(dest)) {
            privateDeleteFile(dest, true); // try again to delete it - this is best effort
            pendingDeletes.remove(dest); // watch out if the delete fails it's back in here.
        }
        Files.move(directory.resolve(source), directory.resolve(dest), StandardCopyOption.ATOMIC_MOVE);
    }

    protected static String getTempFileName(String prefix, String suffix, long counter) {
        return IndexFileNames.segmentFileName(prefix, suffix + "_" + Long.toString(counter, Character.MAX_RADIX), "tmp");
    }
    @Override
    public void close() throws IOException {
        isOpen = false;
        deletePendingFiles();
    }

    @Override
    public void copyFrom(Directory from, String src, String dest, IOContext context) throws IOException {

    }


    private void maybeDeletePendingFiles() throws IOException {
        if (pendingDeletes.isEmpty() == false) {
            // This is a silly heuristic to try to avoid O(N^2), where N = number of files pending deletion, behaviour on Windows:
            int count = opsSinceLastDelete.incrementAndGet();
            if (count >= pendingDeletes.size()) {
                opsSinceLastDelete.addAndGet(-count);
                deletePendingFiles();
            }
        }
    }

    public synchronized void deletePendingFiles() throws IOException {
        if (pendingDeletes.isEmpty() == false) {

            // TODO: we could fix IndexInputs from FSDirectory subclasses to call this when they are closed?

            // Clone the set since we mutate it in privateDeleteFile:
            for(String name : new HashSet<>(pendingDeletes)) {
                privateDeleteFile(name, true);
            }
        }
    }
    private void privateDeleteFile(String name, boolean isPendingDelete) throws IOException {
        try {
            Files.delete(directory.resolve(name));
            pendingDeletes.remove(name);
        } catch (NoSuchFileException | FileNotFoundException e) {
            // We were asked to delete a non-existent file:
            pendingDeletes.remove(name);
            if (isPendingDelete) {
                // TODO: can we remove this OS-specific hacky logic?  If windows deleteFile is buggy, we should instead contain this workaround in
                // a WindowsFSDirectory ...
                // LUCENE-6684: we suppress this check for Windows, since a file could be in a confusing "pending delete" state, failing the first
                // delete attempt with access denied and then apparently falsely failing here when we try ot delete it again, with NSFE/FNFE
            } else {
                throw e;
            }
        } catch (IOException ioe) {
            // On windows, a file delete can fail because there's still an open
            // file handle against it.  We record this in pendingDeletes and
            // try again later.

            // TODO: this is hacky/lenient (we don't know which IOException this is), and
            // it should only happen on filesystems that can do this, so really we should
            // move this logic to WindowsDirectory or something

            // TODO: can/should we do if (Constants.WINDOWS) here, else throw the exc?
            // but what about a Linux box with a CIFS mount?
            pendingDeletes.add(name);
        }
    }


    private static String[] listAll(Path dir, Set<String> skipNames) throws IOException {
        List<String> entries = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                if (skipNames == null || skipNames.contains(name) == false) {
                    entries.add(name);
                }
            }
        }

        String[] array = entries.toArray(new String[entries.size()]);
        Arrays.sort(array);
        return array;
    }


    final class FSIndexOutput extends OutputStreamIndexOutput {
        /**
         * The maximum chunk size is 8192 bytes, because file channel mallocs
         * a native buffer outside of stack if the write buffer size is larger.
         */
        static final int CHUNK_SIZE = 8192;

        public FSIndexOutput(String name) throws IOException {
            this(name, StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
        }

        FSIndexOutput(String name, OpenOption... options) throws IOException {
            super("FSIndexOutput(path=\"" + directory.resolve(name) + "\")", name, new FilterOutputStream(Files.newOutputStream(directory.resolve(name), options)) {
                // This implementation ensures, that we never write more than CHUNK_SIZE bytes:
                @Override
                public void write(byte[] b, int offset, int length) throws IOException {
                    while (length > 0) {
                        final int chunk = Math.min(length, CHUNK_SIZE);
                        out.write(b, offset, chunk);
                        length -= chunk;
                        offset += chunk;
                    }
                }
            }, CHUNK_SIZE);
        }
    }
}
