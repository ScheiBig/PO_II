package edu.jeznach.po2.common.communication;

import edu.jeznach.po2.common.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.function.Function;

/**
 * Contains objects that represents messages used in communication between client and server
 */
public class Messages {

    private Messages() {  }

    /**
     * Tries to parse given message to message object
     * @param message the String containing message
     * @return {@code Msg} instance that contains parsed command, {@code null} if
     * message command is not supported
     */
    public static @Nullable Msg parseMessage(@NotNull String message) {
        String messageIdentifier = message.split(" ")[0];
        Function<String, Msg> parser;
        switch (messageIdentifier) {
            case CreateFile.cmd:
                parser = CreateFile::parse;
                break;
            case UpdateFile.cmd:
                parser = UpdateFile::parse;
                break;
            case DeleteFile.cmd:
                parser = DeleteFile::parse;
                break;
            case ShareFile.cmd:
                parser = ShareFile::parse;
                break;
            case UnshareFile.cmd:
                parser = UnshareFile::parse;
                break;
            case RequestFile.cmd:
                parser = RequestFile::parse;
                break;
            case RequestMapping.cmd:
                parser = RequestMapping::parse;
                break;
            case RequestUsers.cmd:
                parser = RequestUsers::parse;
                break;
            case RequestReceivers.cmd:
                parser = RequestReceivers::parse;
                break;
            case FinishConnection.cmd:
                parser = FinishConnection::parse;
                break;
            default:
                return null;
        }
        return parser.apply(message);
    }

    public static final @NotNull String OK = "OK";
    public static final @NotNull String NO = "NO";
    public static final @NotNull String NO_USER = "NONE";

    /**
     * Abstract command message
     * <blockquote><code>
     *     $CMD$ $USER$,
     *     <br>i.e.: {@linkplain RequestMapping RequestMapping} User
     * </code></blockquote>
     */
    public static abstract class Msg {

        /**
         * Name of command, should be name of class that represents command
         */
        @SuppressWarnings({"ConstantConditions", "unused"})
        public static final @NotNull String cmd = null;

        /**
         * User issuing command
         * @return the username of user issuing command
         */
        public abstract @NotNull String user();

        public abstract @NotNull String toProperString();

        /// Allows parsing via reflection
        @SuppressWarnings("unused")
        protected static @NotNull Msg parse(String message) {
            //noinspection ConstantConditions
            return null;
        }

        protected static short argCount() { return 2; }

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

        protected static short argCount() { return 3; }
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

        protected static short argCount() { return 5; }
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

        protected static short argCount() { return 4; }
    }

    /**
     * Command for creating new file
     */
    public static class CreateFile extends ModFileMsg {

        public static final @NotNull String cmd = "CreateFile";

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
        public String toString() {
            return String.join(" ", cmd, user, Base64.getEncoder().encodeToString(file.getBytes()), size.toString(), checksum);
        }

        @Override
        public @NotNull String toProperString() {
            return String.join(" ", cmd, user, file, size.toString(), checksum);
        }

        protected static @NotNull CreateFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new CreateFile(messageSplit[1], new String(Base64.getDecoder().decode(messageSplit[2].getBytes())), Long.parseLong(messageSplit[3]), messageSplit[4]);
        }
    }

    /**
     * Command for updating existing file. Should contain new values.
     */
    public static class UpdateFile extends ModFileMsg {

        public static final @NotNull String cmd = "UpdateFile";

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
        public String toString() {
            return String.join(" ", cmd, user, Base64.getEncoder().encodeToString(file.getBytes()), size.toString(), checksum);
        }

        @Override
        public @NotNull String toProperString() {
            return String.join(" ", cmd, user, file, size.toString(), checksum);
        }

        protected static @NotNull UpdateFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new UpdateFile(messageSplit[1], new String(Base64.getDecoder().decode(messageSplit[2].getBytes())), Long.parseLong(messageSplit[3]), messageSplit[4]);
        }
    }

    /**
     * Command for deleting existing files
     */
    public static class DeleteFile extends FileMsg {

        public static final @NotNull String cmd = "DeleteFile";

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
        public String toString() {
            return String.join(" ", cmd, user, Base64.getEncoder().encodeToString(file.getBytes()));
        }

        @Override
        public @NotNull String toProperString() {
            return String.join(" ", cmd, user, file);
        }

        protected static @NotNull DeleteFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new DeleteFile(messageSplit[1], new String(Base64.getDecoder().decode(messageSplit[2].getBytes())));
        }
    }

    /**
     * Command for sharing file with receiver
     */
    public static class ShareFile extends ShareFileMsg {

        public static final @NotNull String cmd = "ShareFile";

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
        public String toString() {
            return String.join(" ", cmd, user, Base64.getEncoder().encodeToString(file.getBytes()), receiver);
        }

        @Override
        public @NotNull String toProperString() {
            return String.join(" ", cmd, user, file, receiver);
        }

        protected static @NotNull ShareFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new ShareFile(messageSplit[1], new String(Base64.getDecoder().decode(messageSplit[2].getBytes())), messageSplit[3]);
        }
    }



    /**
     * Command for sharing file with receiver
     */
    public static class UnshareFile extends ShareFileMsg {

        public static final @NotNull String cmd = "UnshareFile";

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
        public String toString() {
            return String.join(" ", cmd, user, Base64.getEncoder().encodeToString(file.getBytes()), receiver);
        }

        @Override
        public @NotNull String toProperString() {
            return String.join(" ", cmd, user, file, receiver);
        }

        protected static @NotNull UnshareFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new UnshareFile(messageSplit[1], new String(Base64.getDecoder().decode(messageSplit[2].getBytes())), messageSplit[3]);
        }
    }

    public static class RequestFile extends FileMsg {

        public static final @NotNull String cmd = "RequestFile";

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        private @NotNull String file;
        @Override public @NotNull String file() { return file; }

        /**
         * Creates RequestFile command message
         * @param user the username of user issuing command
         * @param file the path to related file, relative to mapping node (user)
         */
        public RequestFile(@NotNull String user,
                          @NotNull String file) {
            this.user = user;
            this.file = file;
        }

        @Override
        public String toString() {
            return String.join(" ", cmd, user, Base64.getEncoder().encodeToString(file.getBytes()));
        }

        @Override
        public @NotNull String toProperString() {
            return String.join(" ", cmd, user, file);
        }

        protected static @NotNull RequestFile parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new RequestFile(messageSplit[1], new String(Base64.getDecoder().decode(messageSplit[2].getBytes())));
        }
    }

    public static class RequestMapping extends Msg {

        public static final @NotNull String cmd = "RequestMapping";

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        /**
         * Creates RequestFile command message
         * @param user the username of user issuing command
         */
        public RequestMapping(@NotNull String user) {
            this.user = user;
        }

        @Override
        public String toString() {
            return String.join(" ", cmd, user);
        }

        @Override
        public @NotNull String toProperString() {
            return toString();
        }

        protected static @NotNull RequestMapping parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new RequestMapping(messageSplit[1]);
        }
    }

    public static class RequestUsers extends Msg {

        public static final @NotNull String cmd = "RequestUsers";

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        /**
         * Creates RequestFile command message
         * @param user the username of user issuing command
         */
        public RequestUsers(@NotNull String user) {
            this.user = user;
        }

        @Override
        public String toString() {
            return String.join(" ", cmd, user);
        }

        @Override
        public @NotNull String toProperString() {
            return toString();
        }

        protected static @NotNull RequestUsers parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new RequestUsers(messageSplit[1]);
        }

    }

    public static class RequestReceivers extends FileMsg {

        public static final @NotNull String cmd = "RequestReceivers";

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        private @NotNull String file;
        @Override public @NotNull String file() { return file; }

        public RequestReceivers(@NotNull String user, @NotNull String file) {
            this.user = user;
            this.file = file;
        }

        @Override
        public String toString() {
            return String.join(" ", cmd, user, Base64.getEncoder().encodeToString(file.getBytes()));
        }

        @Override
        public @NotNull String toProperString() {
            return String.join(" ", cmd, user, file);
        }

        protected static @NotNull RequestReceivers parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new RequestReceivers(messageSplit[1], new String(Base64.getDecoder().decode(messageSplit[2].getBytes())));
        }
    }

    public static class FinishConnection extends Msg {

        public static final @NotNull String cmd = "FinishConnection";

        private @NotNull String user;
        @Override public @NotNull String user() { return user; }

        /**
         * Creates RequestFile command message
         * @param user the username of user issuing command
         */
        public FinishConnection(@NotNull String user) {
            this.user = user;
        }

        @Override
        public String toString() {
            return String.join(" ", cmd, user);
        }

        @Override
        public @NotNull String toProperString() {
            return toString();
        }

        protected static @NotNull FinishConnection parse(String message) {
            String[] messageSplit = message.split(" ");
            if (messageSplit.length != argCount())
                throw wrongArgumentNumber(cmd, messageSplit.length, argCount());
            return new FinishConnection(messageSplit[1]);
        }
    }
}
