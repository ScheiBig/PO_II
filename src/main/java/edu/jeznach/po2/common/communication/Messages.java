package edu.jeznach.po2.common.communication;

import edu.jeznach.po2.common.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Messages {

    private Messages() {  }

    public static @Nullable Msg parseMessage(String message) {

        return null;
    }

    /**
     * Abstract command message
     * <blockquote><code>
     *     $CMD$ $USER$,
     *     <br>i.e.: {linkplain RequestMapping RequestMapping} User
     * </code></blockquote>
     */
    public static abstract class Msg {

        /**
         * Command name
         * @return the name of command, should be name of class that represents command
         */
        public abstract @NotNull String cmd();

        /**
         * User issuing command
         * @return the username of user issuing command
         */
        public abstract @NotNull String user();

        /// Allows parsing via reflection
        protected abstract @NotNull Msg parse(String message);

        protected short argCount() { return 2; }

        static IllegalArgumentException wrongArgumentNumber(@NotNull String name,
                                                            @NotNull Integer actual,
                                                            @NotNull Short expected) {
            return new IllegalArgumentException(name + ": parsing error, argument count: " +
                                                actual + ", expected: " + expected);
        }
    }

    /**
     * Abstract file command message
     * <blockquote><code>
     *     $CMD$ $USER$ $FILE$,
     *     <br>i.e.: {@linkplain DeleteFile DeleteFile} User docs/sum.txt
     * </code></blockquote>
     */
    public static abstract class FileMsg extends Msg {

        /**
         * File related to command
         * @return the path to related file, relative to mapping node (user)
         */
        public abstract @NotNull String file();

        @Override protected short argCount() { return 3; }
    }

    /**
     * Abstract file modification command message
     * <blockquote><code>
     *     $CMD$ $USER$ $FILE$ $SIZE$ $CHECKSUM$,
     *     <br>i.e.: {@linkplain CreateFile CreateFile} User docs/sum.txt 50 2d44...40ff
     * </code></blockquote>
     */
    public static abstract class ModFileMsg extends FileMsg {

        /**
         * Size of related file
         * @return the size of related file, in bytes
         */
        public abstract @NotNull Long size();

        /**
         * Checksum of related file
         * @return the checksum of related file contents
         * @see Configuration#CHECKSUM_ALGORITHM
         */
        public abstract @NotNull String checksum();

        @Override protected short argCount() { return 5; }
    }
    /**
     * Abstract file sharing command message
     * <blockquote><code>
     *     $CMD$ $USER$ $FILE$ $RECEIVER$,
     *     <br>i.e.: {@linkplain UnshareFile UnshareFile} User docs/sum.txt Client
     * </code></blockquote>
     */
    public static abstract class ShareFileMsg extends FileMsg {

        /**
         * Receiver of related file
         * @return the username of user that is receiver
         */
        public abstract @NotNull String receiver();

        @Override protected short argCount() { return 4; }
    }

    /**
     * Command for creating new file
     */
    public static class CreateFile extends ModFileMsg {

        private @NotNull String cmd = this.getClass().getSimpleName();
        @Override public @NotNull String cmd() { return cmd; }

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        private @NotNull String file;
        @Override public @NotNull String file() { return file; }

        private @NotNull Long size;
        @Override public @NotNull Long size() { return size; }

        private @NotNull String checksum;
        @Override public @NotNull String checksum() { return checksum; }

        /**
         * Creates NewFile command message
         * @param user the username of user issuing command
         * @param file the path to related file, relative to mapping node (user)
         * @param size the size of related file, in bytes
         * @param checksum the checksum of related file contents
         */
        public CreateFile(@NotNull String user,
                          @NotNull String file,
                          @NotNull Long size,
                          @NotNull String checksum) {
            this.user = user;
            this.file = file;
            this.size = size;
            this.checksum = checksum;
        }

        @Override
        protected @NotNull Messages.CreateFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd(), messageSplit.length, argCount());
            return new CreateFile(messageSplit[1], messageSplit[2], Long.parseLong(messageSplit[3]), messageSplit[4]);
        }
    }

    /**
     * Command for updating existing file. Should contain new values.
     */
    public static class UpdateFile extends ModFileMsg {

        private @NotNull String cmd = this.getClass().getSimpleName();
        @Override public @NotNull String cmd() { return cmd; }

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        private @NotNull String file;
        @Override public @NotNull String file() { return file; }

        private @NotNull Long size;
        @Override public @NotNull Long size() { return size; }

        private @NotNull String checksum;
        @Override public @NotNull String checksum() { return checksum; }

        /**
         * Creates UpdateFile command message
         * @param user the username of user issuing command
         * @param file the path to related file, relative to mapping node (user)
         * @param size the new size of related file, in bytes
         * @param checksum the new checksum of related file contents
         */
        public UpdateFile(@NotNull String user,
                          @NotNull String file,
                          @NotNull Long size,
                          @NotNull String checksum) {
            this.user = user;
            this.file = file;
            this.size = size;
            this.checksum = checksum;
        }

        @Override
        protected @NotNull UpdateFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd(), messageSplit.length, argCount());
            return new UpdateFile(messageSplit[1], messageSplit[2], Long.parseLong(messageSplit[3]), messageSplit[4]);
        }
    }

    /**
     * Command for deleting existing files
     */
    public static class DeleteFile extends FileMsg {
        private @NotNull String cmd = this.getClass().getSimpleName();
        @Override public @NotNull String cmd() { return cmd; }

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        private @NotNull String file;
        @Override public @NotNull String file() { return file; }

        /**
         * Creates DeleteFile command message
         * @param user the username of user issuing command
         * @param file the path to related file, relative to mapping node (user)
         */
        public DeleteFile(@NotNull String user,
                          @NotNull String file) {
            this.user = user;
            this.file = file;
        }

        @Override
        protected @NotNull DeleteFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd(), messageSplit.length, argCount());
            return new DeleteFile(messageSplit[1], messageSplit[2]);
        }
    }

    /**
     * Command for sharing file with receiver
     */
    public static class ShareFile extends ShareFileMsg {

        private @NotNull String cmd = this.getClass().getSimpleName();
        @Override public @NotNull String cmd() { return cmd; }

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        private @NotNull String file;
        @Override public @NotNull String file() { return file; }

        private @NotNull String receiver;
        @Override public @NotNull String receiver() { return receiver; }

        /**
         * Creates ShareFile command message
         * @param user the username of user issuing command
         * @param file the path to related file, relative to mapping node (user)
         * @param receiver the username of user that file is shared with
         */
        public ShareFile(@NotNull String user,
                         @NotNull String file,
                         @NotNull String receiver) {
            this.user = user;
            this.file = file;
            this.receiver = receiver;
        }

        @Override
        protected @NotNull ShareFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd(), messageSplit.length, argCount());
            return new ShareFile(messageSplit[1], messageSplit[2], messageSplit[3]);
        }
    }



    /**
     * Command for sharing file with receiver
     */
    public static class UnshareFile extends ShareFileMsg {

        private @NotNull String cmd = this.getClass().getSimpleName();
        @Override public @NotNull String cmd() { return cmd; }

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        private @NotNull String file;
        @Override public @NotNull String file() { return file; }

        private @NotNull String receiver;
        @Override public @NotNull String receiver() { return receiver; }

        /**
         * Creates UnshareFile command message
         * @param user the username of user issuing command
         * @param file the path to related file, relative to mapping node (user)
         * @param receiver the username of user that file should no longer be shared to
         */
        public UnshareFile(@NotNull String user,
                         @NotNull String file,
                         @NotNull String receiver) {
            this.user = user;
            this.file = file;
            this.receiver = receiver;
        }

        @Override
        protected @NotNull UnshareFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd(), messageSplit.length, argCount());
            return new UnshareFile(messageSplit[1], messageSplit[2], messageSplit[3]);
        }
    }
}
