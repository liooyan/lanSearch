package cn.lioyan.store;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * {@link FSDirectory}
 *
 * @author com.lioyan
 * @date 2023/5/26  15:39
 */
public abstract class FSDirectory implements Directory {

    volatile protected boolean isOpen = true;



    private final Set<String> pendingDeletes = Collections.newSetFromMap(new ConcurrentHashMap<String,Boolean>());
    private final AtomicInteger opsSinceLastDelete = new AtomicInteger();
    private final AtomicLong nextTempFileCounter = new AtomicLong();
    protected final Path directory;

    protected FSDirectory(Path directory) {
        this.directory = directory;
    }


    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        ensureOpen();
        maybeDeletePendingFiles();
        while (true) {
//            try {
//                String name = getTempFileName(prefix, suffix, nextTempFileCounter.getAndIncrement());
//                if (pendingDeletes.contains(name)) {
//                    continue;
//                }
//                return new FSIndexOutput(name,
//                        StandardOpenOption.WRITE, StandardOpenOption.CREATE_NEW);
//            } catch (FileAlreadyExistsException faee) {
//                // Retry with next incremented name
//                continue;
//            }
        }

//        return null;
    }

    @Override
    public void deleteFile(String name) throws IOException {
        if (pendingDeletes.contains(name)) {
            throw new NoSuchFileException("file \"" + name + "\" is already pending delete");
        }
        privateDeleteFile(name, false);
        maybeDeletePendingFiles();
    }


    private void privateDeleteFile(String name, boolean isPendingDelete) throws IOException {
        try {
            Files.delete(directory.resolve(name));
            pendingDeletes.remove(name);
        } catch (NoSuchFileException | FileNotFoundException e) {
            pendingDeletes.remove(name);
            if (isPendingDelete) {
            } else {
                throw e;
            }
        } catch (IOException ioe) {
            pendingDeletes.add(name);
        }
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

    @Override
    public long fileLength(String name) throws IOException {
        ensureOpen();
        if (pendingDeletes.contains(name)) {
            throw new NoSuchFileException("file \"" + name + "\" is pending delete");
        }
        return Files.size(directory.resolve(name));
    }

    @Override
    public String[] listAll() throws IOException {
        ensureOpen();
        return listAll(directory, pendingDeletes);
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

    protected final void ensureOpen() throws AlreadyClosedException {
        if (!isOpen) {
            throw new AlreadyClosedException("this Directory is closed");
        }
    }


    protected static String getTempFileName(String prefix, String suffix, long counter) {
        return IndexFileNames.segmentFileName(prefix, suffix + "_" + Long.toString(counter, Character.MAX_RADIX), "tmp");
    }



}
