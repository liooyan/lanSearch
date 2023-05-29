package cn.lioyan.util;

import java.io.Closeable;
import java.util.Arrays;

public class IOUtils {
    public static void closeWhileHandlingException(Closeable... objects) {
        closeWhileHandlingException(Arrays.asList(objects));
    }

    /**
     * Closes all given <tt>Closeable</tt>s, suppressing all thrown non {@link VirtualMachineError} exceptions.
     * Even if a {@link VirtualMachineError} is thrown all given closeable are closed.
     * @see #closeWhileHandlingException(Closeable...)
     */
    public static void closeWhileHandlingException(Iterable<? extends Closeable> objects) {
        VirtualMachineError firstError = null;
        Throwable firstThrowable = null;
        for (Closeable object : objects) {
            try {
                if (object != null) {
                    object.close();
                }
            } catch (VirtualMachineError e) {
                firstError = useOrSuppress(firstError, e);
            } catch (Throwable t) {
                firstThrowable = useOrSuppress(firstThrowable, t);
            }
        }
        if (firstError != null) {
            // we ensure that we bubble up any errors. We can't recover from these but need to make sure they are
            // bubbled up. if a non-VMError is thrown we also add the suppressed exceptions to it.
            if (firstThrowable != null) {
                firstError.addSuppressed(firstThrowable);
            }
            throw firstError;
        }
    }
    public static <T extends Throwable> T useOrSuppress(T first, T second) {
        if (first == null) {
            return second;
        } else {
            first.addSuppressed(second);
        }
        return first;
    }
}
